package hci.com.vocaagent.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import hci.com.vocaagent.datastructure.RandomQueue;

public class DictionaryParser {
    private static final String meaningUrl = "http://alldic.daum.net/search.do?q=";
    private static final String searchEnglish = "&dic=eng";
    private static final String searchExamples = "&t=example";
    private static final String sentenceUrl = "http://alldic.daum.net/search.do?q=";
    private static final String searchFirstMeaning = "&search_first=Y";

    // get a meaning list
    public static String getMeanings(final String word) {
        String meanings = "";
        try {
            Document doc = Jsoup.connect(meaningUrl + word + searchEnglish + searchFirstMeaning).get();
            Element meaning = doc.select("div[class~=(clean)]>ul.list_mean").first();
            if (meaning == null)
                return "Sorry, we couldn't find meanings.";
            List<String> meaningList = processWordMeaning(meaning.text());
            for (String s : meaningList) {
                meanings += s + "\n";
            }
        } catch (IOException e) {
        }
        return meanings.trim();
    }

    // get sentences along with translations of them
    public static RandomQueue getSentence(final String word) {
        RandomQueue sentences = new RandomQueue();
        try {
            Document doc = Jsoup.connect(sentenceUrl + word + searchExamples + searchEnglish).get();
            Elements ex = doc.select("div.list_exam>div");

            for (Element e : ex) {
                Element sentence = e.select("div.txt>span.inner").first();
                Element tran = e.select("div.trans>span.inner").first();
                sentences.add(sentence.text(), tran.text());
            }
        } catch (IOException e) {
        }
        return sentences;
    }

    public static List<String> processWordMeaning(String s) {
        ArrayList<String> list = new ArrayList<>();
        int start = '①';
        int i = 0;
        int index;
        while (!s.isEmpty()) {
            index = start + (++i);
            if (s.indexOf(index) == -1) {
                list.add(s.trim());
                break;
            } else {
                list.add(s.substring(0, s.indexOf(index)).trim());
                s = s.substring(s.indexOf(index), s.length());
            }
        }
        return list;
    }
}
