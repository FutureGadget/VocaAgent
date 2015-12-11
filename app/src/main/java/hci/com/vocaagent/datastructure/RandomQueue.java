package hci.com.vocaagent.datastructure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;

public class RandomQueue implements Iterable {
    private ArrayList<Node> list;
    private Random random;
    private int count;
    public RandomQueue() {
        random = new Random();
        list = new ArrayList<>();
        count = 0;
    }
    public void add(String sentence, String trans) {
        Node node = new Node(sentence, trans);
        list.add(node);
        ++count;
        Collections.swap(list, count-1, random.nextInt(count));
    }

    @Override
    public Iterator<String[]> iterator() {
        return new RandomQueueIterator();
    }

    class RandomQueueIterator implements Iterator<String[]> {
        private int cursor = 0;
        @Override
        public boolean hasNext() {
            return cursor != count ;
        }

        @Override
        public String[] next() {
            String[] item = new String[2];
            item[0] = list.get(cursor).sentence;
            item[1] = list.get(cursor).translation;
            ++cursor;
            return item;
        }

        @Override
        public void remove() {
            // Intentionally left blank
        }
    }

    class Node {
        private String sentence;
        private String translation;
        public Node(String s, String t) {
            sentence = s;
            translation = t;
        }
    }
}
