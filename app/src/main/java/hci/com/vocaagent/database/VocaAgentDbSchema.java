package hci.com.vocaagent.database;

public class VocaAgentDbSchema {
    public static final class BookTable {
        public static final String NAME = "Book";

        public static final class Cols {
            public static final String book_id = "bid";
            public static final String name = "name";
            public static final String last_modified = "last_modified";
        }
    }

    public static final class WordTable {
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
            public static final String today = "today";
        }
    }

    public static final class DictionaryTable {
        public static final String NAME = "Dictionary";

        public static final class Cols {
            public static final String id = "id";
            public static final String word = "word";
            public static final String meaning = "meaning";
        }
    }

    public static final class MetaDataTable {
        public static final String NAME = "MetaData";

        public static final class Cols {
            public static final String date = "date";
            public static final String count = "count";
            public static final String correct = "correct";
            public static final String increment = "increment";
            public static final String streak = "streak";
        }
    }

    // This schema supports adding various meanings and sentences per word in a book.
    // (two same words cannot exist in one book)
    public static final class MeaningTable {
        public static final String NAME = "Meaning";
        public static final class Cols {
            public static final String id = "id";
            public static final String wordId = "wid";
            public static final String meaning = "meaning";
        }
    }

    // This table is not used for now
//    public static final class SentenceTable {
//        public static final String NAME = "Sentence";
//        public static final class Cols {
//            public static final String wordId = "wid";
//            public static final String sentence = "sentence";
//        }
//    }
}
