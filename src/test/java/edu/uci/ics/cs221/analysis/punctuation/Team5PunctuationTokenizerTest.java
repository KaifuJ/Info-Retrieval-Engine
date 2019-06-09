package edu.uci.ics.cs221.analysis.punctuation;

import edu.uci.ics.cs221.analysis.PunctuationTokenizer;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class Team5PunctuationTokenizerTest {

    @Test
    public void test1() {
        String text = "He did not pass The Exam, did he?\n\r\t";
        List<String> expected = Arrays.asList("pass", "exam");
        PunctuationTokenizer tokenizer = new PunctuationTokenizer();

        assertEquals("test ,? split, ignoring stop words and uppercase to lowercase transfer",
                expected, tokenizer.tokenize(text));
    }

    @Test
    public void test2() {
        String text = "Thanks God! I found my wallet there.";
        List<String> expected = Arrays.asList("thanks", "god","found","wallet");
        PunctuationTokenizer tokenizer = new PunctuationTokenizer();
        assertEquals("test ! . split, ignoring stop words and uppercase to lowercase transfer",
                expected, tokenizer.tokenize(text));
    }

    @Test
    public void test3() {
        String text = "";
        List<String> expected = Arrays.asList();
        PunctuationTokenizer tokenizer = new PunctuationTokenizer();

        assertEquals("test empty input",
                expected, tokenizer.tokenize(text));
    }
    @Test
    public void test4() {
        String text = "         ";
        List<String> expected = Arrays.asList();
        PunctuationTokenizer tokenizer = new PunctuationTokenizer();
        assertEquals("test more  one spaces",
                expected, tokenizer.tokenize(text));
    }
    @Test
    public void test5() {
        String text = "    tomorrow";
        List<String> expected = Arrays.asList("tomorrow");
        PunctuationTokenizer tokenizer = new PunctuationTokenizer();
        assertEquals("test more than one spaces between words",
                expected, tokenizer.tokenize(text));
    }
    @Test
    public void test6() {
        String text = "!,";
        List<String> expected = Arrays.asList();
        PunctuationTokenizer tokenizer = new PunctuationTokenizer();
        assertEquals("test only punctuations",
                expected, tokenizer.tokenize(text));
    }

    @Test
    public void test7() {
        String text = "Dog like Cat";
        List<String> expected = Arrays.asList("dog", "like", "cat");
        PunctuationTokenizer tokenizer = new PunctuationTokenizer();

        assertEquals("test no stop words",
                expected, tokenizer.tokenize(text));
    }

    @Test
    public void test8() {
        String text =
                "herselF, me Own. ourS; tHiS? her thEirs were onLY; THese. Hidden oUrselVeS again, agaInsT hAs An? " +
                        "our have, he. oN. bEing aM CAn WiTh; So THRough? Them tHoSe. few. itS! Below! was? once Do Is! By of eACh. " +
                        "hImself; hiM; such? My; whO haViNg beEN haD She during! bEcAuse; other doEs; uNDeR oveR sHoUld JUSt! MoRe fOr Be " +
                        "into dID WHich thE, MySelf. hers; wHErE? They; now veRy aBouT NO information bUt tHemSeLVEs aRe hOw? tHeir NoT, bEFOrE? ANd wHat " +
                        "yourself; We froM? nor yOuR aboVe too wHY Or! yOurSelVeS theRE. DOn! dOwN; T. I sAme hERE uP; At. furThEr To; While; wILL; " +
                        "yours! bEtween? ThAt. you OfF theN as aLL both? uNTil; aNY Doing? tHAn iTsELf, ouT! WhEn IT whom; S, Some most A if. iN hIs! after.";

        List<String> expected = Arrays.asList("hidden", "information");
        PunctuationTokenizer tokenizer = new PunctuationTokenizer();

        assertEquals("test all stop words and punctuations: ",
                expected, tokenizer.tokenize(text));
    }

    @Test
    public void test9() {
        String text = "I don't like your! You are a fast man!";
        List<String> expected = Arrays.asList("don't", "like","fast","man");
        PunctuationTokenizer tokenizer = new PunctuationTokenizer();

        assertEquals("test not punctuations that should not be removed.",
                expected, tokenizer.tokenize(text));
    }

}
