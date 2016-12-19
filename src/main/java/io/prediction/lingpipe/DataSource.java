package io.prediction.lingpipe;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.apache.predictionio.controller.EmptyParams;
import org.apache.predictionio.controller.java.PJavaDataSource;
import org.apache.predictionio.data.storage.Event;
import org.apache.predictionio.data.store.java.OptionHelper;
import org.apache.predictionio.data.store.java.PJavaEventStore;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;

import java.util.*;

public class DataSource extends PJavaDataSource<TrainingData, EmptyParams, Query, Set<String>> {

    private static final Logger logger = LoggerFactory.getLogger(DataSource.class);
    private final DataSourceParams dsp;
    private static Map<String,List<String>> Stopwords =  Maps.newConcurrentMap();
    public DataSource(DataSourceParams dsp) {
        this.dsp = dsp;
    }

    @Override
    public TrainingData readTraining(SparkContext sc) {
        try {

            JavaRDD<Item> samples = PJavaEventStore.find(
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
                    .filter((Function<Event, Boolean>) event -> isEligible(event))
                    .map((Function<Event, Item>) event -> getItem(event))
                    .filter(item -> item != null);

            List<Boolean> collect = PJavaEventStore.find(
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
                    .map((Function<Event, Boolean>) event -> getStopword(event)).collect();

            return new TrainingData(samples);
        }
        catch (Exception e) {
            Exceptions.log(e);
        }
        return null;
    }

    private boolean getStopword(Event event) {
        String locale = event.properties().get(dsp.getstopwordlocaleField(), String.class).toLowerCase();
        String word = event.properties().get(dsp.stopwordField(), String.class);
        if (!Stopwords.containsKey(locale)) {
            List<String> words = new ArrayList<>();
            words.add(word);
            Stopwords.put(locale.toLowerCase(), words);
        } else {
            List<String> categories = Stopwords.get(locale);
            categories.add(word);;
        }
        return true;
    }

    private boolean isEligible(Event event) {
        try {
            String cleanedtext = TextCleaner.cleanUpSpecialCharachters(event.properties().get(dsp.getQueryField(), String.class));
            String query = event.properties().get(dsp.getQueryField(), String.class);
            String locale = event.properties().get(dsp.getLocaleField(), String.class);
            PreProcessingPipeline.Process(query,Stopwords.get(locale.toLowerCase()), locale.toLowerCase());
            String category = event.properties().get(dsp.getCategoryField(), String.class);
            if(Strings.isNullOrEmpty(query) || Strings.isNullOrEmpty(category) || Strings.isNullOrEmpty(locale)){
                return false;
            }
            return cleanedtext.length() > 0;
        }catch (Exception e) {
            Exceptions.log(e);
        }
        return false;
    }

    private Item getItem(Event event){
        try {
            String query = event.properties().get(dsp.getQueryField(), String.class);
            String locale = event.properties().get(dsp.getLocaleField(), String.class);
            PreProcessingPipeline.Process(query,Stopwords.get(locale.toLowerCase()), locale.toLowerCase());
            String category = event.properties().get(dsp.getCategoryField(), String.class);
            return new Item(query.toLowerCase(), category.toLowerCase(), locale);
        }
        catch (Exception e) {
            Exceptions.log(e);
         }
         return null;
        }
 }
