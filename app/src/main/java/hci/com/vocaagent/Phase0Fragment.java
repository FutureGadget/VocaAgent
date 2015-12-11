package hci.com.vocaagent;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.Iterator;

import hci.com.vocaagent.datastructure.RandomQueue;
import hci.com.vocaagent.parser.DictionaryParser;

public class Phase0Fragment extends Fragment {
    private TextView mWordTitle;
    private TextView mContent; // meaning, sentence
    private static final String ARG_WORDID = "word_id";
    private Word mWord;
    private String mMeaning;
    private Iterator<String[]> mIterator;
    private Button mShowExamplesButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_phase0, container, false);
        mWord = VocaLab.getVoca(getActivity()).getWordByID(getArguments().getInt(ARG_WORDID));
        mWordTitle = (TextView) v.findViewById(R.id.word_title_text_view);
        mContent = (TextView) v.findViewById(R.id.word_meaning_sentence_text_view);
        mShowExamplesButton = (Button)v.findViewById(R.id.show_examples_button);
        mWordTitle.setText(mWord.getWord());
        new AsyncTaskRunner().execute();

        mWord.setPhase(1);
        VocaLab.getVoca(getActivity()).updateWord(mWord);
        return v;
    }

    private class AsyncTaskRunner extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                mMeaning = DictionaryParser.getMeanings(mWord.getWord());
                mIterator = DictionaryParser.getSentence(mWord.getWord()).iterator();
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mContent.setText(mMeaning);
            mShowExamplesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mShowExamplesButton.setText("다음예문");
                    if (mIterator.hasNext()) {
                        String[] text = mIterator.next();
                        mContent.setText(text[0] + "\n\n" + text[1]);
                    } else {
                        mShowExamplesButton.setText("끝");
                    }
                }
            });
        }
    }

    public static Phase0Fragment newInstance(int wordId) {
        Bundle arg = new Bundle();
        arg.putInt(ARG_WORDID, wordId);

        Phase0Fragment fragment = new Phase0Fragment();
        fragment.setArguments(arg);
        return fragment;
    }
}
