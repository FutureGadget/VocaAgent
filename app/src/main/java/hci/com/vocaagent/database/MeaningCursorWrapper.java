package hci.com.vocaagent.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import hci.com.vocaagent.Meaning;

public class MeaningCursorWrapper extends CursorWrapper{
    public MeaningCursorWrapper (Cursor cursor) {
        super(cursor);
    }

    public Meaning getMeaning() {
        Meaning m = new Meaning();

        int id = getInt(getColumnIndex(VocaAgentDbSchema.MeaningTable.Cols.id));
        int wordId = getInt(getColumnIndex(VocaAgentDbSchema.MeaningTable.Cols.wordId));
        String meaning = getString(getColumnIndex(VocaAgentDbSchema.MeaningTable.Cols.meaning));
        m.setId(id);
        m.setWordId(wordId);
        m.setMeaning(meaning);
        return m;
    }
}
