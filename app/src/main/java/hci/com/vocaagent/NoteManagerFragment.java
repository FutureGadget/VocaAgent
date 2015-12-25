package hci.com.vocaagent;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NoteManagerFragment extends Fragment {
    private RecyclerView mBookRecyclerView;
    private NoteAdapter mAdapter;
    private boolean[] mSavedViewHolderStatus;
    private Set<Book> mBooksSelected;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initState(VocaLab.getVoca(getActivity()).getBooks().size());
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_note_manager, container, false);

        mBookRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        mBookRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        updateUI();

        AdView mAdView = (AdView) v.findViewById(R.id.note_manager_adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("단어장 내보내기");
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_notemanager_export_menu, menu);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    /*
     * Behaviors when one of the overflow menu options on Toolbar is selected.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_export_book:
                LayoutInflater inflater = LayoutInflater.from(getActivity());
                View exportFile = inflater.inflate(R.layout.dialog_export_file_name, null);
                final EditText exportFileName = (EditText) exportFile.findViewById(R.id.export_file_name);
                AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String exFileName = exportFileName.getText().toString();
                                boolean success = VocaLab.getVoca(getActivity()).exportNote(exFileName, mBooksSelected);
                                if (success)
                                    Toast.makeText(getActivity(), exFileName + ".xls 로 내보내기 완료.", Toast.LENGTH_SHORT).show();
                                else {
                                    Toast.makeText(getActivity(), "내보낼 단어가 없습니다", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .create();
                alertDialog.setView(exportFile);

                alertDialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateUI() {
        VocaLab vocaLab = VocaLab.getVoca(getActivity());
        List<Book> books = vocaLab.getBooks();
        if (mAdapter == null) {
            mAdapter = new NoteAdapter(books);
            mBookRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setBooks(books);
            mAdapter.notifyDataSetChanged();
        }
    }

    // initialize selection status
    private void initState(int size) {
        mBooksSelected = new HashSet<>();
        mSavedViewHolderStatus = new boolean[size];
    }

    private class BookHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mTitleTextView;
        private TextView mDetailTextView;
        private CheckBox mCheckBox; // to delete/merge multiple Vocabulary Books.
        private Book mBook;
        public int index;

        public BookHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this); // enable clicking

            mTitleTextView = (TextView) itemView.findViewById(R.id.title_text_view);
            mDetailTextView = (TextView) itemView.findViewById(R.id.detail_text_view);
            mCheckBox = (CheckBox) itemView.findViewById(R.id.select_check_box);
            mCheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSavedViewHolderStatus[index] = mCheckBox.isChecked();
                    if (mCheckBox.isChecked()) {
                        mBooksSelected.add(mBook);
                    } else
                        mBooksSelected.remove(mBook);
                }
            });
        }

        public void bindBook(Book book) {
            mBook = book;
            mTitleTextView.setText(mBook.getBookName());
            mDetailTextView.setText("완료 단어 수: " + VocaLab.getVoca(getActivity()).getNumberOfCompletedWordsInBook(mBook.getBookId()) +
                    "\n미 완료 단어 수: " + VocaLab.getVoca(getActivity()).getNumberOfNotCompletedWordsInBook(mBook.getBookId()) +
                    "\n수정일:" + mBook.getLastModified()); // Book DB에서 단어수 지우기
            if (mSavedViewHolderStatus[index] && !mCheckBox.isChecked()) {
                mCheckBox.performClick();
            } else if (!mSavedViewHolderStatus[index] && mCheckBox.isChecked()) {
                mCheckBox.performClick();
            }
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
