package io.prediction.lingpipe;

import com.aliasi.classify.*;
import com.aliasi.lm.NGramProcessLM;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.apache.predictionio.controller.java.PJavaAlgorithm;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.rdd.RDD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

import java.util.*;

public class Algorithm extends PJavaAlgorithm<PreparedData, Model, Query, PredictedResult> {

    private static final Logger logger = LoggerFactory.getLogger(Algorithm.class);
    private final AlgorithmParams ap;

    public Algorithm(AlgorithmParams ap) {
        this.ap = ap;
    }
    private static Map<ModelConfig, Set<String>> configSetMap = Maps.newConcurrentMap();
    private static Map<ModelConfig, List<Item>> configListItemsMap = Maps.newConcurrentMap();
    private static Map<ModelConfig, DynamicLMClassifier<NGramProcessLM>> ClassifierList = new HashMap<>();

    @Override
    public Model train(SparkContext sc, PreparedData preparedData) {

        TrainingData data = preparedData.getTrainingData();
        JavaRDD<Item> docsamples = data.getSamples();
        List<Boolean> BuildMaps = docsamples.map((Function<Item, Boolean>) item -> getItem(item)).collect();
        for (ModelConfig modelConfig : configSetMap.keySet()) {
            Set<String> disticntList = configSetMap.get(modelConfig);
            if (disticntList.size() < 2) {
                continue;
            }
            String[] CategoryArray = new String[disticntList.size()];
            CategoryArray = disticntList.toArray(CategoryArray);
            DynamicLMClassifier<NGramProcessLM> classifier
                    = DynamicLMClassifier.createNGramProcess(CategoryArray, ap.getngramsize());

            List<Item> list = configListItemsMap.get(modelConfig);

            list.stream().filter(item -> item != null).forEach(item -> {
                        Classification classification
                                = new Classification(item.getCategory());
                        Classified<CharSequence> classified
                                = new Classified<>(item.getQuery(), classification);
                        classifier.handle(classified);
                    }
            );
            ClassifierList.put(modelConfig, classifier);
        }

        return new Model(ClassifierList);
    }



    private boolean getItem(Item item) {
        ModelConfig modelConfig = new ModelConfig(item.getLocale());
        if (!configSetMap.containsKey(modelConfig)) {
            Set<String> categories = new HashSet<>();
            List<Item> items = new ArrayList<>();
            categories.add(item.getCategory());
            items.add(item);
            configListItemsMap.put(modelConfig, items);
            configSetMap.put(modelConfig, categories);
        } else {
            Set<String> categories = configSetMap.get(modelConfig);
            categories.add(item.getCategory());
            List<Item> itemList = configListItemsMap.get(modelConfig);
            itemList.add(item);
            configListItemsMap.put(modelConfig, itemList);
            configSetMap.put(modelConfig, categories);
        }
        return true;
    }


    @Override
    public PredictedResult predict(Model model, final Query query) {
        logger.info(String.format("prediction: " + "model: %s." + " query: %s", model.toString(), query.toString()));
        List<Result> results = new ArrayList<>();
        try {
            if (Strings.isNullOrEmpty(query.getLocale()) || Strings.isNullOrEmpty(query.getQuerytext())) {
                new PredictedResult(results);
          }
            for (Map.Entry<ModelConfig, JointClassifier<CharSequence>> entry : model.getCompiledClassifier().entrySet()) {
                ModelConfig modelConfig = entry.getKey();
                JointClassifier<CharSequence> classifier = entry.getValue();
                if (Objects.equals(modelConfig.getLocale().toLowerCase(), query.getLocale().toLowerCase())) {
                    JointClassification predictedFactory = classifier.classify(model.getCleanedQuery(query));
                    String bestCategory = predictedFactory.bestCategory();
                    for (int index = 0; index < ap.getMaxresults() && index < predictedFactory.size(); index++) {
                        double score = predictedFactory.score(index);
                        Result predictedResult = new Result(predictedFactory.category(index),  String.valueOf(score));
                        results.add(predictedResult);
                    }
                    logger.info(String.format("prediction: " + "Query: %s." + " Best Category Guessed: %s", query.toString(), bestCategory));
                    return new PredictedResult(results);
                }
            }
        } catch (Exception e) {
            logger.info(String.format("Exception: " + "Exception: %s.", e.toString()));
            Exceptions.log(e);

            return new PredictedResult(results);

        }
        return new PredictedResult(results);
    }


    @Override
    public RDD<Tuple2<Object, PredictedResult>> batchPredict(Model model, RDD<Tuple2<Object, Query>> qs) {
        try {
            List<Tuple2<Object, Query>> indexQueries = qs.toJavaRDD().collect();
            List<Tuple2<Object, PredictedResult>> results = new ArrayList<>();
            for (Tuple2<Object, Query> indexQuery : indexQueries) {
                results.add(new Tuple2<>(indexQuery._1(), predict(model, indexQuery._2())));
            }

            return new JavaSparkContext(qs.sparkContext()).parallelize(results).rdd();
        } catch (Exception e) {
            Exceptions.log(e);
            Exceptions.rethrowRuntimeException(e);
        }
        return null;
    }

}
