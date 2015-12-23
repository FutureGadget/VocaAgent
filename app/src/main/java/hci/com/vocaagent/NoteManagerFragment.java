package hci.com.vocaagent;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import hci.com.vocaagent.database.VocaAgentDbSchema.BookTable;

public class NoteManagerFragment extends Fragment {
    private RecyclerView mBookRecyclerView;
    private NoteAdapter mAdapter;
    private boolean[] mSavedViewHolderStatus;
    private Set<Book> mBooksSelected;
    private int mMode;

    public static final String DIALOG_ADD_BOOK = "DIALOG_ADD_BOOK";
    public static final String DIALOG_REMOVE_BOOK = "DIALOG_REMOVE_BOOK";
    public static final int EXPORT_BOOK_MODE = 1;
    public static final int NOTE_MANAGER_MODE = 0;
    private static final String ARG_MODE = "ARG_MODE";
    private static final int REQUEST_TITLE = 0;
    private static final int REQUEST_REMOVE = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initState(VocaLab.getVoca(getActivity()).getBooks().size());
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_note_manager, container, false);

        mMode = getArguments().getInt(ARG_MODE);
        mBookRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        mBookRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        updateUI();

        FloatingActionButton FAB = (FloatingActionButton) v.findViewById(R.id.fab);
        FAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddBookFragment dialogFragment = new AddBookFragment();
                dialogFragment.show(getFragmentManager(), DIALOG_ADD_BOOK);
                dialogFragment.setTargetFragment(NoteManagerFragment.this, REQUEST_TITLE);
            }
        });

        if (mMode == NOTE_MANAGER_MODE)
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("단어장 관리");
        else if (mMode == EXPORT_BOOK_MODE) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("단어장 내보내기");
            FAB.hide();
        }
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (mMode == NOTE_MANAGER_MODE) // manage books mode (add, delete books)
            inflater.inflate(R.menu.fragment_notemanager_menu, menu);
        else if (mMode == EXPORT_BOOK_MODE) {
            inflater.inflate(R.menu.fragment_notemanager_export_menu, menu);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    public static NoteManagerFragment newInstance(int option) {
        NoteManagerFragment fragment = new NoteManagerFragment();
        Bundle arg = new Bundle();
        arg.putInt(ARG_MODE, option);

        fragment.setArguments(arg);
        return fragment;
    }

    /*
     * Behaviors when one of the overflow menu options on Toolbar is selected.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_del_book:
                RemoveConfirmDialog dialog = new RemoveConfirmDialog();
                dialog.show(getFragmentManager(), DIALOG_REMOVE_BOOK);
                dialog.setTargetFragment(NoteManagerFragment.this, REQUEST_REMOVE);
                return true;
            case R.id.menu_item_edit_book:
                if (mBooksSelected.size() != 1) {
                    new AlertDialog.Builder(getActivity())
                            .setNegativeButton(android.R.string.cancel, null)
                            .setMessage("하나의 단어장을 선택해 주세요.")
                            .show();
                } else {
                    View titleChange = LayoutInflater.from(getActivity()).
                            inflate(R.layout.dialog_change_book_title, null);
                    final EditText newTitle = (EditText) titleChange.findViewById(R.id.change_book_title_edit_text);
                    new AlertDialog.Builder(getActivity())
                            .setTitle("단어장 이름 변경")
                            .setView(titleChange)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Book newBook = mBooksSelected.iterator().next();
                                    newBook.setBookName(newTitle.getText().toString());
                                    VocaLab.getVoca(getActivity()).updateBook(newBook);
                                    updateUI();
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, null)
                            .show();
                }
                return true;
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

    private void deleteBooks() {
        for (Book b : mBooksSelected) {
            VocaLab.getVoca(getActivity()).
                    deleteBooks(BookTable.Cols.book_id + " = ?", new String[]{b.getBookId() + ""});
        }
        initState(VocaLab.getVoca(getActivity()).getBooks().size());
        updateUI();
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
            VocaLab.getVoca(getActivity()).addNewBook(title); // add a new book
            initState(VocaLab.getVoca(getActivity()).getBooks().size());
            updateUI();
        } else if (requestCode == REQUEST_REMOVE) {
            deleteBooks();
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
            if (mMode == NOTE_MANAGER_MODE)
                itemView.setOnClickListener(this); // enable clicking when note manager mode (edit book mode)

            mTitleTextView = (TextView) itemView.findViewById(R.id.title_text_view);
            mDetailTextView = (TextView) itemView.findViewById(R.id.detail_text_view);
            mCheckBox = (CheckBox) itemView.findViewById(R.id.select_check_box);
            mCheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSavedViewHolderStatus[index] = mCheckBox.isChecked();
                    if (mCheckBox.isChecked()) {
                        mBooksSelected.add(mBook);
                    }
                    else
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
