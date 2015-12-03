package hci.com.vocaagent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dw on 2015-11-21.
 */
public class Book {
    private int bookId;
    private String bookName;
    private List<Word> words;

    public Book() {
        words = new ArrayList<>();
    }

    public void addWord(Word word) {
        words.add(word);
    }

    public List<Word> getWords() {
        return words;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    private String lastModified;
}
