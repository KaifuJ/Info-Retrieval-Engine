package edu.uci.ics.cs221.analysis.stemmer;

import edu.uci.ics.cs221.analysis.PorterStemmer;
import edu.uci.ics.cs221.analysis.Stemmer;
import org.junit.Test;

import java.util.Arrays;

import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertEquals;

public class Team23PorterStemmerTest {

    public static String testStem(Stemmer stemmer, String sentence) {
        return Arrays.stream(sentence.split("\\s+"))
                .map(token -> stemmer.stem(token))
                .collect(joining(" "));
    }

    /**
     * This test case covers the words which should be modified (or should not be falsely modified) by step 1a, 1b,
     * 1b-cleanup, 1c of Porter Stemmer
     *
     * Covers words ending with - `sses`, `ies`, `s`, (m>0)`eed`, (*v*)`ed`, (*v*)`ing`, (*v*) y
     *
     * `agreed` becomes `agree` in step1 but changes to `agre` in step 5a of Porter Stemmer
     * `conflated` becomes `conflate` in step1 but changes to `conflat` in step 4 of Porter Stemmer
     * `troubled` becomes `trouble` in step1 but changes to `troubl` in step 5a of Porter Stemmer
     * `filing` becomes `file` in step1 but changes to `fil` in step 5a of Porter Stemmer
     */
    @Test
    public void test1() {
        String originalString1a = "caresses ponies caress cats ";
        String expectedString1a = "caress poni caress cat ";

        String originalString1b = "agreed feed plastered bled motoring sing ";
        String expectedString1b = "agre feed plaster bled motor sing ";

        String originalString1bCleanup = "conflated troubled sized hopping fizzed failing filing ";
        String expectedString1bCleanup = "conflat troubl size hop fizz fail file ";

        String originalString1c = "happy sky";
        String expectedString1c = "happi sky";

        String original = originalString1a + originalString1b + originalString1bCleanup + originalString1c;
        String expected = expectedString1a + expectedString1b + expectedString1bCleanup + expectedString1c;

        PorterStemmer porterStemmer = new PorterStemmer();
        assertEquals(expected, testStem(porterStemmer, original));
    }

    /**
     * This test case covers the words which should be modified (or should not be falsely modified) by step 2
     * of Porter Stemmer
     *
     * covers words with (m>0) `ational`, (m>0) `tional`, (m>0) `enci`, (m>0) `anci`, (m>0) `izer`, (m>0) `abli`,
     * (m>0) `alli`, (m>0) `entli`, (m>0) `eli`, (m>0) `ousli`, (m>0) `ization`, (m>0) `ation`, (m>0) `ator`,
     * (m>0) `alism`, (m>0) `iveness`, (m>0) `fulness`, (m>0) `ousness`, (m>0) `aliti`, (m>0) `iviti`, (m>0) `biliti`
     *
     * `relational` becomes `relate` in step2 but changes to `relat` in step 5a of Porter Stemmer
     * `conditional` becomes `condition` in step2 but changes to `condit` in step 4 of Porter Stemmer
     * `rational` stays `rational` in step2 but changes to `ration` in step 4 of Porter Stemmer
     * `valenci` stays `valence` in step2 but changes to `valenc` in step 5a of Porter Stemmer
     *
     * Similarly for the remaining words
     */
    @Test
    public void test2() {
        String original = "relational conditional rational valenci hesitanci digitizer conformabli radicalli " +
                "differentli vileli analogousli vietnamization predication operator feudalism decisiveness " +
                "hopefulness callousness formaliti sensitiviti sensibiliti";
        String expected = "relat condit ration valenc hesit digit conform radic differ vile analog vietnam predic " +
                "oper feudal decis hope callous formal sensit sensibl";

        PorterStemmer porterStemmer = new PorterStemmer();
        assertEquals(expected, testStem(porterStemmer, original));
    }

    /**
     * This test case covers the words which should be modified (or should not be falsely modified) by step 3
     * of Porter Stemmer
     *
     * covers words with (m>0) `icate`, (m>0) `ative`, (m>0) `alize`, (m>0) `iciti`, (m>0) `ical`, (m>0) `ful` and
     * (m>0) `ness`,
     *
     * `electriciti` and `electrical` becomes `electric` in step3 but changes to `electr` in step 4 of Porter Stemmer
     */
    @Test
    public void test3() {
        String original = "triplicate formative formalize electriciti electrical hopeful goodness";
        String expected = "triplic form formal electr electr hope good";

        PorterStemmer porterStemmer = new PorterStemmer();
        assertEquals(expected, testStem(porterStemmer, original));
    }

    /**
     * This test case covers the words which should be modified (or should not be falsely modified) by step 4
     * of Porter Stemmer
     *
     * covers words with (m>1) `al`, (m>1) `ance`, (m>1) `ence`, (m>1) `er`, (m>1) `ic`, (m>1) `able`, (m>1) `ible`,
     * (m>1) `ant`, (m>1) `ement`, (m>1) `ment`, (m>1) `ent`, (m>1 and (*S or *T)) `ion`, (m>1) `ou`, (m>1) `ism`,
     * (m>1) `ate`, (m>1) `iti`, (m>1) `ous`, (m>1) `ive`, (m>1) `ize`
     */
    @Test
    public void test4() {
        String original = "revival  allowance inference airliner gyroscopic adjustable defensible irritant replacement " +
                "adjustment dependent adoption homologou communism activate angulariti homologous effective bowdlerize";
        String expected = "reviv allow infer airlin gyroscop adjust defens irrit replac adjust depend adopt homolog " +
                "commun activ angular homolog effect bowdler";

        PorterStemmer porterStemmer = new PorterStemmer();
        assertEquals(expected, testStem(porterStemmer, original));
    }

    /**
     * This test case covers the words which should be modified (or should not be falsely modified) by step 5a, 5b
     * of Porter Stemmer
     *
     * covers words with (m>1) e, (m=1 and not *o) e, (m > 1 and *d and *L)
     */
    @Test
    public void test5() {
        String original = "probate rate cease controll roll";
        String expected = "probat rate ceas control roll";

        PorterStemmer porterStemmer = new PorterStemmer();
        assertEquals(expected, testStem(porterStemmer, original));
    }
}
