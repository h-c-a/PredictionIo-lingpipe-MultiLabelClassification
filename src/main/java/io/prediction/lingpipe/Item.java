package io.prediction.lingpipe;

import java.io.Serializable;

public class Item implements Serializable {
    private final String Query;
    private final String Category;
    private final String Locale;

    public Item(String query, String category,String locale) {
        this.Query = query;
        this.Category = category;
        this.Locale = locale;
    }

    public String getQuery() {
        return Query;
    }
    public String getCategory() {
        return Category;
    }
    public String getLocale() {
        return Locale;
    }

    @Override
    public String toString() {
        return "Item{" +
                "Query='" + Query + '\'' +
                ", Category='" + Category + '\'' +
                ", Locale='" + Locale + '\'' +
                '}';
    }
}