package hci.com.vocaagent;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hci.com.vocaagent.datastructure.RandomQueue;
import hci.com.vocaagent.parser.DictionaryParser;

public class Phase0Fragment extends Fragment {
    private TextView mWordTitle;
    private TextView mContent; // meaning, sentence
    private static final String ARG_WORDID = "word_id";
    private static final String STAT_DIALOG = "STAT_DIALOG";
    private Word mWord;
    private String mMeaning;
    private RandomQueue mSentences;
    private Button mShowExamplesButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_phase0, container, false);
        mWord = VocaLab.getVoca(getActivity()).getWordByID(getArguments().getInt(ARG_WORDID));
        mWordTitle = (TextView) v.findViewById(R.id.word_title_text_view);
        mContent = (TextView) v.findViewById(R.id.word_meaning_sentence_text_view);
        mShowExamplesButton = (Button) v.findViewById(R.id.show_examples_button);
        mWordTitle.setText(mWord.getWord());
        new AsyncTaskRunner().execute();

        mWord.setPhase(1);
        mWord.setRecentTestDate(VocaLab.getToday());
        mWord.setToday(1);
//        mWord.setNumCorrect(mWord.getNumCorrect() + 1);
//        mWord.setTestCount(mWord.getTestCount() + 1);

        VocaLab.getVoca(getActivity()).updateWord(mWord);
        VocaLab.getVoca(getActivity()).addResultWord(mWord, 1);
        VocaLab.getVoca(getActivity()).updateMetaInfo(1, 1, 1); // update meta data
        return v;
    }

    private class AsyncTaskRunner extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                mMeaning = DictionaryParser.getMeanings(mWord.getWord());
                mSentences = DictionaryParser.getSentence(mWord.getWord());
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mContent.setText(mMeaning);
            final Iterator<String[]> it = mSentences.iterator();
            mShowExamplesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mShowExamplesButton.setText("다음예문");
                    if (it.hasNext()) {
                        String[] text = it.next();
                        mContent.setText(Html.fromHtml(getWordEmphasizedSentenceHTMLFormat(text[0]) + "<p>"+text[1]+"</p>"));
                    } else {
                        mShowExamplesButton.setText("끝");
                        ViewPager vp = (ViewPager) getActivity().findViewById(R.id.activity_exam_pager);
                        if (vp.getCurrentItem() == vp.getAdapter().getCount() - 1) {
                            StatisticsDialogFragment dialog = new StatisticsDialogFragment();
                            dialog.show(getFragmentManager(), STAT_DIALOG);
                        } else
                            vp.setCurrentItem(vp.getCurrentItem() + 1);
                    }
                }
            });
        }
    }

    // set bold, color to the matching word
    private String getWordEmphasizedSentenceHTMLFormat(String sentence) {
        String pattern = "("+mWord.getWord()+"[^\\s]*)";
        Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(sentence);
        if (m.find()) {
            String processed = sentence.replaceAll("(?i)"+m.group(), "<b><font color=#EC407A>"+m.group()+"</font></b>");
            return "<p>"+processed+"</p>";
        }
        return sentence;
    }

    public static Phase0Fragment newInstance(int wordId) {
        Bundle arg = new Bundle();
        arg.putInt(ARG_WORDID, wordId);

        Phase0Fragment fragment = new Phase0Fragment();
        fragment.setArguments(arg);
        return fragment;
    }
}
