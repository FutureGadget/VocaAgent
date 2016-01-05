package hci.com.vocaagent;

import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import hci.com.vocaagent.parser.DictionaryParser;

public class FlashCardFragment extends Fragment {
    private TextView mWordTextView;
    private TextView mMeaningTextView;
    private ImageButton mWordVoiceButton;
    private TextToSpeech mTextToSpeech;

    public static final int MEMORIZE_MODE = 0;
    public static final int TEST_MODE = 1;

    private static final String ARG_EXAM_WORDS = "arg_exam_words";
    private static final String ARG_OPTION = "arg_option";
    private int mOption;
    private Word mWord;
    private List<Meaning> mMeanings;
    private String mBackupMeanings;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWord = getArguments().getParcelable(ARG_EXAM_WORDS);
        new AsyncTaskRunner().execute(); // get meanings from the Internet (in case there is no user defined one)
        mOption = getArguments().getInt(ARG_OPTION);
        mMeanings = VocaLab.getVoca(getActivity()).getMeaning(mWord.getWordId());

        // init tts object
        mTextToSpeech = new TextToSpeech(getActivity(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    mTextToSpeech.setLanguage(Locale.US);
                }
            }
        });

        mTextToSpeech.setSpeechRate(0.9f);
        mTextToSpeech.setPitch(0.8f);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_phase0, container, false);
        mWordTextView = (TextView) v.findViewById(R.id.word_title_text_view);
        mMeaningTextView = (TextView) v.findViewById(R.id.word_meaning_sentence_text_view);
        mWordVoiceButton = (ImageButton) v.findViewById(R.id.voice_word);

        mWordTextView.setText(mWord.getWord());

        if (mOption == MEMORIZE_MODE) {
            printMeanings();
            mWordVoiceButton = (ImageButton) v.findViewById(R.id.voice_word);
            mWordVoiceButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String toSpeak = mWordTextView.getText().toString();
                    mTextToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                }
            });
        } else if (mOption == TEST_MODE) {
            mMeaningTextView.setText("정답 보기");
            mMeaningTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    printMeanings();
                }
            });
        }
        return v;
    }

    private void printMeanings() {
        if (mMeanings.isEmpty())
            mMeaningTextView.setText(mBackupMeanings);
        else {
            String meaning = "";
            for (Meaning m : mMeanings) {
                meaning += m.getMeaning()+"\n";
            }
            mMeaningTextView.setText(meaning);
        }
    }


    public static FlashCardFragment createFragment(Word word, int option) {
        FlashCardFragment fragment = new FlashCardFragment();
        Bundle arg = new Bundle();
        arg.putParcelable(ARG_EXAM_WORDS, word);
        arg.putInt(ARG_OPTION, option);
        fragment.setArguments(arg);
        return fragment;
    }

    private class AsyncTaskRunner extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... params) {
            mBackupMeanings = DictionaryParser.getMeanings(mWord.getWord());
            return null;
        }
    }
}
