package hci.com.vocaagent;

import android.os.Parcel;
import android.os.Parcelable;

public class Book implements Parcelable{
    private int bookId;
    private String bookName;
    private String lastModified;

    public Book() {
        super();
    }
    public Book (Parcel in) {
        bookId = in.readInt();
        bookName = in.readString();
        lastModified = in.readString();
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(bookId);
        dest.writeString(bookName);
        dest.writeString(lastModified);
    }

    public static final Parcelable.Creator<Book> CREATOR = new Parcelable.Creator<Book>()
    {
        public Book createFromParcel(Parcel in)
        {
            return new Book(in);
        }
        public Book[] newArray(int size)
        {
            return new Book[size];
        }
    };
}
