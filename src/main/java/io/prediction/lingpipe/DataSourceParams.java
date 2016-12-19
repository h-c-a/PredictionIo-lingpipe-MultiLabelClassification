package io.prediction.lingpipe;

import org.apache.predictionio.controller.Params;

public class DataSourceParams implements Params{
    private final String appName;
    private final String categoryField;
    private final String queryField;
    private final String localeField;
    private final String entityType;
    private final String eventType;
    private final String stopwordField;
    private final String stopwordentityType;
    private final String stopwordeventType;
    private final String stopwordlocaleField;


    public DataSourceParams(String appName, String categoryField, String queryField, String entityType, String eventType, String stopwordField, String stopwordentityType, String stopwordeventType, String stopwordlocaleField,String localeField) {
        this.appName = appName;
        this.categoryField= categoryField;
        this.queryField = queryField;
        this.localeField = localeField;
        this.entityType = entityType;
        this.eventType = eventType;
        this.stopwordField = stopwordField;
        this.stopwordentityType = stopwordentityType;
        this.stopwordeventType = stopwordeventType;
        this.stopwordlocaleField = stopwordlocaleField;
    }

    public String getAppName() {
        return appName;
    }
    public String getCategoryField() { return categoryField; }
    public String getQueryField() { return queryField; }
    public String getLocaleField() { return localeField; }
    public String getEntityTypeField() {return entityType;}
    public String getEventTypeField() {
        return eventType;
    }
    public String stopwordField() {
        return stopwordField;
    }
    public String getstopwordeEntityTypeField() {
        return stopwordentityType;
    }
    public String getstopwordEventTypeField() {
        return stopwordeventType;
    }
    public String getstopwordlocaleField() {
        return stopwordlocaleField;
    }


    @Override
    public String toString() {
        return "DataSourceParams{" +
                "  appName=" + appName +
                ", categoryField=" + categoryField +
                ", queryField=" + queryField +
                ", localeField=" + localeField +
                ", entityType=" + entityType +
                ", eventType=" + eventType +
                ", stopwordField=" + stopwordField +
                ", stopwordentityType=" + stopwordentityType +
                ", stopwordeventType=" + stopwordeventType +
                ", stopwordlocaleField=" + stopwordlocaleField +
                '}';
    }
}
