package hci.com.vocaagent;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

public class NoteManagerFragment extends Fragment {
    private RecyclerView mBookRecyclerView;
    private NoteAdapter mAdapter;
    private boolean[] mSavedViewHolderStatus;

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
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_item_add_book:
                AddBookFragment dialogFragment = new AddBookFragment();
                dialogFragment.show(getFragmentManager(),"add_book");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    private void updateUI() {
        VocaLab vocaLab = VocaLab.getVoca(getActivity());
        List<Book> books = vocaLab.getBooks();
        if (mAdapter == null) {
            mAdapter = new NoteAdapter(books);
            mBookRecyclerView.setAdapter(mAdapter);
        }
        else {
            mAdapter.setBooks(books);
            mAdapter.notifyDataSetChanged();
        }
        mSavedViewHolderStatus = new boolean[books.size()];
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
