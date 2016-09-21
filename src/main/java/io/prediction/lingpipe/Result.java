package io.prediction.lingpipe;

import java.io.Serializable;

public class Result implements Serializable{
    private final String Category;
    private final String Score;

    public Result(String category,String score) {
        this.Category = category;
        this.Score = score;
    }

    public String getItemCategory() {
        return Category;
    }
    public String getItemScore() {
        return Score;
    }

    @Override
    public String toString() {
        return "PredictedResult{" +
                "Category=" + Category +
                "Score=" + Score +
                '}';
    }
}