package hci.com.vocaagent.database;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import hci.com.vocaagent.database.VocaAgentDbSchema.BookTable;
import hci.com.vocaagent.database.VocaAgentDbSchema.DictionaryTable;
import hci.com.vocaagent.database.VocaAgentDbSchema.WordTable;

public class VocaBaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "vocaBase.db";
    private Context context;

    public VocaBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createBookTable(db);
        createWordTable(db);
        createDictionaryTable(db);
        // insert initial dictionary data
        insertWordsInDictionary(db);
    }

    private void insertWordsInDictionary(SQLiteDatabase db) {
        BufferedReader br;
        try {
            br = new BufferedReader(new InputStreamReader(((Activity) context).getAssets().open("insertDictionary.txt"), "UTF-8"));
            String line = null;

            // batch insertion to increase insertion speed
            db.beginTransaction();
            while ((line = br.readLine()) != null) {
                db.execSQL(line.trim());
            }
            db.setTransactionSuccessful();
            db.endTransaction();
            br.close();
        } catch (Exception e) {
            Log.d("TEST", e.getMessage());
        }
    }

    private void createBookTable(SQLiteDatabase db) {
        String createStr = "CREATE TABLE IF NOT EXISTS " + BookTable.NAME + "("
                + BookTable.Cols.book_id + " integer primary key autoincrement, "
                + BookTable.Cols.name + ", "
                + BookTable.Cols.num_word + " integer default 0, "
                + BookTable.Cols.last_modified + " default CURRENT_DATE)";
        db.execSQL(createStr);
    }

    private void createWordTable(SQLiteDatabase db) {
        String createStr = "CREATE TABLE IF NOT EXISTS " + WordTable.NAME + "("
                + WordTable.Cols.word_id + " integer primary key autoincrement, "
                + WordTable.Cols.word + " UNIQUE, "
                + WordTable.Cols.book_id + " integer NOT NULL references Book(book_id) on delete cascade on update cascade, "
                + WordTable.Cols.completed + " NOT NULL default 0, "
                + WordTable.Cols.recent_test_date + " NOT NULL default '0000-00-00', "
                + WordTable.Cols.test_count + " integer NOT NULL default 0, "
                + WordTable.Cols.num_correct + " integer NOT NULL default 0, "
                + WordTable.Cols.phase + " integer NOT NULL default 0)";
        db.execSQL(createStr);
    }

    private void createDictionaryTable(SQLiteDatabase db) {
        String createStr = "CREATE TABLE IF NOT EXISTS " + DictionaryTable.NAME + "("
                + DictionaryTable.Cols.id + " integer primary key autoincrement, "
                + DictionaryTable.Cols.word + " NOT NULL, "
                + DictionaryTable.Cols.meaning + ")";
        db.execSQL(createStr);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
