package hci.com.vocaagent;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
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
import java.util.Set;

import hci.com.vocaagent.database.BookCursorWrapper;
import hci.com.vocaagent.database.MeaningCursorWrapper;
import hci.com.vocaagent.database.MetaCursorWrapper;
import hci.com.vocaagent.database.VocaAgentDbSchema.BookTable;
import hci.com.vocaagent.database.VocaAgentDbSchema.DictionaryTable;
import hci.com.vocaagent.database.VocaAgentDbSchema.MeaningTable;
import hci.com.vocaagent.database.VocaAgentDbSchema.MetaDataTable;
import hci.com.vocaagent.database.VocaAgentDbSchema.WordTable;
import hci.com.vocaagent.database.VocaBaseHelper;
import hci.com.vocaagent.database.WordCursorWrapper;

public class VocaLab {
    public static VocaLab sVocaLab;
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

    private static ContentValues getMeaningContentValues(Meaning meaning) {
        ContentValues values = new ContentValues();
        values.put(MeaningTable.Cols.wordId, meaning.getWordId());
        values.put(MeaningTable.Cols.meaning, meaning.getMeaning());
        return values;
    }

    // Methods related to Meaning table
    public void insertMeaning(Meaning meaning) {
        ContentValues values = getMeaningContentValues(meaning);
        mDatabase.insert(MeaningTable.NAME, null, values);
    }

    public void updateMeaning(Meaning meaning) {
        ContentValues values = getMeaningContentValues(meaning);
        mDatabase.update(MeaningTable.NAME, values, MeaningTable.Cols.id + " = ?",
                new String[]{meaning.getId() + ""});
    }

    public void deleteMeaning(Meaning meaning) {
        mDatabase.delete(MeaningTable.NAME, MeaningTable.Cols.id + " = ?",
                new String[]{meaning.getId() + ""});
    }

    public List<Meaning> getMeaning(int wordId) {
        MeaningCursorWrapper cursor = queryMeaning(wordId);
        List<Meaning> m = new ArrayList<>();
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                m.add(cursor.getMeaning());
                cursor.moveToNext();
            }
        } finally {
            try {
                if (cursor != null) cursor.close();
            } catch (Exception e) {
            }
        }
        return m;
    }

    public MeaningCursorWrapper queryMeaning(int wordId) {
        Cursor cursor = mDatabase.query(MeaningTable.NAME, null, MeaningTable.Cols.wordId
                + " = ?", new String[]{wordId + ""}, null, null, null);
        return new MeaningCursorWrapper(cursor);
    }


    public Book getBookByName(String bookName) {
        BookCursorWrapper cursor = new BookCursorWrapper(mDatabase.rawQuery("SELECT * FROM Book WHERE name =  '" + bookName + "'", null));
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

            for (Row myRow : sheet) {
                Iterator<Cell> it = myRow.cellIterator();
                Cell wordCell, meaningCell;
                Word w;
                if (it.hasNext()) {
                    wordCell = it.next();
                    addNewWord(wordCell.toString(), bookId);
                    w = getWordByWordAndBook(wordCell.toString(), bookId);

                    if (it.hasNext()) {
                        meaningCell = it.next();
                        String[] meanings = meaningCell.toString().split(";");
                        for (String m : meanings) {
                            Meaning newMeaning = new Meaning();
                            newMeaning.setMeaning(m);
                            newMeaning.setWordId(w.getWordId());
                            insertMeaning(newMeaning);
                        }
                    }
                }
            }
            mDatabase.setTransactionSuccessful();
            mDatabase.endTransaction();
        } catch (Exception e) {
            Log.e("TEST", "File read error", e);
        }
    }

    public int getNumberOfCompletedWordsInBook(int bookId) {
        String queryStr = "SELECT COUNT(*) FROM " + WordTable.NAME + " WHERE " + WordTable.Cols.book_id + " = " + bookId
                + " AND " + WordTable.Cols.completed + " = 1";
        Cursor cursor = mDatabase.rawQuery(queryStr, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    public int getNumberOfNotCompletedWordsInBook(int bookId) {
        String queryStr = "SELECT COUNT(*) FROM " + WordTable.NAME + " WHERE " + WordTable.Cols.book_id + " = " + bookId
                + " AND " + WordTable.Cols.completed + " = 0";
        Cursor cursor = mDatabase.rawQuery(queryStr, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    public boolean exportNote(String fileName, Set<Book> exportBooks) {
        List<Word> words = new ArrayList<>();

        // gather all the words to be exported into one long list.
        for (Book b : exportBooks) {
            for (Word w : getWordInBook(b.getBookId())) {
                words.add(w);
            }
        }

        if (Utils.isExternalStorageWritable()) {
            Workbook wb = new HSSFWorkbook();

            // new sheet
            Sheet sheet1 = null;
            sheet1 = wb.createSheet("단어목록");

            // write to xls work book (on memory)
            for (int i = 0; i < words.size(); ++i) {
                Row row = sheet1.createRow(i);

                // set word
                Cell c = row.createCell(0);
                c.setCellValue(words.get(i).getWord());

                // set meanings
                c = row.createCell(1);
                List<Meaning> listMeaning = getMeaning(words.get(i).getWordId());
                String meanings = "";
                for (Meaning m : listMeaning) {
                    meanings += m.getMeaning() + ";";
                }
                c.setCellValue(meanings);
            }

            // no words -> return false
            if (words.isEmpty()) return false;

            // write them to the SD card (/sdcard/VocaAgent/fileName.xls)
            File file = new File(Environment.getExternalStorageDirectory() + "/VocaAgent/", fileName + ".xls");
            FileOutputStream os = null;
            try {
                os = new FileOutputStream(file);
                wb.write(os);
                return true;
            } catch (IOException e) {
                Log.w("TEST", "Error writting " + file, e);
            } catch (Exception e) {
                Log.w("TEST", "Failed to save file", e);
            } finally {
                try {
                    if (os != null) os.close();
                } catch (Exception e) {
                }
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
        updateBook.setLastModified(VocaLab.getToday());
        updateBook(updateBook);
    }

    public void addNewBook(String bookTitle) {
        Book book = new Book();
        String lastModified = VocaLab.getToday();
        book.setBookName(bookTitle);
        book.setLastModified(lastModified);

        addBook(book);
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

    public ArrayList<Word> getTestWords(ArrayList<Book> examBooks) {
        WordCursorWrapper cursor = queryTestWords(examBooks);
        ArrayList<Word> words = new ArrayList<>();
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

    public WordCursorWrapper queryTestWords(ArrayList<Book> examBooks) {
        // select words from mExamBooks based on the algorithm below.
        /*
        --Random알고리즘 적용
            --랜덤으로 N개 뽑은 뒤 그 안에서 아래의 알고리즘 적용
            --랜덤추출시 완료비트가 설정된 단어들은 제외

        --정렬시 numCorrect가 같으면 testCount가 높은 단어가 우선순위가 높다.
        --즉, 정답률이 낮은 단어가 우선순위가 높음*/

        String whereClause = "WHERE (" + WordTable.Cols.completed + " <> 1)";
        for (int i = 0; i < examBooks.size(); ++i) {
            if (i == 0) {
                whereClause += " AND (";
            }
            if (i == examBooks.size() - 1) {
                whereClause += "bid = " + examBooks.get(i).getBookId() + ")";
            } else {
                whereClause += "bid = " + examBooks.get(i).getBookId() + " OR ";
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

    public Word getWordByWordAndBook(String word, int bookId) {
        WordCursorWrapper cursor = queryWords(WordTable.Cols.word + " = ? " +
                "AND " + WordTable.Cols.book_id + " = ?", new String[]{word, bookId + ""});
        try {
            if (!cursor.isAfterLast()) {
                cursor.moveToFirst();
                return cursor.getWord();
            }
        } finally {
            cursor.close();
        }
        return null;
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

    public int deleteBook(String whereClause, String[] whereArgs) {
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
