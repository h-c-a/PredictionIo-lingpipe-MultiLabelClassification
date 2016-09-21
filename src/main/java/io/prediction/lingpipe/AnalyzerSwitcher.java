package io.prediction.lingpipe;

import org.apache.lucene.analysis.ar.ArabicAnalyzer;
import org.apache.lucene.analysis.da.DanishAnalyzer;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.analysis.fi.FinnishAnalyzer;
import org.apache.lucene.analysis.fr.FrenchAnalyzer;
import org.apache.lucene.analysis.hu.HungarianAnalyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.analysis.no.NorwegianAnalyzer;
import org.apache.lucene.analysis.pt.PortugueseAnalyzer;
import org.apache.lucene.analysis.ro.RomanianAnalyzer;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.analysis.sv.SwedishAnalyzer;
import org.apache.lucene.analysis.tr.TurkishAnalyzer;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.carrot2.core.LanguageCode;
import org.slf4j.Logger;

import java.util.HashMap;

public class AnalyzerSwitcher {
    final static Logger logger = org.slf4j.LoggerFactory
            .getLogger(AnalyzerSwitcher.class);

    final static StopwordAnalyzerBase getAnalyzer(LanguageCode language) {
        switch (language) {
            case ARABIC:
                return new ArabicAnalyzer();

            default:

                return SnowballStemmerFactory.createAnalyzer(language);
        }
    }

    private final static class SnowballStemmerFactory {

        private static HashMap<LanguageCode, Class<? extends StopwordAnalyzerBase>> snowballStemmerClasses;
        static {
            snowballStemmerClasses = new HashMap<LanguageCode, Class<? extends StopwordAnalyzerBase>>();
            snowballStemmerClasses.put(LanguageCode.DANISH, DanishAnalyzer.class);
            snowballStemmerClasses.put(LanguageCode.ENGLISH, EnglishAnalyzer.class);
            snowballStemmerClasses.put(LanguageCode.FINNISH, FinnishAnalyzer.class);
            snowballStemmerClasses.put(LanguageCode.FRENCH, FrenchAnalyzer.class);
            snowballStemmerClasses.put(LanguageCode.GERMAN, GermanAnalyzer.class);
            snowballStemmerClasses
                    .put(LanguageCode.HUNGARIAN, HungarianAnalyzer.class);
            snowballStemmerClasses.put(LanguageCode.ITALIAN, ItalianAnalyzer.class);
            snowballStemmerClasses
                    .put(LanguageCode.NORWEGIAN, NorwegianAnalyzer.class);
            snowballStemmerClasses.put(LanguageCode.PORTUGUESE,
                    PortugueseAnalyzer.class);
            snowballStemmerClasses.put(LanguageCode.ROMANIAN, RomanianAnalyzer.class);
            snowballStemmerClasses.put(LanguageCode.RUSSIAN, RussianAnalyzer.class);
            snowballStemmerClasses.put(LanguageCode.SPANISH, SpanishAnalyzer.class);
            snowballStemmerClasses.put(LanguageCode.SWEDISH, SwedishAnalyzer.class);
            snowballStemmerClasses.put(LanguageCode.TURKISH, TurkishAnalyzer.class);
        }


        public static StopwordAnalyzerBase createAnalyzer(LanguageCode language) {
            final Class<? extends StopwordAnalyzerBase> stemmerClazz = snowballStemmerClasses
                    .get(language);

            if (stemmerClazz == null) {
                logger.warn("No Snowball stemmer class for: " + language.name()
                        + ". Quality of clustering may be degraded.");
                return new EnglishAnalyzer();
            }

            try {
                return stemmerClazz.newInstance();
            } catch (Exception e) {
                logger.warn("Could not instantiate snowball stemmer"
                        + " for language: " + language.name()
                        + ". Quality of clustering may be degraded.", e);

                return new EnglishAnalyzer();
            }
        }
    }
}
