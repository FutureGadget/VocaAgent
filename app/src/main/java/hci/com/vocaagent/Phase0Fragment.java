package hci.com.vocaagent;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.Iterator;
import java.util.Locale;

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
    private TextToSpeech mTextToSpeech;

    private Iterator<String[]> mSentencesIterator;
    private Spanned mSavedSentence;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // set retain true. onCreate() will not be called again.
        setRetainInstance(true);
        mWord = VocaLab.getVoca(getActivity()).getWordByID(getArguments().getInt(ARG_WORDID));

        new AsyncTaskRunner().execute();
        mWord.setPhase(1);
        mWord.setRecentTestDate(VocaLab.getToday());
        mWord.setToday(1);

        // init tts object
        mTextToSpeech = new TextToSpeech(getActivity(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    mTextToSpeech.setLanguage(Locale.US);
                }
            }
        });

        VocaLab.getVoca(getActivity()).updateWord(mWord);
        VocaLab.getVoca(getActivity()).addResultWord(mWord, 1);
        VocaLab.getVoca(getActivity()).updateMetaInfo(0, 0, 1); // update the mete Data

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_phase_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_item_set_completed:
                new AlertDialog.Builder(getActivity())
                        .setTitle("완료단어 설정")
                        .setMessage("시험에 다시 안나와도 괜찮습니까?")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mWord.setCompleted(true);
                                VocaLab.getVoca(getActivity()).updateWord(mWord);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .create()
                        .show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_phase0, container, false);
        mWordTitle = (TextView) v.findViewById(R.id.word_title_text_view);
        mContent = (TextView) v.findViewById(R.id.word_meaning_sentence_text_view);
        mShowExamplesButton = (Button) v.findViewById(R.id.show_examples_button);
        mWordTitle.setText(mWord.getWord());

        // if this is a retained fragment
        if (mSentences != null) {

            if (mSavedSentence != null) {
                mContent.setText(mSavedSentence);
                if (mSentencesIterator.hasNext()) {
                    mShowExamplesButton.setText("다음예문");
                } else {
                    mShowExamplesButton.setText("끝");
                }
            }

            else {
                mContent.setText(mMeaning);
            }

            mShowExamplesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mShowExamplesButton.setText("다음예문");
                    if (mSentencesIterator.hasNext()) {
                        String[] text = mSentencesIterator.next();
                        mSavedSentence = Html.fromHtml(emphasizeWord(text[0], text[2]) + "<p>" + text[1] + "</p>");
                        mContent.setText(mSavedSentence);
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
        return v;
    }

    private class AsyncTaskRunner extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

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
            mSentencesIterator = mSentences.iterator();

            mShowExamplesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mShowExamplesButton.setText("다음예문");
                    if (mSentencesIterator.hasNext()) {
                        String[] text = mSentencesIterator.next();
                        mSavedSentence = Html.fromHtml(emphasizeWord(text[0], text[2]) + "<p>" + text[1] + "</p>");
                        mContent.setText(mSavedSentence);
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
    private String emphasizeWord(String sentence, String empWord) {
        if (empWord != null) {
            String processed = sentence.replaceAll("(?i)" + empWord, "<b><font color=#EC407A>" + empWord + "</font></b>");
            return "<p>" + processed + "</p>";
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
