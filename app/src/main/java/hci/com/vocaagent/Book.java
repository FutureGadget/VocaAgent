package hci.com.vocaagent;

public class Book {
    private int bookId;
    private String bookName;
    private String lastModified;
    private int numWords;

    public int getNumWords() {
        return numWords;
    }

    public void setNumWords(int numWords) {
        this.numWords = numWords;
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
}
