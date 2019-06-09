package edu.uci.ics.cs221.analysis;

import java.util.List;

/**
 * Analyzer is a general concept of analyzing a string of text into a list of tokens,
 *  it could include the process of tokenization and stemming.
 *
 */
public interface Analyzer {

    List<String> analyze(String text);

}
