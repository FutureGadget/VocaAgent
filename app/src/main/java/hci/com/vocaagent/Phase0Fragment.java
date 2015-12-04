package hci.com.vocaagent;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class Phase0Fragment extends Fragment {
    private TextView mWordTitle;
    private TextView mContent; // meaning, sentence
    private static final String ARG_WORDID = "word_id";
    private Word mWord;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_phase0, container, false);
        mWord = VocaLab.getVoca(getActivity()).getWordByID(getArguments().getInt(ARG_WORDID));
        mWordTitle = (TextView)v.findViewById(R.id.word_title_text_view);
        mContent = (TextView)v.findViewById(R.id.word_meaning_sentence_text_view);

        //TEST
        mWordTitle.setText(mWord.getWord() + " in Book#" + mWord.getBookid());
        return v;
    }

    public static Phase0Fragment newInstance(int wordId) {
        Bundle arg = new Bundle();
        arg.putInt(ARG_WORDID, wordId);

        Phase0Fragment fragment = new Phase0Fragment();
        fragment.setArguments(arg);
        return fragment;
    }
}
