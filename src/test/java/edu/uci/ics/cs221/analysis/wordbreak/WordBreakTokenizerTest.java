package edu.uci.ics.cs221.analysis.wordbreak;

import edu.uci.ics.cs221.analysis.WordBreakTokenizer;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class WordBreakTokenizerTest {

    @Test
    public void test1() {
        String text = "catdog";
        List<String> expected = Arrays.asList("cat", "dog");
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();

        assertEquals(expected, tokenizer.tokenize(text));

    }

    // test a long text, with 20 seconds timeout
    @Test(timeout=20000)
    public void longTest1() {
        String text = "tosherlockholmessheisalwaysthewomanihaveseldomheardhimmentionherunderanyothernameinhiseyessheeclipsesandpredominatesthewholeofhersexitwasnotthathefeltanyemotionakintoloveforireneadlerallemotionsandthatoneparticularlywereabhorrenttohiscoldprecisebutadmirablybalancedmindhewasitakeitthemostperfectreasoningandobservingmachinethattheworldhasseenbutasaloverhewouldhaveplacedhimselfinafalsepositionheneverspokeofthesofterpassionssavewithagibeandasneertheywereadmirablethingsfortheobserverexcellentfordrawingtheveilfrommenmotivesandactionsbutforthetrainedreasonertoadmitsuchintrusionsintohisowndelicateandfinelyadjustedtemperamentwastointroduceadistractingfactorwhichmightthrowadoubtuponallhismentalresultsgritinasensitiveinstrumentoracrackinoneofhisownhighpowerlenseswouldnotbemoredisturbingthanastrongemotioninanaturesuchashisandyettherewasbutonewomantohimandthatwomanwasthelateireneadlerofdubiousandquestionablememory";
        String expectedStr = "sherlock holmes always woman seldom heard mention name eyes eclipses predominates whole sex felt emotion akin love irene adler emotions one particularly abhorrent cold precise admirably balanced mind take perfect reasoning observing machine world seen lover would placed false position never spoke softer passions save gibe sneer admirable things observer excellent drawing veil men motives actions trained reasoner admit intrusions delicate finely adjusted temperament introduce distracting factor might throw doubt upon mental results grit sensitive instrument crack one high power lenses would disturbing strong emotion nature yet one woman woman late irene adler dubious questionable memory";
        List<String> expected = Arrays.asList(expectedStr.split(" "));

        WordBreakTokenizer tokenizer = new WordBreakTokenizer();
        assertEquals(expected, tokenizer.tokenize(text));

    }

    // test a long text, with 20 seconds timeout
    @Test(timeout=20000)
    public void longTest2() {
        String text = "ihadseenlittleofholmeslatelymymarriagehaddriftedusawayfromeachothermyowncompletehappinessandthehomecentredinterestswhichriseuparoundthemanwhofirstfindshimselfmasterofhisownestablishmentweresufficienttoabsorballmyattentionwhileholmeswholoathedeveryformofsocietywithhiswholesoulremainedinourlodgingsinbakerstreetburiedamonghisoldbooksandalternatingfromweektoweekbetweencocaineandambitionthedrowsinessofthedrugandthefierceenergyofhisownkeennaturehewasstillaseverdeeplyattractedbythestudyofcrimeandoccupiedhisimmensefacultiesandextraordinarypowersofobservationinfollowingoutthosecluesandclearingupthosemysterieswhichhadbeenabandonedashopelessbytheofficialpolicefromtimetotimeiheardsomevagueaccountofhisdoingsofhissummonstoodessainthecaseofthemurderofhisclearingupofthesingulartragedyoftheatkinsonbrothersattrincomaleeandfinallyofthemissionwhichhehadaccomplishedsodelicatelyandsuccessfullyforthereigningfamilyofhollandbeyondthesesignsofhisactivityhoweverwhichimerelysharedwithallthereadersofthedailypressiknewlittleofmyformerfriendandcompanion";
        String expectedStr = "seen little holmes lately marriage drifted us away complete happiness home centred interests rise around man first finds master establishment sufficient absorb attention holmes loathed every form society whole soul remained lodgings baker street buried among old books alternating week week cocaine ambition drowsiness drug fierce energy keen nature still ever deeply attracted study crime occupied immense faculties extraordinary powers observation following clues clearing mysteries abandoned hopeless official police time time heard vague account doings summons odessa case murder clearing singular tragedy atkinson brothers trincomalee finally mission accomplished delicately successfully reigning family holland beyond signs activity however merely shared readers daily press knew little former friend companion";
        List<String> expected = Arrays.asList(expectedStr.split(" "));

        WordBreakTokenizer tokenizer = new WordBreakTokenizer();
        assertEquals(expected, tokenizer.tokenize(text));

    }


}