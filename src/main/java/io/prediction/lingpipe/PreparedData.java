package io.prediction.lingpipe;

import org.apache.predictionio.controller.SanityCheck;

import java.io.Serializable;

public class PreparedData implements Serializable, SanityCheck {
    private final TrainingData trainingData;
    public PreparedData(TrainingData trainingData) {
        this.trainingData = trainingData;
    }
    public TrainingData getTrainingData() {return trainingData;}

    @Override
    public void sanityCheck() {
        if (trainingData.getSamples().isEmpty()) {
            throw new AssertionError("trainingData data is empty");
        }
    }
}
