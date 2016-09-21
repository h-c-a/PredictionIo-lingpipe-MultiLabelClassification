package io.prediction.lingpipe;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;

public class ModelConfig implements Serializable{
    private String locale;

    public ModelConfig(String locale) {
        this.locale = locale.toLowerCase();
    }

    public String getLocale() {
        return locale;
    }

    @Override
    public String toString() {
        return "ModelConfig{" +
                "locale='" + locale + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + StringUtils.trimToEmpty(locale).hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ModelConfig other = (ModelConfig) obj;
        if (!StringUtils.trimToEmpty(locale).equals(StringUtils.trimToEmpty(other.getLocale())))
            return false;
        return true;
    }

}