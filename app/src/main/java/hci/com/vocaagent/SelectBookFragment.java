package hci.com.vocaagent;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import hci.com.vocaagent.ExamPagerActivity;
import hci.com.vocaagent.R;
import hci.com.vocaagent.VocaLab;
import hci.com.vocaagent.Book;

/**
 * Users can choose which books to study.
 */
public class SelectBookFragment extends Fragment {
    private RecyclerView mBookRecyclerView;
    private NoteAdapter mAdapter;
    private Button mSelectButton;
    private boolean[] mSavedViewHolderStatus;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_select_book, container, false);
        mBookRecyclerView = (RecyclerView) v.findViewById(R.id.select_book_recycler_view);
        mBookRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mSelectButton = (Button) v.findViewById(R.id.select_book_button);
        mSelectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ExamPagerActivity.class);
                startActivity(intent);
            }
        });
        updateUI();

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

    private class BookHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private LinearLayout mNoteManagerList;
        private TextView mTitleTextView;
        private TextView mDetailTextView;
        private CheckBox mCheckBox; // to select multiple Vocabulary Books.
        private Book mBook;

        public int index; // for saving view holder's status

        public BookHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            mNoteManagerList = (LinearLayout) itemView.findViewById(R.id.note_manager_list);
            mTitleTextView = (TextView) itemView.findViewById(R.id.title_text_view);
            mDetailTextView = (TextView) itemView.findViewById(R.id.detail_text_view);
            mCheckBox = (CheckBox) itemView.findViewById(R.id.select_check_box);
            mCheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*
                     * Add the book to the ExamBook List when touched.
                     */
                    if (mCheckBox.isChecked()) {
                        VocaLab.getVoca(getActivity()).addExamBook(mBook);
                        changeViewHolderStatus(mCheckBox.isChecked());
                    } else {
                        VocaLab.getVoca(getActivity()).removeExamBook(mBook);
                        changeViewHolderStatus(mCheckBox.isChecked());
                    }
                }
            });
        }

        public void bindBook(Book book) {
            mBook = book;
            mTitleTextView.setText(mBook.getBookName());
            mDetailTextView.setText("단어수: " + mBook.getNumWords() + "\n수정일:" + mBook.getLastModified());
            changeViewHolderStatus(mSavedViewHolderStatus[index]);
        }

        @Override
        public void onClick(View v) {
            mCheckBox.performClick();
        }

        private void changeViewHolderStatus(boolean isChecked) {
            if (isChecked) {
                mNoteManagerList.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                mTitleTextView.setTextColor(getResources().getColor(R.color.colorWhite));
                mDetailTextView.setTextColor(getResources().getColor(R.color.colorWhite));
                mSavedViewHolderStatus[index] = true;
                mCheckBox.setChecked(true);
            } else {
                mNoteManagerList.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                mTitleTextView.setTextColor(getResources().getColor(R.color.textSecondary));
                mDetailTextView.setTextColor(getResources().getColor(R.color.textSecondary));
                mSavedViewHolderStatus[index] = false;
                mCheckBox.setChecked(false);
            }
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
