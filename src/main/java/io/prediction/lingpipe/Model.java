package io.prediction.lingpipe;

import com.aliasi.classify.DynamicLMClassifier;
import com.aliasi.classify.JointClassifier;
import com.aliasi.lm.NGramProcessLM;
import com.aliasi.util.AbstractExternalizable;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.apache.predictionio.controller.Params;
import org.apache.predictionio.controller.PersistentModel;
import org.apache.predictionio.data.storage.Event;
import org.apache.predictionio.data.store.java.OptionHelper;
import org.apache.predictionio.data.store.java.PJavaEventStore;
import org.apache.predictionio.workflow.EngineLanguage;
import org.apache.predictionio.workflow.JsonExtractorOption;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.function.Function;
import org.joda.time.DateTime;
import org.json4s.JsonAST;
import org.json4s.ReaderInput;
import org.json4s.jackson.JsonMethods$;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;
import scala.Tuple2;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.apache.predictionio.workflow.WorkflowUtils.getParamsFromJsonByFieldAndClass;

public class Model implements PersistentModel<AlgorithmParams>, Serializable {

    private static final Logger logger = LoggerFactory.getLogger(Model.class);
    private static Map<ModelConfig, DynamicLMClassifier<NGramProcessLM>> ClassifierList;
    private static AlgorithmParams ap;
    private static Map<String,List<String>> Stopwords =  Maps.newConcurrentMap();

    private static DataSourceParams dsp = null;
    private static Map<ModelConfig,JointClassifier<CharSequence>> CompiledClassifier;

    public Model(Map<ModelConfig, DynamicLMClassifier<NGramProcessLM>> modelpayload) {
        this.ClassifierList = modelpayload;
    }

    public static <R> Model load(String id, Params  params, SparkContext sc) {
        try {
            JsonAST.JValue jval = null;
            BufferedReader br = new BufferedReader(new FileReader("engine.json"));
            jval = JsonMethods$.MODULE$.parse(new ReaderInput(br), false);

            if(jval == null){
                throw new RuntimeException("Engine.json not found. Please verify if the json exists.");
            }
            scala.collection.immutable.Map<String, Class<?>> Classmap = new scala.collection.immutable.Map.Map1<>("algo", Algorithm.class);
            Tuple2<String, Params> engineParamsGenerator = getParamsFromJsonByFieldAndClass(jval, "algorithms",Classmap, EngineLanguage.Java(), JsonExtractorOption.Gson());
            ap = (AlgorithmParams) engineParamsGenerator._2;
            CompiledClassifier  = new HashMap<>();
                       for(int k=0; k< ap.getmodelLocales().size(); k++){
                        int indexK = k;
                         String fileName  = ap.getmodelbuildfilepath() + ap.getmodelLocales().get(indexK) + "_" + id;
                         Path path = Paths.get(fileName);
                         if (Files.exists(path)) {
                            ModelConfig modelConfig = new ModelConfig(ap.getmodelLocales().get(indexK));
                            CompiledClassifier.put(modelConfig,(JointClassifier<CharSequence>) AbstractExternalizable.readObject(new File(fileName)));
                         }
            }

            if(dsp == null){
                JsonAST.JValue dspjval = null;
                BufferedReader dspbr = new BufferedReader(new FileReader("engine.json"));
                dspjval = JsonMethods$.MODULE$.parse(new ReaderInput(dspbr), false);

                if(dspjval == null){
                    throw new RuntimeException("Engine.json not found. Please verify if the json exists.");
                }
                scala.collection.immutable.Map<String, Class<?>> dspClassmap = new scala.collection.immutable.Map.Map1<>("", DataSource.class);
                Tuple2<String, Params> dspengineParamsGenerator = getParamsFromJsonByFieldAndClass(dspjval, "datasource",dspClassmap, EngineLanguage.Java(), JsonExtractorOption.Gson());
                dsp = (DataSourceParams) dspengineParamsGenerator._2;
            }


            PJavaEventStore.find(
                    dsp.getAppName(),
                    OptionHelper.<String>none(),
                    OptionHelper.<DateTime>none(),
                    OptionHelper.<DateTime>none(),
                    OptionHelper.some(dsp.getEntityTypeField()),
                    OptionHelper.<String>none(),
                    OptionHelper.some(Collections.singletonList(dsp.getEventTypeField())),
                    OptionHelper.<Option<String>>none(),
                    OptionHelper.<Option<String>>none(),
                    sc)
                    .filter((Function<Event, Boolean>) event -> isEligible(event)).collect();


              PJavaEventStore.find(
                    dsp.getAppName(),
                    OptionHelper.<String>none(),
                    OptionHelper.<DateTime>none(),
                    OptionHelper.<DateTime>none(),
                    OptionHelper.some(dsp.getstopwordeEntityTypeField()),
                    OptionHelper.<String>none(),
                    OptionHelper.some(Collections.singletonList(dsp.getstopwordEventTypeField())),
                    OptionHelper.<Option<String>>none(),
                    OptionHelper.<Option<String>>none(),
                    sc)
                    .map((Function<Event, Boolean>) event -> fetchStopword(event)).collect();


            return new Model(ClassifierList);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Unable to load data");
    }

    private static boolean fetchStopword(Event event) {
        String locale = event.properties().get(dsp.getstopwordlocaleField(), String.class);
        String word = event.properties().get(dsp.stopwordField(), String.class);
        if (!Stopwords.containsKey(locale)) {
            List<String> words = new ArrayList<>();
            words.add(word);
            Stopwords.put(locale.toLowerCase(), words);
        } else {
            List<String> categories = Stopwords.get(locale);
            categories.add(word);
        }
        return true;
    }

    private static boolean isEligible(Event event) {
        try {
            String category = event.properties().get(dsp.getCategoryField(), String.class);
             if(Strings.isNullOrEmpty(category)) {
                return false;
            }
            return true;
        }catch (Exception e) {
            Exceptions.log(e);
        }
        return false;
    }

    public String getCleanedQuery(Query query) {return  PreProcessingPipeline.Process(query.getQuerytext(),Stopwords.get(query.getLocale().toLowerCase()), query.getLocale());}

    public Map<ModelConfig, DynamicLMClassifier<NGramProcessLM>> getClassifier() {
        return ClassifierList;
    }

    public Map<ModelConfig, JointClassifier<CharSequence>> getCompiledClassifier() {return CompiledClassifier;}

    @Override
    public boolean save(String id, AlgorithmParams params, SparkContext sc) {
        try {
            for (Map.Entry<ModelConfig, DynamicLMClassifier<NGramProcessLM>> entry : ClassifierList.entrySet()) {
                ModelConfig modelConfig = entry.getKey();
                DynamicLMClassifier<NGramProcessLM> classifier = entry.getValue();
                String fileName  = params.getmodelbuildfilepath() + "_" + modelConfig.getLocale() + "_" + id;
                AbstractExternalizable.compileTo(classifier, new File(fileName));
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
