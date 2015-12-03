package hci.com.vocaagent.database;

/**
 * Created by dw on 2015-11-20.
 */
public class VocaAgentDbSchema {
    public static final class Book {
        public static final String NAME = "Book";
        public static final class Cols {
            public static final String book_id = "bid";
            public static final String name = "name";
            public static final String num_word = "num_word";
            public static final String last_modified = "last_modified";
        }
    }
    public static final class Word {
        public static final String NAME = "Word";
        public static final class Cols {
            public static final String word_id = "wid";
            public static final String word = "word";
            public static final String book_id = "bid";
            public static final String completed = "completed";
            public static final String recent_test_date = "recent_test_date";
            public static final String test_count = "test_count";
            public static final String num_correct = "num_correct";
            public static final String phase = "phase";
        }
    }
}
