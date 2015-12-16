package hci.com.vocaagent.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import hci.com.vocaagent.Meta;

import hci.com.vocaagent.database.VocaAgentDbSchema.*;

public class MetaCursorWrapper extends CursorWrapper {
    public MetaCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Meta getMeta() {
        String date = getString(getColumnIndex(MetaDataTable.Cols.date));
        int count = getInt(getColumnIndex(MetaDataTable.Cols.count));
        int correct = getInt(getColumnIndex(MetaDataTable.Cols.correct));
        int increment = getInt(getColumnIndex(MetaDataTable.Cols.increment));
        int streak = getInt(getColumnIndex(MetaDataTable.Cols.streak));

        Meta meta = new Meta();
        meta.setCorrect(correct);
        meta.setDate(date);
        meta.setIncrement(increment);
        meta.setStreak(streak);
        meta.setCount(count);
        return meta;
    }
}
