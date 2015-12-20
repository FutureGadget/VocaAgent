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

    // get a meaning list
    public static String getMeanings(final String word) {
        String meanings = "";
        try {
            Document doc = Jsoup.connect(searchHeader + optionSearch + word + searchEnglish).get();
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
        String trans, example, answer;
        try {
            Document d = Jsoup.connect(searchHeader + optionSearch + word + searchEnglish).get();
            Elements e = d.select("div.clean_word>strong>a");

            String newUrl = searchHeader + e.attr("href");

            // get candidate words pattern
            d = Jsoup.connect(newUrl).get();
            e = d.select("div#variant_div");

            // build regex pattern
            String p;
            Pattern pattern;
            Matcher m;
            p = getCandidateWordsPattern(e.text(), word);
            pattern = Pattern.compile(p, Pattern.CASE_INSENSITIVE);

            newUrl = newUrl.replace("http://alldic.daum.net/word/view.do?", "http://alldic.daum.net/word/view_example.do?");
            d = Jsoup.connect(newUrl).get();
            e = d.select("div.list_exam");

            for (Element element : e) {
                Elements listExams = element.select("div.desc");
                for (Element text : listExams) {
                    answer = null;
                    Element sentence = text.select("div.txt>span.inner").first();
                    Element translation = text.select("div.trans>span.inner").first();
                    example = sentence.text();
                    trans = translation.text();
                    // replace matched
                    m = pattern.matcher(example);
                    if (m.find()) {
                        MatchResult result = m.toMatchResult();
                        answer = result.group(2);
                    }
                    sentences.add(example, trans, answer);
                }
            }
        } catch (IOException ex) {
            return sentences;
        }
        return sentences;
    }

    public static String getCandidateWordsPattern(String line, String word) {
        boolean skip = false;
        PriorityQueue<String> queue = new PriorityQueue<>(10, new byLength());
        line = line.replaceAll("[\u00a0|,]", " "); // remove &nbsp; , [, (comma)]
        if (line == null || !line.contains("기본형")) {
            queue.offer(word);
            if (line == null)
                skip = true;
        }

        String pattern = "(\\W)(";

        if (!skip) {
            Pattern p = Pattern.compile("[A-Za-z]+( [A-Za-z]+)*");
            Matcher m = p.matcher(line);

            while (m.find()) {
                queue.offer(m.group());
            }
        }

        int size = queue.size();
        while (size-- > 0) {
            pattern += queue.poll();
            if (size > 0) {
                pattern += "|";
            }
        }
        pattern += ")(\\W)";
        return pattern;
    }

    private static class byLength implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            return o2.length() - o1.length();
        }
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
