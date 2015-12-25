package hci.com.vocaagent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateFormat;
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

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import hci.com.vocaagent.database.VocaAgentDbSchema.*;

/**
 * Users can modify words in vocabulary books on this Fragment.
 */
public class BookFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private WordAdapter mAdapter;
    private int mBookId;
    private static final String ARG_BOOK_ID = "book_id";
    private boolean[] mSavedViewHolderStatus;
    private Set<Word> mWordsSelected;

    private static final int REQUEST_ADD_WORD = 0;
    private static final int REQUEST_REMOVE_WORD = 1;

    public static final String ADD_WORD_DIALOG = "ADD_WORD_DIALOG";
    public static final String REMOVE_WORD_DIALOG = "REMOVE_WORD_DIALOG";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_book, container, false);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.book_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mBookId = getArguments().getInt(ARG_BOOK_ID);

        FloatingActionButton FAB = (FloatingActionButton) v.findViewById(R.id.book_fab);
        FAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddWordFragment dialogFragment = new AddWordFragment();// notify which book is selected so that the word can be added accordingly.
                dialogFragment.show(getFragmentManager(), ADD_WORD_DIALOG);
                dialogFragment.setTargetFragment(BookFragment.this, REQUEST_ADD_WORD);
            }
        });

        AdView mAdView = (AdView) v.findViewById(R.id.book_adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        updateUI();

        getActivity().setTitle("단어 관리");
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_notemanager_book_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_del_word:
                RemoveConfirmDialog dialog = new RemoveConfirmDialog();
                dialog.show(getFragmentManager(), REMOVE_WORD_DIALOG);
                dialog.setTargetFragment(BookFragment.this, REQUEST_REMOVE_WORD);
                return true;
            case R.id.manager_menu_item_set_completed:
                for (Word w : mWordsSelected) {
                    w.setCompleted(true);
                    VocaLab.getVoca(getActivity()).updateWord(w);
                }
                updateUI();
                return true;
            case R.id.manager_menu_item_set_not_completed:
                for (Word w : mWordsSelected) {
                    w.setCompleted(false);
                    VocaLab.getVoca(getActivity()).updateWord(w);
                }
                updateUI();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK)
            return;
        if (requestCode == REQUEST_ADD_WORD) {
            String wordString = data.getStringExtra(AddWordFragment.EXTRA_WORD);
            VocaLab vocaLab = VocaLab.getVoca(getActivity());
            // add new word
            vocaLab.addNewWord(wordString, mBookId);

            updateUI();
        }
        else if (requestCode == REQUEST_REMOVE_WORD) {
            deleteWords();
        }
    }

    private void deleteWords() {
        int countDeleted = 0;
        VocaLab vocaLab = VocaLab.getVoca(getActivity());
        for (Word w : mWordsSelected) {
            vocaLab.deleteWords(WordTable.Cols.word_id +
                    " = ? AND " + WordTable.Cols.book_id + " = ?", new String[]{w.getWordId() + "", mBookId + ""});
            ++countDeleted;
        }
        // update book (update number of contained words and last modified date)
        Book updateBook = vocaLab.getBookByID(mBookId);
        updateBook.setNumWords(updateBook.getNumWords()-countDeleted);
        updateBook.setLastModified(VocaLab.getToday());
        vocaLab.updateBook(updateBook);
        updateUI();
    }

    public void updateUI() {
        List<Word> words = VocaLab.getVoca(getActivity()).getWordInBook(mBookId);

        mWordsSelected = new HashSet<>();
        mSavedViewHolderStatus = new boolean[words.size()];
        if (mAdapter == null) {
            mAdapter = new WordAdapter(words);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setWords(words);
            mAdapter.notifyDataSetChanged();
        }
    }

    public static BookFragment newInstance(int bid) {
        Bundle args = new Bundle();
        args.putInt(ARG_BOOK_ID, bid);

        BookFragment fragment = new BookFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private class WordHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private CheckBox mCheckBox;
        private TextView mTextView;
        private Word mWord;

        public int index;

        public WordHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mCheckBox = (CheckBox) itemView.findViewById(R.id.select_word_checkbox);
            mTextView = (TextView) itemView.findViewById(R.id.word_text_view);
            mCheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSavedViewHolderStatus[index] = mCheckBox.isChecked();
                    if (mCheckBox.isChecked()) {
                        mWordsSelected.add(mWord);
                    } else {
                        mWordsSelected.remove(mWord);
                    }
                }
            });
        }

        public void bindWord(Word word) {
            mWord = word;

            // coloring if completed
            if (mWord.isCompleted()) {
                mTextView.setText(Html.fromHtml("<font color=#009688>" + mWord.getWord() + "</font>"));
            } else
                mTextView.setText(word.getWord());
            mCheckBox.setChecked(mSavedViewHolderStatus[index]);
        }

        @Override
        public void onClick(View v) {
            mCheckBox.performClick();
        }
    }

    private class WordAdapter extends RecyclerView.Adapter<WordHolder> {
        private List<Word> mWords;

        public WordAdapter(List<Word> words) {
            mWords = words;
        }

        @Override
        public int getItemCount() {
            return mWords.size();
        }

        public void setWords(List<Word> words) {
            mWords = words;
        }

        @Override
        public void onBindViewHolder(WordHolder holder, int position) {
            Word word = mWords.get(position);
            holder.index = position;
            holder.bindWord(word);
        }

        @Override
        public WordHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_item_word, parent, false);
            return new WordHolder(view);
        }
    }
}
