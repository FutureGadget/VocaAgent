package hci.com.vocaagent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Users can choose which books to study.
 */
public class SelectBookFragment extends Fragment {
    private RecyclerView mBookRecyclerView;
    private NoteAdapter mAdapter;
    private boolean[] mSavedViewHolderStatus;
    private LinearLayout mEmptyLinearLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_select_book, container, false);
        mBookRecyclerView = (RecyclerView) v.findViewById(R.id.select_book_recycler_view);
        mBookRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mEmptyLinearLayout = (LinearLayout) v.findViewById(R.id.select_book_recycler_view_empty);
        updateUI();

        if (mAdapter.getItemCount() == 0) {
            mBookRecyclerView.setVisibility(View.GONE);
            mEmptyLinearLayout.setVisibility(View.VISIBLE);
        } else {
            mBookRecyclerView.setVisibility(View.VISIBLE);
            mEmptyLinearLayout.setVisibility(View.GONE);
        }


        getActivity().setTitle("학습 시작");
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Landscape -> vertical -> landscape..
        VocaLab.getVoca(getActivity()).resetExamBooks();
        updateUI();
    }

    private void updateUI() {
        VocaLab vocaLab = VocaLab.getVoca(getActivity());
        List<Book> books = vocaLab.getBooks();
        mSavedViewHolderStatus = new boolean[books.size()];
        if (mAdapter == null) {
            mAdapter = new NoteAdapter(books);
            mBookRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setBooks(books);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_selectbook_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.selectbook_menu_menu_start:
                if (VocaLab.getVoca(getActivity()).getExamBooks().isEmpty()) {
                    Toast.makeText(getActivity(), "단어장을 선택하세요.", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = ExamPagerActivity.newIntent(getActivity(), 0);
                    startActivityForResult(intent, 1); // test request code = 1
                }
                return true;
            case R.id.selectbook_menu_select_all:
                for (int i = 0; i < mSavedViewHolderStatus.length; ++i)
                    mSavedViewHolderStatus[i] = true;
                mAdapter.notifyDataSetChanged();
                return true;
            case R.id.selectbook_menu_select_cancel:
                for (int i = 0; i < mSavedViewHolderStatus.length; ++i)
                    mSavedViewHolderStatus[i] = false;
                mAdapter.notifyDataSetChanged();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 1) {
            Intent intent = ExamPagerActivity.newIntent(getActivity(), 1);
            startActivityForResult(intent, 1);
        }
    }

    private class BookHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mTitleTextView;
        private TextView mDetailTextView;
        private CheckBox mCheckBox; // to select multiple Vocabulary Books.
        private Book mBook;

        public int index; // for saving view holder's status

        public BookHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            mTitleTextView = (TextView) itemView.findViewById(R.id.title_text_view);
            mDetailTextView = (TextView) itemView.findViewById(R.id.detail_text_view);
            mCheckBox = (CheckBox) itemView.findViewById(R.id.select_check_box);
            mCheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*
                     * Add the book to the ExamBook List when touched.
                     */
                    mSavedViewHolderStatus[index] = mCheckBox.isChecked();
                    if (mCheckBox.isChecked()) {
                        VocaLab.getVoca(getActivity()).addExamBook(mBook);

                    } else {
                        VocaLab.getVoca(getActivity()).removeExamBook(mBook);
                    }
                }
            });
        }

        public void bindBook(Book book) {
            mBook = book;
            mTitleTextView.setText(mBook.getBookName());
            if (mSavedViewHolderStatus[index]) { mCheckBox.performClick(); }
            else if(mCheckBox.isChecked() == true) {
                mCheckBox.performClick();
            }
//            mCheckBox.setChecked(mSavedViewHolderStatus[index]);
            mDetailTextView.setText("단어수: " + mBook.getNumWords() + "\n수정일:" + mBook.getLastModified());
        }

        @Override
        public void onClick(View v) {
            mCheckBox.performClick();
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
