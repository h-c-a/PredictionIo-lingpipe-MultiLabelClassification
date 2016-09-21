package io.prediction.lingpipe;

import java.io.Serializable;

public class Query implements Serializable{
    private final String text;
    private final String locale;

    public Query(String text,String locale) {
        this.text = text;
        this.locale = locale;
    }

    public String getQuerytext() {
        return text;
    }
    public String getLocale() {
        return locale;
    }

    @Override
    public String toString() {
        return "Query{" +
                "text='" + text + '\'' +
                "locale='" + locale + '\'' +
                '}';
    }
}