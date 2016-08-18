package hci.com.vocaagent.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hci.com.vocaagent.datastructure.RandomQueue;

public class DictionaryParser {
    private static final String searchHeader = "http://alldic.daum.net";
    private static final String optionSearch = "/search.do?q=";
    private static final String searchEnglish = "&dic=eng&search_first=Y";
    private static final String backupExampleRoute = "&t=example&dic=eng";

    // get a meaning list
    public static String getMeanings(final String word) {
        String meanings = "";
        try {
            Document doc = Jsoup.connect(searchHeader + optionSearch + word + searchEnglish).get();
            Elements liMeanings = doc.select("div[class~=(clean)]+ul>li");
            if (liMeanings.text().equals(""))
                return "Sorry, we couldn't find the meanings.";
            meanings = processWordMeaning(liMeanings);
        } catch (IOException e) {
        }
        return meanings.trim();
    }

    // get sentences along with translations of them
    public static RandomQueue getSentence(final String word) {
        RandomQueue sentences = new RandomQueue();
        String trans, examText, answer;
        boolean matched = false;
        try {
            Document d = Jsoup.connect(searchHeader + optionSearch + word + searchEnglish).get();
            Element link = d.select("div[class~=(clean)]>strong>a").first();

            String newUrl = searchHeader + link.attr("href");

            // get candidate words pattern
            d = Jsoup.connect(newUrl).get();
            Elements liVariants = d.select("ul.list_sort>li");

            // build regex pattern
            String p;
            Pattern pattern;
            Matcher m;
            p = getCandidateWordsPattern(liVariants, word);
            pattern = Pattern.compile(p, Pattern.CASE_INSENSITIVE);

            Elements liExamples = d.select("ul.list_example>li");

            for (Element e : liExamples) {
                answer = null;
                examText = e.select("span.txt_example>span").text();
                trans = e.select("span.mean_example").text();

                /*
                    get answer word from the searched sentence
                    The matched word will be replaced with blank later.)
                 */
                m = pattern.matcher(examText);
                if (m.find()) {
                    matched = true;
                    MatchResult result = m.toMatchResult();
                    answer = result.group(2);
                }
                sentences.add(examText, trans, answer);
            }

            // try backup route
            if (sentences.size() < 5 || !matched) {
                newUrl = searchHeader + optionSearch + "\""+ word + "\"" + backupExampleRoute;
                d = Jsoup.connect(newUrl).get();
                liExamples = d.select("ul.list_example>li");

                for (Element e : liExamples) {
                    answer = null;
                    examText = e.select("span.txt_example>span").text();
                    trans = e.select("span.mean_example").text();

                /*
                    get answer word from the searched sentence
                    The matched word will be replaced with blank later.)
                 */
                    m = pattern.matcher(examText);
                    if (m.find()) {
                        matched = true;
                        MatchResult result = m.toMatchResult();
                        answer = result.group(2);
                    }
                    sentences.add(examText, trans, answer);
                }
            }
        } catch (IOException ex) {
            return sentences;
        }
        return sentences;
    }

    public static String getCandidateWordsPattern(Elements li, String word) {
        PriorityQueue<String> q = new PriorityQueue<>(10, new byLength());
        String pattern = "(\\W|^)(";
        if (li.text().equals("")) {
            q.offer(word);
        } else {
            for (Element e : li) {
                q.offer(e.ownText());
            }
        }
        while(q.size() > 1) {
            pattern += q.poll()+"|";
        }
        pattern += q.poll() + ")(\\W)";
        return pattern;
    }

    private static class byLength implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            return o2.length() - o1.length();
        }
    }

    public static String processWordMeaning(Elements liMeanings) {
        String meanings = "";
        for (Element e : liMeanings) {
            meanings += e.text()+"\n";
        }
        return meanings;
    }
}
