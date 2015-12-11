package hci.com.vocaagent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import hci.com.vocaagent.database.VocaAgentDbSchema.*;

public class NoteManagerFragment extends Fragment {
    private RecyclerView mBookRecyclerView;
    private NoteAdapter mAdapter;
    private boolean[] mSavedViewHolderStatus;
    private static final int REQUEST_TITLE = 0;
    private Set<Book> mBooksSelected;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_note_manager, container, false);
        mBookRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        mBookRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        updateUI();
        return v;
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_notemanager_menu, menu);
    }

    /*
     * Behaviors when one of the overflow menu options on Toolbar is selected.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_item_add_book:
                AddBookFragment dialogFragment = new AddBookFragment();
                dialogFragment.show(getFragmentManager(),"add_book");
                dialogFragment.setTargetFragment(NoteManagerFragment.this, REQUEST_TITLE);
                return true;
            case R.id.menu_item_del_book:
                for (Book b : mBooksSelected)
                    VocaLab.getVoca(getActivity()).
                            deleteBooks(BookTable.Cols.book_id + " = ?", new String[]{b.getBookId() + ""});
                updateUI();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    /*
     * To get result from the dialog fragments when an option is selected.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK)
            return;
        if (requestCode == REQUEST_TITLE) {
            String title = data.getStringExtra(AddBookFragment.EXTRA_TITLE);

            Book book = new Book();
            String lastModified = DateFormat.format("yyyy-MM-dd", new Date()).toString();
            book.setBookName(title);
            book.setLastModified(lastModified);
            book.setNumWords(0);

            VocaLab.getVoca(getActivity()).addBook(book);
            updateUI();
        }
    }

    private void updateUI() {
        VocaLab vocaLab = VocaLab.getVoca(getActivity());
        List<Book> books = vocaLab.getBooks();
        mBooksSelected = new HashSet<>();
        mSavedViewHolderStatus = new boolean[books.size()];
        if (mAdapter == null) {
            mAdapter = new NoteAdapter(books);
            mBookRecyclerView.setAdapter(mAdapter);
        }
        else {
            mAdapter.setBooks(books);
            mAdapter.notifyDataSetChanged();
        }
    }

    private class BookHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mTitleTextView;
        private TextView mDetailTextView;
        private CheckBox mCheckBox; // to delete/merge multiple Vocabulary Books.
        private Book mBook;
        public int index;

        public BookHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mTitleTextView = (TextView) itemView.findViewById(R.id.title_text_view);
            mDetailTextView = (TextView) itemView.findViewById(R.id.detail_text_view);
            mCheckBox = (CheckBox) itemView.findViewById(R.id.select_check_box);
            mCheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSavedViewHolderStatus[index] = mCheckBox.isChecked();
                    if (mSavedViewHolderStatus[index])
                        mBooksSelected.add(mBook);
                    else
                        mBooksSelected.remove(mBook);
                }
            });
        }

        public void bindBook(Book book) {
            mBook = book;
            mTitleTextView.setText(mBook.getBookName());
            mDetailTextView.setText("단어수: " + mBook.getNumWords() + "\n수정일:" + mBook.getLastModified());
            mCheckBox.setChecked(mSavedViewHolderStatus[index]);
        }

        @Override
        public void onClick(View v) {
            // must pass the Book id.
            // start Book modification Activity
            Intent intent = BookActivity.newIntent(getActivity(), mBook.getBookId());
            startActivity(intent);
        }
    }

    private class NoteAdapter extends RecyclerView.Adapter<BookHolder> {
        private List<Book> mBooks;

        public NoteAdapter(List<Book> books) {
            mBooks = books;
        }

        public void setBooks(List<Book> books) {
            mBooks = books;
        }

        @Override
        public BookHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_item_note, parent, false);
            return new BookHolder(view);
        }

        @Override
        public void onBindViewHolder(BookHolder holder, int position) {
            Book book = mBooks.get(position);
            holder.index = position;
            holder.bindBook(book);
        }

        @Override
        public int getItemCount() {
            return mBooks.size();
        }
    }
}
