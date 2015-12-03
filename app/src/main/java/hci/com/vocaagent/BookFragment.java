package hci.com.vocaagent;

import android.app.FragmentTransaction;
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

/**
 * Created by dw on 2015-11-21.
 * Users can modify words in vocabulary books on this Fragment.
 */
public class BookFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private WordAdapter mAdapter;
    private int mBookId;
    private static final String ARG_BOOK_ID = "book_id";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_note_manager, container, false);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mBookId = getArguments().getInt(ARG_BOOK_ID);
        updateUI();
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_notemanager_book_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_item_add_word:
                DialogFragment dialogFragment = new AddWordFragment();
                dialogFragment.show(getFragmentManager(),"add_word");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void updateUI() {
        mAdapter = new WordAdapter(VocaLab.getVoca().getBookByID(mBookId).getWords());
        mRecyclerView.setAdapter(mAdapter);
    }

    public static Fragment newInstance(int bid) {
        Bundle args = new Bundle();
        args.putInt(ARG_BOOK_ID, bid);

        BookFragment fragment = new BookFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private class WordHolder extends RecyclerView.ViewHolder {
        private CheckBox mCheckBox;
        private TextView mTextView;
        public WordHolder(View itemView) {
            super(itemView);
            mCheckBox = (CheckBox)itemView.findViewById(R.id.select_word_checkbox);
            mTextView = (TextView)itemView.findViewById(R.id.word_text_view);
        }
        public void bindWord(Word word) {
            mTextView.setText(word.getWord());
        }
    }

    private class WordAdapter extends RecyclerView.Adapter<WordHolder> {
        private List<Word> mWords;
        public WordAdapter (List<Word> words) {
            mWords = words;
        }
        @Override
        public int getItemCount() {
            return mWords.size();
        }

        @Override
        public void onBindViewHolder(WordHolder holder, int position) {
            Word word = mWords.get(position);
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
