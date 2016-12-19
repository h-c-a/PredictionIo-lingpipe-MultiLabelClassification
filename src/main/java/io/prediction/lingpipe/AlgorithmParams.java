package io.prediction.lingpipe;

import org.apache.predictionio.controller.Params;

import java.util.List;

public class AlgorithmParams implements Params {
    private final int ngramsize;
    private final int maxresults;
    private final String modelbuildfilepath;
    private final List<String> locales;

    public AlgorithmParams(int maxresults,int ngramsize, String modelbuildfilepath, List<String> pcm, List<String> countries, List<String> locales) {
        this.ngramsize = ngramsize;
        this.maxresults = maxresults;
        this.modelbuildfilepath = modelbuildfilepath;
        this.locales = locales;
    }

    public int getMaxresults() { return maxresults; }

    public String getmodelbuildfilepath() { return modelbuildfilepath; }

    public int getngramsize() {
        return ngramsize;
    }

    public List<String> getmodelLocales() { return locales; }

    @Override
    public String toString() {
        return "AlgorithmParams{" +
                "  ngramsize=" + ngramsize +
                ", maxresults=" + maxresults +
                ", modelbuildfilepath=" + modelbuildfilepath +
                ", locales=" + locales +
                '}';
    }
}
