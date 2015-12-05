package hci.com.vocaagent.database;

//--Random알고리즘 적용
//        --랜덤으로 N개 뽑은 뒤 그 안에서 아래의 알고리즘 적용
//        --랜덤추출시 완료비트가 설정된 단어들은 제외
//
//        --정렬시 numCorrect가 같으면 testCount가 높은 단어가 우선순위가 높다.
//        --즉, 정답률이 낮은 단어가 우선순위가 높음
//
//        --점수 system
//        --Recent Test Date에 기반하여 phase +- 정도를 정한다.
//        --3일 이내 다시 나온 단어를 틀렸을 시 무조건 phase 0으로
//        --하루에 올라갈 수 있는 phase는 최대 2.
//
//        --phase가 10이 되면 완료비트 설정하여 우선순위를 최하위로 한다.
//        --(최소 5일은 맞춰야 완료가능)
//
//        --문장시험 10개 중 7개 이상 맞추면 페이즈 올림
//        --5~7개 면 페이즈 유지
//        --그 이하면 페이즈 낮춤(0까지만)
//
//
//        --CONVERT(char(8), GETDATE(), 112) : yyyymmdd
public class VocaAgentDbSchema {
    public static final class BookTable {
        public static final String NAME = "Book";

        public static final class Cols {
            public static final String book_id = "bid";
            public static final String name = "name";
            public static final String num_word = "num_word";
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
}
