package edu.uci.ics.cs221.analysis;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toList;

public class ComposableAnalyzer implements Analyzer {

    private final Tokenizer tokenizer;
    private final Stemmer stemmer;

    public ComposableAnalyzer(Tokenizer tokenizer, Stemmer stemmer) {
        checkNotNull(tokenizer);
        checkNotNull(stemmer);
        this.tokenizer = tokenizer;
        this.stemmer = stemmer;
    }

    @Override
    public List<String> analyze(String text) {
        return tokenizer.tokenize(text).stream().map(token -> stemmer.stem(token)).collect(toList());
    }

}
