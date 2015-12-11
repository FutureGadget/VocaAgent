package hci.com.vocaagent;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.Iterator;

import hci.com.vocaagent.datastructure.RandomQueue;
import hci.com.vocaagent.parser.DictionaryParser;

public class Phase1Fragment extends Fragment {
    private static final String ARG_WORDID = "word_id";
    private TextView mSentenceTextView;
    private RadioButton[] mRadioButton;
    private Button mSubmitButton;
    private Word mWord;
    private RandomQueue mSentences;
    private static final int[] BUTTON_ID = {R.id.choice1, R.id.choice2, R.id.choice3, R.id.choice3};

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        View v = layoutInflater.inflate(R.layout.fragment_phase1, container, false);
        mWord = VocaLab.getVoca(getActivity()).getWordByID(getArguments().getInt(ARG_WORDID));
        mSentenceTextView = (TextView) v.findViewById(R.id.sentence_text_view);
        mSubmitButton = (Button) v.findViewById(R.id.submit_button);
        mRadioButton = new RadioButton[4];
        for (int i = 0; i < 4; ++i) {
            mRadioButton[i] = (RadioButton) v.findViewById(BUTTON_ID[i]);
        }
        // TEST
        mWord.setPhase(0);
        VocaLab.getVoca(getActivity()).updateWord(mWord);
        new AsyncTaskRunner().execute();
        return v;
    }

    private class AsyncTaskRunner extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                mSentences = DictionaryParser.getSentence(mWord.getWord());
            } catch(Throwable t) {
                t.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mSentenceTextView.setText(mSentences.iterator().next()[0]);
        }
    }

    public static Phase1Fragment newInstance(int wordId) {
        Bundle arg = new Bundle();
        arg.putInt(ARG_WORDID, wordId);

        Phase1Fragment fragment = new Phase1Fragment();
        fragment.setArguments(arg);
        return fragment;
    }
}
