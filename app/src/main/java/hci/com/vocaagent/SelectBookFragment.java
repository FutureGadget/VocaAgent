package hci.com.vocaagent;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.List;

/**
 * Users can choose which books to study.
 */
public class SelectBookFragment extends Fragment {
    //    private RecyclerView mBookRecyclerView;
    private NoteAdapter mAdapter;
    private boolean[] mSavedViewHolderStatus;
    private List<Book> mBooks;
    private ArrayList<Book> mExamBooks;
    private RecyclerView mBookRecyclerView;
    private LinearLayout mEmptyLinearLayout;
    private FloatingActionButton fab;

    // request codes
    private static final int REQUEST_START_TEST = 0;
    private static final int REQUEST_TITLE = 1;
    private static final int REQUEST_REFRESH = 2;

    // options
    public static final int EXAM_TYPE_NORMAL = 0;
    public static final int EXAM_TYPE_REVIEW = 1;
    public static final int EXAM_TYPE_COMPLETED = 2;
    public static final int EXAM_TYPE_CONTINUE = 3;

    public static final String DIALOG_ADD_BOOK = "DIALOG_ADD_BOOK";
    public static final String NOTE_MANAGE_OPTIONS_DIALOG = "NOTE_MANAGE_OPTIONS_DIALOG";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_select_book, container, false);
        mBookRecyclerView = (RecyclerView) v.findViewById(R.id.select_book_recycler_view);
        mBookRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mEmptyLinearLayout = (LinearLayout) v.findViewById(R.id.select_book_recycler_view_empty);

        AdView mAdView = (AdView) v.findViewById(R.id.select_book_adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        fab = (FloatingActionButton) v.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddBookFragment dialogFragment = new AddBookFragment();
                dialogFragment.show(getFragmentManager(), DIALOG_ADD_BOOK);
                dialogFragment.setTargetFragment(SelectBookFragment.this, REQUEST_TITLE);
            }
        });

        if (mAdapter == null)
            mAdapter = new NoteAdapter();
        mBookRecyclerView.setAdapter(mAdapter);

        updateUI();

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("학습 시작");

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    private void updateUI() {
        if (mAdapter != null) {
            mBooks = VocaLab.getVoca(getActivity()).getBooks();
            mAdapter.notifyDataSetChanged();
            if (mAdapter.getItemCount() == 0) {
                mBookRecyclerView.setVisibility(View.GONE);
                mEmptyLinearLayout.setVisibility(View.VISIBLE);
                setFABAnchorID(R.id.select_book_recycler_view_empty);
            } else {
                mBookRecyclerView.setVisibility(View.VISIBLE);
                mEmptyLinearLayout.setVisibility(View.GONE);
                setFABAnchorID(R.id.select_book_recycler_view);
            }
        }
    }

    private void setFABAnchorID(int id) {
        CoordinatorLayout.LayoutParams p = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
        p.setAnchorId(id);
        fab.setLayoutParams(p);
    }

    private void init() {
        mBooks = VocaLab.getVoca(getActivity()).getBooks();
        int size = mBooks.size();
        mSavedViewHolderStatus = new boolean[size];
        mExamBooks = new ArrayList<>();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_selectbook_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.selectbook_menu_menu_start:
                if (mExamBooks.isEmpty()) {
                    Toast.makeText(getActivity(), "단어장을 선택하세요.", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = ExamPagerActivity.newIntent(getActivity(), EXAM_TYPE_NORMAL, mExamBooks);
                    startActivityForResult(intent, REQUEST_START_TEST); // test request code = 1
                }
                return true;
            case R.id.selectbook_menu_test_completed_words:
                Intent intent = ExamPagerActivity.newIntent(getActivity(), EXAM_TYPE_COMPLETED, mExamBooks);
                startActivityForResult(intent, REQUEST_START_TEST);
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
        if (requestCode == REQUEST_START_TEST) {
            if (resultCode == EXAM_TYPE_REVIEW) {
                Intent intent = ExamPagerActivity.newIntent(getActivity(), EXAM_TYPE_REVIEW, mExamBooks);
                startActivityForResult(intent, REQUEST_START_TEST);
            } else if (resultCode == EXAM_TYPE_CONTINUE) {
                Intent intent = ExamPagerActivity.newIntent(getActivity(), EXAM_TYPE_NORMAL, mExamBooks);
                startActivityForResult(intent, REQUEST_START_TEST);
            }
        }

        else if (requestCode == REQUEST_TITLE) {
            String title = data.getStringExtra(AddBookFragment.EXTRA_TITLE);
            VocaLab.getVoca(getActivity()).addNewBook(title); // add a new book
            init();
            updateUI();
        }
        else if (requestCode == REQUEST_REFRESH) {
            init();
            updateUI();
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
                        mExamBooks.add(mBook);

                    } else {
                        mExamBooks.remove(mBook);
                    }
                }
            });
        }

        public void bindBook(Book book) {
            mBook = book;
            mTitleTextView.setText(mBook.getBookName());
            if (mSavedViewHolderStatus[index] ^ mCheckBox.isChecked()) {
                mCheckBox.performClick();
            }
            mDetailTextView.setText("완료 단어 수: " + VocaLab.getVoca(getActivity()).getNumberOfCompletedWordsInBook(mBook.getBookId()) +
                    "\n미 완료 단어 수: " + VocaLab.getVoca(getActivity()).getNumberOfNotCompletedWordsInBook(mBook.getBookId()) +
                    "\n수정일:" + mBook.getLastModified());
        }

        @Override
        public void onClick(View v) {
            NoteManageOptionsDialog optionsDialog = NoteManageOptionsDialog.newDialogInstance(mBook.getBookId());
            FragmentManager fm = getActivity().getSupportFragmentManager();
            optionsDialog.setTargetFragment(SelectBookFragment.this, REQUEST_REFRESH);
            optionsDialog.show(fm, NOTE_MANAGE_OPTIONS_DIALOG);
        }
    }

    private class NoteAdapter extends RecyclerView.Adapter<BookHolder> {
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
