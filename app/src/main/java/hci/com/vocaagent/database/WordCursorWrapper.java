package hci.com.vocaagent.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import hci.com.vocaagent.Word;
import hci.com.vocaagent.database.VocaAgentDbSchema.*;
public class WordCursorWrapper extends CursorWrapper{
    public WordCursorWrapper(Cursor cursor) {
        super(cursor);
    }
    public Word getWord() {
        int wordId = getInt(getColumnIndex(WordTable.Cols.word_id));
        String wordString = getString(getColumnIndex(WordTable.Cols.word));
        int bookId = getInt(getColumnIndex(WordTable.Cols.book_id));
        String recentTestDate = getString(getColumnIndex(WordTable.Cols.recent_test_date));
        boolean completed = getInt(getColumnIndex(WordTable.Cols.completed)) == 1;
        int numTest = getInt(getColumnIndex(WordTable.Cols.test_count));
        int numCorrect = getInt(getColumnIndex(WordTable.Cols.num_correct));
        int phase = getInt(getColumnIndex(WordTable.Cols.phase));
        int today = getInt(getColumnIndex(WordTable.Cols.today));

        Word word = new Word();
        word.setBookid(bookId);
        word.setCompleted(completed);
        word.setNumCorrect(numCorrect);
        word.setPhase(phase);
        word.setRecentTestDate(recentTestDate);
        word.setTestCount(numTest);
        word.setWord(wordString);
        word.setWordId(wordId);
        word.setToday(today);
        return word;
    }
}
