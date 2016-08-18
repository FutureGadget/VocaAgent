package hci.com.vocaagent.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import hci.com.vocaagent.Book;
import hci.com.vocaagent.database.VocaAgentDbSchema.BookTable;

public class BookCursorWrapper extends CursorWrapper {
    public BookCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Book getBook() {
        int bookId = getInt(getColumnIndex(BookTable.Cols.book_id));
        String name = getString(getColumnIndex(BookTable.Cols.name));
        String lastModified = getString(getColumnIndex(BookTable.Cols.last_modified));

        Book book = new Book();
        book.setBookId(bookId);
        book.setBookName(name);
        book.setLastModified(lastModified);
        return book;
    }
}
