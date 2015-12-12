package hci.com.vocaagent;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import hci.com.vocaagent.database.BookCursorWrapper;
import hci.com.vocaagent.database.VocaAgentDbSchema.BookTable;
import hci.com.vocaagent.database.VocaAgentDbSchema.DictionaryTable;
import hci.com.vocaagent.database.VocaAgentDbSchema.WordTable;
import hci.com.vocaagent.database.VocaBaseHelper;
import hci.com.vocaagent.database.WordCursorWrapper;

public class VocaLab {
    public static VocaLab sVocaLab;
    private List<Book> mExamBooks;
    private Context mContext;
    private SQLiteDatabase mDatabase;

    private VocaLab(Context context) {
        mExamBooks = new ArrayList<>();
        mContext = context;
        mDatabase = new VocaBaseHelper(mContext).getWritableDatabase();
    }

    private static ContentValues getBookContentValues(Book book) {
        ContentValues values = new ContentValues();
        // omit book_id since it is an autoincrement(identity) attribute.
        values.put(BookTable.Cols.name, book.getBookName());
        values.put(BookTable.Cols.num_word, book.getNumWords());
        values.put(BookTable.Cols.last_modified, book.getLastModified());
        return values;
    }

    private static ContentValues getWordContentValues(Word word) {
        ContentValues values = new ContentValues();
        // omit word_id since it is an autoincrement(identity) attribute.
        values.put(WordTable.Cols.word, word.getWord());
        values.put(WordTable.Cols.book_id, word.getBookid());
        values.put(WordTable.Cols.completed, (word.isCompleted() ? 1 : 0));
        values.put(WordTable.Cols.num_correct, word.getNumCorrect());
        values.put(WordTable.Cols.test_count, word.getTestCount());
        values.put(WordTable.Cols.recent_test_date, word.getRecentTestDate());
        values.put(WordTable.Cols.phase, word.getPhase());
        values.put(WordTable.Cols.today, word.getToday());
        return values;
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

    public void addBook(Book book) {
        ContentValues values = getBookContentValues(book);
        mDatabase.insert(BookTable.NAME, null, values);
    }

    public void updateBook(Book book) {
        String book_id = book.getBookId() + "";
        ContentValues values = getBookContentValues(book);

        mDatabase.update(BookTable.NAME, values, BookTable.Cols.book_id + " = ?",
                new String[]{book_id});
    }

    public void addWord(Word word) {
        ContentValues values = getWordContentValues(word);
        mDatabase.insert(WordTable.NAME, null, values);
    }

    public void updateWord(Word word) {
        String word_id = word.getWordId() + "";
        ContentValues values = getWordContentValues(word);

        mDatabase.update(WordTable.NAME, values, WordTable.Cols.word_id + " = ?",
                new String[]{word_id});
    }

    public List<Word> getTestWords() {
        WordCursorWrapper cursor = queryTestWords();
        List<Word> words = new ArrayList<>();
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                words.add(cursor.getWord());
                cursor.moveToNext();
            }
        } catch (Exception e) {
        }
        return words;
    }

    public WordCursorWrapper queryTestWords() {
        // select words from mExamBooks based on the algorithm below.
        /*
        --Random알고리즘 적용
            --랜덤으로 N개 뽑은 뒤 그 안에서 아래의 알고리즘 적용
            --랜덤추출시 완료비트가 설정된 단어들은 제외

        --정렬시 numCorrect가 같으면 testCount가 높은 단어가 우선순위가 높다.
        --즉, 정답률이 낮은 단어가 우선순위가 높음*/

        String whereClause = "WHERE (" + WordTable.Cols.completed + " <> 1) AND (";
        for (int i = 0; i < mExamBooks.size(); ++i) {
            if (i == mExamBooks.size() - 1) {
                whereClause += "bid = " + mExamBooks.get(i).getBookId() + ")";
            } else {
                whereClause += "bid = " + mExamBooks.get(i).getBookId() + " OR ";
            }
        }
        Cursor cursor = mDatabase.rawQuery("WITH samples(" + WordTable.Cols.word_id + "," + WordTable.Cols.word + "," +
                WordTable.Cols.book_id + "," + WordTable.Cols.completed + "," + WordTable.Cols.recent_test_date + "," + WordTable.Cols.test_count + "," +
                WordTable.Cols.num_correct + "," + WordTable.Cols.phase + ") AS (SELECT * FROM " + WordTable.NAME + " " +
                whereClause + " ORDER BY RANDOM() LIMIT 100)" +
                " SELECT * FROM samples ORDER BY " + WordTable.Cols.num_correct + " ASC,"
                + WordTable.Cols.test_count + " DESC LIMIT 10", null);
        return new WordCursorWrapper(cursor);
    }


    public static VocaLab getVoca(Context context) {
        if (sVocaLab == null) {
            sVocaLab = new VocaLab(context);
        }
        return sVocaLab;
    }

    public List<Book> getBooks() {
        List<Book> books = new ArrayList<>();

        BookCursorWrapper cursor = queryBooks(null, null);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                books.add(cursor.getBook());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
        return books;
    }

    public List<Word> getWordInBook(int bookId) {
        List<Word> words = new ArrayList<>();
        WordCursorWrapper cursor = queryWords(WordTable.Cols.book_id + " = ?", new String[]{bookId + ""});
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                words.add(cursor.getWord());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
        return words;
    }

    public Book getBookByID(int id) {
        BookCursorWrapper cursor = queryBooks(BookTable.Cols.book_id + " = ?",
                new String[]{id + ""});
        try {
            if (cursor.getCount() == 0) {
                return null;
            }
            cursor.moveToFirst();
            return cursor.getBook();
        } finally {
            cursor.close();
        }
    }

    public Word getWordByID(int id) {
        WordCursorWrapper cursor = queryWords(WordTable.Cols.word_id + " = ?",
                new String[]{id + ""});
        try {
            if (cursor.getCount() == 0) {
                return null;
            }
            cursor.moveToFirst();
            return cursor.getWord();
        } finally {
            cursor.close();
        }
    }

    private WordCursorWrapper queryWords(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                WordTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );
        return new WordCursorWrapper(cursor);
    }

    private BookCursorWrapper queryBooks(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                BookTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );
        return new BookCursorWrapper(cursor);
    }

    public int deleteBooks(String whereClause, String[] whereArgs) {
        return mDatabase.delete(BookTable.NAME,
                whereClause, whereArgs);
    }

    public int deleteWords(String whereCluase, String[] whereArgs) {
        return mDatabase.delete(WordTable.NAME, whereCluase, whereArgs);
    }

    public List<AutoCompleteDictionary> getAutoAvailable(String searchTerm) {
        List<AutoCompleteDictionary> autoLists = new ArrayList<>();
        String sql =
                "SELECT * FROM " + DictionaryTable.NAME
                        + " WHERE " + DictionaryTable.Cols.word + " LIKE '" + searchTerm + "%'"
                        + " ORDER BY " + DictionaryTable.Cols.word
                        + " LIMIT 0,5";

        Cursor cursor = mDatabase.rawQuery(sql, null);

        if (cursor.moveToFirst()) {
            do {
                String word = cursor.getString(cursor.getColumnIndex(DictionaryTable.Cols.word));
                String meaning = cursor.getString(cursor.getColumnIndex(DictionaryTable.Cols.meaning));
                AutoCompleteDictionary dict = new AutoCompleteDictionary(word, meaning);
                autoLists.add(dict);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return autoLists;
    }
}
