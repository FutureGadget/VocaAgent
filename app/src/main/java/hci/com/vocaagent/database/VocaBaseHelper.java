package hci.com.vocaagent.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.BufferedReader;
import java.io.FileReader;

import hci.com.vocaagent.database.VocaAgentDbSchema.BookTable;
import hci.com.vocaagent.database.VocaAgentDbSchema.DictionaryTable;
import hci.com.vocaagent.database.VocaAgentDbSchema.WordTable;

public class VocaBaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "vocaBase.db";

    public VocaBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createBookTable(db);
        createWordTable(db);
        createDictionaryTable(db);
        insertWordsInDictionary(db);
    }

    private void insertWordsInDictionary(SQLiteDatabase db) {
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader("insertDictionary.txt"));
            String line;
            while((line=br.readLine())!=null)
                db.execSQL(line);
            br.close();
        } catch (Exception e) {
            System.out.println("FileNotFoundException" + e.getMessage());
        }
    }

    private void createBookTable(SQLiteDatabase db) {
        String createStr = "CREATE TABLE IF NOT EXISTS " + BookTable.NAME + "("
                + BookTable.Cols.book_id + " integer primary key autoincrement, "
                + BookTable.Cols.name + ", "
                + BookTable.Cols.num_word + " default 0, "
                + BookTable.Cols.last_modified + " default CURRENT_DATE)";
        db.execSQL(createStr);
    }

    private void createWordTable(SQLiteDatabase db) {
        String createStr = "CREATE TABLE IF NOT EXISTS " + WordTable.NAME + "("
                + WordTable.Cols.word_id + " integer primary key autoincrement, "
                + WordTable.Cols.word + " UNIQUE, "
                + WordTable.Cols.book_id + " NOT NULL references Book(book_id) on delete cascade on update cascade, "
                + WordTable.Cols.completed + " NOT NULL default 0, "
                + WordTable.Cols.recent_test_date + " NOT NULL default '0000-00-00', "
                + WordTable.Cols.test_count + " NOT NULL default 0, "
                + WordTable.Cols.num_correct + " NOT NULL default 0, "
                + WordTable.Cols.phase + " NOT NULL default 0)";
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
