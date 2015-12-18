package hci.com.vocaagent;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import hci.com.vocaagent.database.BookCursorWrapper;
import hci.com.vocaagent.database.MetaCursorWrapper;
import hci.com.vocaagent.database.VocaAgentDbSchema.BookTable;
import hci.com.vocaagent.database.VocaAgentDbSchema.DictionaryTable;
import hci.com.vocaagent.database.VocaAgentDbSchema.MetaDataTable;
import hci.com.vocaagent.database.VocaAgentDbSchema.WordTable;
import hci.com.vocaagent.database.VocaBaseHelper;
import hci.com.vocaagent.database.WordCursorWrapper;

public class VocaLab {
    public static VocaLab sVocaLab;
    private List<Book> mExamBooks; // to select exam books
    private List<ResultWord> mResultWords; // for saving tested words
    private List<Word> mReviewWords;
    private Context mContext;
    private SQLiteDatabase mDatabase;

    public static VocaLab getVoca(Context context) {
        if (sVocaLab == null) {
            sVocaLab = new VocaLab(context);
        }
        return sVocaLab;
    }

    private VocaLab(Context context) {
        mExamBooks = new ArrayList<>();
        mResultWords = new ArrayList<>();
        mReviewWords = new ArrayList<>();
        mContext = context;
        mDatabase = new VocaBaseHelper(mContext).getWritableDatabase();
    }

    // words for review
    public void initReviewWords() {
        mReviewWords = new ArrayList<>();
    }

    public void addReviewWords(Word w) {
        mReviewWords.add(w);
    }

    public List<Word> getReviewWords() {
        return mReviewWords;
    }

    public List<Book> getExamBooks() {
        return mExamBooks;
    }

    // save tested words for statistics when the exam ends.
    public void initResultWords() {
        mResultWords = new ArrayList<>();
    }

    public void addResultWord(Word w, int increment) {
        ResultWord rw = new ResultWord();
        rw.setPhaseIncrement(increment);
        rw.setResultWord(w);
        mResultWords.add(rw);
    }

    public List<ResultWord> getResultWords() {
        return mResultWords;
    }

    /**
     * @return "Today" in "yyyy-MM-dd" format.
     */
    public static String getToday() {
        Date date = Calendar.getInstance().getTime();
        return DateFormat.format("yyyy-MM-dd", date).toString();
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

    private static ContentValues getMetaDataCountentValues(Meta meta) {
        ContentValues values = new ContentValues();
        values.put(MetaDataTable.Cols.date, meta.getDate());
        values.put(MetaDataTable.Cols.count, meta.getCount());
        values.put(MetaDataTable.Cols.correct, meta.getCorrect());
        values.put(MetaDataTable.Cols.increment, meta.getIncrement());
        values.put(MetaDataTable.Cols.streak, meta.getStreak());
        return values;
    }

    public Book getBookByName(String bookName) {
        BookCursorWrapper cursor = new BookCursorWrapper(mDatabase.rawQuery("SELECT * FROM Book WHERE name =  '"+ bookName +"'", null));
        cursor.moveToFirst();
        try {
            if (!cursor.isAfterLast())
                return cursor.getBook();
        } finally {
            cursor.close();
        }
        return null;
    }

    public void importNote(String fileName, String bookName) {
        File f = new File(Environment.getExternalStorageDirectory() + "/VocaAgent/", fileName);
        try {
            FileInputStream input = new FileInputStream(f);

            POIFSFileSystem poifsFileSystem = new POIFSFileSystem(input);

            HSSFWorkbook wb = new HSSFWorkbook(poifsFileSystem);

            Sheet sheet = wb.getSheetAt(0);

            Book book = getBookByName(bookName);

            // transaction
            mDatabase.beginTransaction();
            if (book == null) { // no such book with the name provided
                addNewBook(bookName);
                book = getBookByName(bookName);
            }

            int bookId = book.getBookId();

            Log.d("TEST", "BOOKID: "+bookId);
            for (Row myRow : sheet) {
                for (Cell myCell : myRow) {
                    addNewWord(myCell.toString(), bookId);
                    Log.d("TEST", myCell.toString() + " CELL");
                }
            }
            mDatabase.setTransactionSuccessful();
            mDatabase.endTransaction();
        } catch (Exception e) {
            Log.e("TEST", "File read error", e);
        }
    }

    public boolean exportNote(String fileName, int bookId) {
        if (Utils.isExternalStorageWritable()) {
            Workbook wb = new HSSFWorkbook();

            // new sheet
            Sheet sheet1 = null;
            sheet1 = wb.createSheet("단어목록");

            CellStyle cs = wb.createCellStyle();
            cs.setFillForegroundColor(HSSFColor.LIME.index);
            cs.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

            // get words to export and write them to the workbook
            List<Word> words = getWordInBook(bookId);
            for (int i = 0; i < words.size(); ++i) {
                Row row = sheet1.createRow(i);
                Cell c = row.createCell(0);
                c.setCellValue(words.get(i).getWord());
                if ((i & 1) == 0) {
                    c.setCellStyle(cs);
                }
            }
            File file = new File(Environment.getExternalStorageDirectory() + "/VocaAgent/", fileName + ".xls");
            FileOutputStream os = null;
            try {
                os = new FileOutputStream(file);
                wb.write(os);

            } catch (IOException e) {
                Log.w("TEST", "Error writting "+file, e);
            } catch (Exception e) {
                Log.w("TEST", "Failed to save file", e);
            } finally {
                try {
                    if (os != null) os.close();
                } catch(Exception e){}
            }
        }
        return false;
    }

    public void addNewWord(String wordString, int bookId) {
        Word newWord = new Word();
        newWord.setTestCount(0);
        newWord.setWord(wordString);
        newWord.setRecentTestDate("0000-00-00");
        newWord.setPhase(0);
        newWord.setBookid(bookId);

        newWord.setCompleted(false);
        newWord.setNumCorrect(0);

        addWord(newWord);

        // update book (update number of contained words and last modified date)
        Book updateBook = getBookByID(bookId);
        updateBook.setNumWords(updateBook.getNumWords() + 1);
        updateBook.setLastModified(VocaLab.getToday());
        updateBook(updateBook);
    }

    public void addNewBook(String bookTitle) {
        Book book = new Book();
        String lastModified = VocaLab.getToday();
        book.setBookName(bookTitle);
        book.setLastModified(lastModified);
        book.setNumWords(0);

        addBook(book);
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

    public List<String> getRandomWords() {
        List<String> randomWords = new ArrayList<>();
        Cursor cursor = mDatabase.rawQuery("SELECT " +
                DictionaryTable.Cols.word
                + " FROM " + DictionaryTable.NAME
                + " ORDER BY RANDOM()"
                + " LIMIT 3", null);
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                randomWords.add(cursor.getString(cursor.getColumnIndex(DictionaryTable.Cols.word)));
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
        return randomWords;
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
        } finally {
            cursor.close();
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

        String whereClause = "WHERE (" + WordTable.Cols.completed + " <> 1)";
        for (int i = 0; i < mExamBooks.size(); ++i) {
            if (i == 0) {
                whereClause += " AND (";
            }
            if (i == mExamBooks.size() - 1) {
                whereClause += "bid = " + mExamBooks.get(i).getBookId() + ")";
            } else {
                whereClause += "bid = " + mExamBooks.get(i).getBookId() + " OR ";
            }
        }

        String getTestWordStr = "SELECT * FROM (SELECT * FROM " + WordTable.NAME + " " + whereClause +
                " ORDER BY RANDOM() LIMIT 100) as t ORDER BY " + WordTable.Cols.num_correct + " ASC," + WordTable.Cols.test_count + " DESC LIMIT 10";
//        Cursor cursor = mDatabase.rawQuery("WITH samples(" + WordTable.Cols.word_id + "," + WordTable.Cols.word + "," +
//                WordTable.Cols.book_id + "," + WordTable.Cols.completed + "," + WordTable.Cols.recent_test_date + "," + WordTable.Cols.test_count + "," +
//                WordTable.Cols.num_correct + "," + WordTable.Cols.phase + "," + WordTable.Cols.today + ") AS (SELECT * FROM " + WordTable.NAME + " " +
//                whereClause + " ORDER BY RANDOM() LIMIT 100)" +
//                " SELECT * FROM samples ORDER BY " + WordTable.Cols.num_correct + " ASC,"
//                + WordTable.Cols.test_count + " DESC LIMIT 10", null);
        return new WordCursorWrapper(mDatabase.rawQuery(getTestWordStr, null));
    }

    // return correct ratio (num_correct / count_test)
    public double getCorrectRatio() {
        WordCursorWrapper wrapper = queryWords(null, null);
        double numCorrect = 0, totalTestCount = 0;
        if (wrapper.getCount() == 0)
            return 0;
        try {
            wrapper.moveToFirst();
            while (!wrapper.isAfterLast()) {
                numCorrect += wrapper.getWord().getNumCorrect();
                totalTestCount += wrapper.getWord().getTestCount();
                wrapper.moveToNext();
            }
        } finally {
            wrapper.close();
        }
        Log.d("TEST", numCorrect + ": numCorrect, " + totalTestCount + ": TotalTestCount");
        return numCorrect / totalTestCount;
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

    // call this method every time when the user takes a test
    public void updateMetaInfo(int correct, int count, int increment) {
        Meta latestMeta = getLatestMeta();
        boolean notLatest = true;
        int streak = 0;
        if (latestMeta == null) {
            insertMetaData(getToday());
        } else if (Utils.getDateDiff(latestMeta.getDate()) != 0) {
            if (Utils.getDateDiff(latestMeta.getDate()) == 1) {
                streak = latestMeta.getStreak() + 1;
            }
            insertMetaData(getToday());
        } else {
            notLatest = false;
            streak = latestMeta.getStreak();
        }

        if (notLatest)
            latestMeta = getLatestMeta();

        latestMeta.setIncrement(latestMeta.getIncrement() + increment);
        latestMeta.setCount(latestMeta.getCount() + count);
        latestMeta.setCorrect(latestMeta.getCorrect() + correct);
        latestMeta.setStreak(streak);

        ContentValues updateValue = getMetaDataCountentValues(latestMeta);
        mDatabase.update(MetaDataTable.NAME, updateValue, MetaDataTable.Cols.date + " = ?", new String[]{latestMeta.getDate()});
    }

    private void insertMetaData(String date) {
        Meta meta = new Meta();
        meta.setCorrect(0);
        meta.setCount(0);
        meta.setIncrement(0);
        meta.setStreak(0);
        meta.setDate(date);

        ContentValues value = getMetaDataCountentValues(meta);
        mDatabase.insert(MetaDataTable.NAME, null, value);
    }

    public Meta getLatestMeta() {
        Meta meta;
        Cursor cursor = mDatabase.query(
                MetaDataTable.NAME,
                null,
                null,
                null,
                null,
                null,
                MetaDataTable.Cols.date + " DESC",
                "1"
        );
        MetaCursorWrapper wrapper = new MetaCursorWrapper(cursor);
        try {
            if (wrapper.getCount() == 0)
                return null;
            wrapper.moveToFirst();
            meta = wrapper.getMeta();
            return meta;
        } finally {
            wrapper.close();
        }
    }

    public List<Word> getCompletedWords() {
        List<Word> completed = new ArrayList<>();
        String queryStr = "SELECT * FROM " + WordTable.NAME + " WHERE "
                + WordTable.Cols.completed + " = 1 ORDER BY " + WordTable.Cols.num_correct
                + " ASC," + WordTable.Cols.test_count + " DESC";
        WordCursorWrapper cursor = new WordCursorWrapper(mDatabase.rawQuery(queryStr, null));
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                completed.add(cursor.getWord());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
        return completed;
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
        try {
            if (cursor.moveToFirst()) {
                do {
                    String word = cursor.getString(cursor.getColumnIndex(DictionaryTable.Cols.word));
                    String meaning = cursor.getString(cursor.getColumnIndex(DictionaryTable.Cols.meaning));
                    AutoCompleteDictionary dict = new AutoCompleteDictionary(word, meaning);
                    autoLists.add(dict);
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
        return autoLists;
    }
}
