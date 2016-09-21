package io.prediction.lingpipe;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.carrot2.core.LanguageCode;
import org.carrot2.shaded.guava.common.base.Joiner;
import org.carrot2.text.linguistic.IStemmer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;


public class PreProcessingPipeline {
    private static final Logger logger = LoggerFactory.getLogger(PreProcessingPipeline.class);

    final static String Process (String query,List<String> stopwords, String isocode) {
        try {
            if (isocode.toLowerCase().equals("nb")){
                isocode = "no";
            }
            Analyzer analyzer = AnalyzerSwitcher.getAnalyzer(LanguageCode.forISOCode(isocode));
            TokenStream tokenStream = analyzer.tokenStream(LuceneConstants.CONTENTS, new StringReader(query));
            tokenStream = new LowerCaseFilter(tokenStream);
            if(stopwords != null && !stopwords.isEmpty()){
                CharArraySet stopset = StopFilter.makeStopSet(stopwords, true);
                tokenStream = new StopFilter(tokenStream, stopset);
            }
            StemmerFactory luceneCarrot2StemmerFactory = new StemmerFactory();
            IStemmer stemmer = luceneCarrot2StemmerFactory.getStemmer(LanguageCode.forISOCode(isocode));
            CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();
            List<String> stringarray = new ArrayList<>();
            List<String> stringstems = new ArrayList<>();
            while (tokenStream.incrementToken()) {
                stringarray.add(charTermAttribute.toString());
                CharSequence stem = stemmer.stem(charTermAttribute.toString());
                if (stem != null) {
                    stringstems.add(stem.toString());
                }
            }
            tokenStream.end();
            tokenStream.close();
            stringstems.forEach(stem -> replace(stem,stringarray));
            String join = Joiner.on(" ").join(stringstems);

            logger.info(String.format("Before: %s " + "After: %s.", query, join));
            if (join.length() > 0){
                return join;
            }else {
                return query;
            }
        } catch (Exception e) {
            Exceptions.log(e);
            e.printStackTrace();
        }

        return query;
    }

    private static void replace(String stem, List<String> stringarray) {
        if(stringarray.contains(stem)){
           int idx = stringarray.indexOf(stem);
           stringarray.set(idx, stem);
        }

    }
}

