package hci.com.vocaagent;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import hci.com.vocaagent.datastructure.RandomQueue;
import hci.com.vocaagent.parser.DictionaryParser;

public class Phase1Fragment extends Fragment {
    private static final String ARG_WORDID = "word_id";
    private TextView mSentenceTextView;
    private RadioButton[] mRadioButton;
    private Button mSubmitButton;
    private boolean isSubmitted;
    private Word mWord;
    private RandomQueue mSentences;
    private static final int[] BUTTON_ID = {R.id.choice1, R.id.choice2, R.id.choice3, R.id.choice4};

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        View v = layoutInflater.inflate(R.layout.fragment_phase1, container, false);
        mWord = VocaLab.getVoca(getActivity()).getWordByID(getArguments().getInt(ARG_WORDID));
        mSentenceTextView = (TextView) v.findViewById(R.id.sentence_text_view);
        mSubmitButton = (Button) v.findViewById(R.id.submit_button);
        mRadioButton = new RadioButton[4];

        buildSelects(v);

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isSubmitted) {
                    isSubmitted = true;
                    for (int i = 0; i < 4; ++i) {
                        if (mRadioButton[i].isChecked()) {
                            scoring(mRadioButton[i].getText().toString());
                        }
                    }
                }
            }
        });

        new AsyncTaskRunner().execute();
        return v;
    }

    private void buildSelects(View v) {
        shuffle(); // shuffle buttons
        for (int i = 0; i < 4; ++i) {
            mRadioButton[i] = (RadioButton) v.findViewById(BUTTON_ID[i]);
        }

        List<String> randomWords = VocaLab.getVoca(getActivity()).getRandomWords();
        for (int i = 0; i < 3; ++i) {
            mRadioButton[i].setText(randomWords.get(i));
        }
        mRadioButton[3].setText(mWord.getWord());
    }

    // shuffle buttons
    private void shuffle() {
        Random r = new Random();
        for (int i = 0; i < BUTTON_ID.length; ++i) {
            int a = r.nextInt(i+1);

            int temp = BUTTON_ID[i];
            BUTTON_ID[i] = BUTTON_ID[a];
            BUTTON_ID[a] = temp;
        }
    }

    private void scoring(String userAnswer) {
        int limit = 2;
        int testCount = mWord.getTestCount();
        int numCorrect = mWord.getNumCorrect();
        int todayTotal = mWord.getToday();

        String recentTestDate = mWord.getRecentTestDate();
        int dateDiff = getDateDiff(recentTestDate);

        int phaseIncrement = 0;

        int phase = mWord.getPhase();

        if (mWord.getWord().toLowerCase().equals(userAnswer.toLowerCase())) {
            numCorrect++;
            phaseIncrement = getPhaseIncrement(testCount, dateDiff, todayTotal, true, phase);
        }
        else {
            numCorrect--;
            phaseIncrement = getPhaseIncrement(testCount, dateDiff, todayTotal, false, phase);
        }

        todayTotal += phaseIncrement;
        phase += phaseIncrement;
        recentTestDate = VocaLab.getToday();
        testCount++;

        // Update word DB
        mWord.setTestCount(testCount);
        mWord.setPhase(phase);
        mWord.setNumCorrect(numCorrect);
        if (phase >= 10) {
            mWord.setCompleted(true);
        }
        mWord.setRecentTestDate(recentTestDate);
        mWord.setToday(todayTotal);
        VocaLab.getVoca(getActivity()).updateWord(mWord);
    }

    /**채점방식
     Recent Test Date에 기반하여 phase +- 정도를 정한다.
     3일 이내 다시 나온 단어를 틀렸을 시 무조건 phase 0으로
     하루에 올라갈 수 있는 phase는 최대 2.
     phase가 10이 되면 완료비트 설정하여 우선순위를 최하위로 한다.
     (최소 5일은 맞춰야 완료가능)
     * @param testCount To check this is the first time
     * @param dateDiff recent test date - today
     * @param todayTotal to check if the phase is not exceeding daily limit
     * @param correct is correct answer
     * @param phase current phase of the word
     * @return phase increment value
     */
    private int getPhaseIncrement(int testCount, int dateDiff, int todayTotal, boolean correct, int phase) {
        if (correct) {
            Toast.makeText(getActivity(), "정답입니다!", Toast.LENGTH_SHORT).show();
            if (testCount == 0) {
                return 1;
            } else {
                if (dateDiff == 0) {
                    if (todayTotal < 2) {
                        return 1;
                    } else {
                        return 0;
                    }
                } else {
                    if (dateDiff > 7) {
                        return 5;
                    } else if (dateDiff > 5) {
                        return 3;
                    } else if (dateDiff > 3) {
                        return 2;
                    } else {
                        return 1;
                    }
                }
            }
        }else {
            Toast.makeText(getActivity(), "오답입니다!", Toast.LENGTH_SHORT).show();
            if (dateDiff < 3) {
                return -phase;
            } else if (dateDiff < 5) {
                return 2-phase;
            } else if (dateDiff < 7) {
                return 5-phase;
            } else {
                return 7-phase;
            }
        }
    }

    private int getDateDiff(String recentTestDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date recent = null;
        Date now = null;
        try {
            recent = formatter.parse(recentTestDate);
            now = formatter.parse(VocaLab.getToday());
        } catch(Exception e) {}

        return Days.daysBetween(new DateTime(recent), new DateTime(now)).getDays();
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
            Iterator<String[]> it = mSentences.iterator();
            String test = "";
            if (it.hasNext()) {
                String[] testStr = it.next()[0].split("\\s+");

                // make test sentence (insert blank to matched words)
                for (String s : testStr) {
                    if (s.toLowerCase().equals(mWord.getWord().toLowerCase())) {
                        test += "_____" + " ";
                    } else {
                        test += s + " ";
                    }
                }
            } else {
                test = "Sorry, There is no example sentence.";
            }
            mSentenceTextView.setText(test);
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
