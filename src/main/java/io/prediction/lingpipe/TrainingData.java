package io.prediction.lingpipe;

import org.apache.predictionio.controller.SanityCheck;
import org.apache.spark.api.java.JavaRDD;

import java.io.Serializable;

public class TrainingData implements Serializable, SanityCheck {
    private final JavaRDD<Item> Samples;
    public TrainingData(JavaRDD<Item> samples) {
        this.Samples = samples;
    }
    public JavaRDD<Item> getSamples() {
        return Samples;
    }

    @Override
    public void sanityCheck() {
        if (Samples.isEmpty()) {
            throw new AssertionError("Samples data is empty");
        }
    }
}
