package hci.com.vocaagent;

import android.text.format.DateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by dw on 2015-11-20.
 */
public class VocaLab {
    public static VocaLab sVocaLab;
    private List<Book> mBooks;
    private List<Book> mExamBooks;

    private VocaLab() {
        mBooks = new ArrayList<>();
        mExamBooks = new ArrayList<>();
        // Test code
        for (int i = 0; i < 10; ++i) {
            Book book = new Book();
            book.setBookId(i);
            book.setBookName(i + "" + "Book");
            book.setLastModified(DateFormat.format("EEEE, MMM, d, yyyy", new Date()).toString());
            List<Word> words = book.getWords();
            for (int j = 0; j < 5; ++j) {
                Word word = new Word();
                word.setCompleted(false);
                word.setNumCorrect(0);
                word.setPhase(j);
                word.setRecentTestDate(DateFormat.format("EEEE, MMM, d, yyyy", new Date()).toString());
                word.setTestCount(0);
                word.setBookid(i);
                word.setWord("Test word #" + j);
                word.setWordId(10*i+j);
                words.add(word);
            }
            mBooks.add(book);
        }
        // EOT
    }

    public void addExamBook(Book book) {
        mExamBooks.add(book);
    }

    public void removeExamBook(Book book) {
        mExamBooks.remove(book);
    }

    public void resetExamBooks() {
        mExamBooks = new ArrayList<>();
    }

    /*
     * Get 4 sample words to show them in choices.
     */
    public Word[] getSamples() {
        Word[] samples = new Word[4];
        return samples;
    }

    public List<Word> getTestWords() {
        // select words from mExamBooks based on the algorithm below.
        /*
        --Random알고리즘 적용
            --랜덤으로 N개 뽑은 뒤 그 안에서 아래의 알고리즘 적용
            --랜덤추출시 완료비트가 설정된 단어들은 제외

        --정렬시 numCorrect가 같으면 testCount가 높은 단어가 우선순위가 높다.
        --즉, 정답률이 낮은 단어가 우선순위가 높음

        --점수 system
            --Recent Test Date에 기반하여 phase +- 정도를 정한다.
            --3일 이내 다시 나온 단어를 틀렸을 시 무조건 phase 0으로
            --하루에 올라갈 수 있는 phase는 최대 2.

            --phase가 10이 되면 완료비트 설정하여 우선순위를 최하위로 한다.
            --(최소 5일은 맞춰야 완료가능)
         */

        // JUST FOR TEST
        List<Word> list = new ArrayList<>();
        for (Book b : mExamBooks) {
            for (Word w : b.getWords()) {
                list.add(w);
            }
        }
        return list;
    }


    public static VocaLab getVoca() {
        if (sVocaLab == null) {
            sVocaLab = new VocaLab();
        }
        return sVocaLab;
    }

    public List<Book> getBooks() {
        return mBooks;
    }

    public Book getBookByID(int id) {
        // Must be changed to SQL
        for (Book b : mBooks) {
            if (b.getBookId() == id) {
                return b;
            }
        }
        return null;
    }

    public Word getWordByID(int id) {
        // Must be changed to SQL
        for (Book b : mBooks) {
            for (Word w : b.getWords()) {
                if (w.getWordId() == id) {
                    return w;
                }
            }
        }
        return null;
    }
}
