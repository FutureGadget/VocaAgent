package hci.com.vocaagent;

import android.os.Parcel;
import android.os.Parcelable;

public class Word implements Parcelable {
    private int wordId;
    private String word;
    private int bookid;
    private boolean completed;
    private String recentTestDate;
    private int testCount;
    private int numCorrect;
    private int phase;
    private int today;

    public Word () {
        super();
    }

    public Word (Parcel in) {
        wordId = in.readInt();
        word = in.readString();
        bookid = in.readInt();
        completed = (in.readInt() == 1);
        recentTestDate = in.readString();
        testCount = in.readInt();
        numCorrect = in.readInt();
        phase = in.readInt();
        today = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(wordId);
        dest.writeString(word);
        dest.writeInt(bookid);
        dest.writeInt((completed ? 1 : 0));
        dest.writeString(recentTestDate);
        dest.writeInt(testCount);
        dest.writeInt(numCorrect);
        dest.writeInt(phase);
        dest.writeInt(today);
    }

    public static final Parcelable.Creator<Word> CREATOR = new Parcelable.Creator<Word>()
    {
        public Word createFromParcel(Parcel in)
        {
            return new Word(in);
        }
        public Word[] newArray(int size)
        {
            return new Word[size];
        }
    };

    public int getToday() {
        return today;
    }

    public void setToday(int today) {
        this.today = today;
    }

    public int getBookid() {
        return bookid;
    }

    public void setBookid(int bookid) {
        this.bookid = bookid;
    }

    public int getWordId() {
        return wordId;
    }

    public void setWordId(int wordId) {
        this.wordId = wordId;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getRecentTestDate() {
        return recentTestDate;
    }

    public void setRecentTestDate(String recentTestDate) {
        this.recentTestDate = recentTestDate;
    }

    public int getTestCount() {
        return testCount;
    }

    public void setTestCount(int testCount) {
        this.testCount = testCount;
    }

    public int getNumCorrect() {
        return numCorrect;
    }

    public void setNumCorrect(int numCorrect) {
        this.numCorrect = numCorrect;
    }

    public int getPhase() {
        return phase;
    }

    public void setPhase(int phase) {
        this.phase = phase;
    }
}
