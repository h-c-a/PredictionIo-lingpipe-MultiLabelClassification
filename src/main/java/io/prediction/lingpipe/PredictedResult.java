package io.prediction.lingpipe;

import java.io.Serializable;
import java.util.List;

public class PredictedResult implements Serializable{
    private final List<Result> items;

    public PredictedResult(List<Result> items) {
        this.items = items;
    }

    public List<Result> getItemScores() {
        return items;
    }

    @Override
    public String toString() {
        return "PredictedResult{" +
                "items=" + items +
                '}';
    }
}
