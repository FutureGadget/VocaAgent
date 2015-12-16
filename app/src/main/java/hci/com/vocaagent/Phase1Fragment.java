package hci.com.vocaagent;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
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
    private static final String STAT_DIALOG = "STAT_DIALOG";
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
                    for (int i = 0; i < 4; ++i) {
                        if (mRadioButton[i].isChecked()) {
                            isSubmitted = true;
                            scoring(mRadioButton[i].getText().toString());

                            ViewPager vp = (ViewPager) getActivity().findViewById(R.id.activity_exam_pager);
                            if (vp.getCurrentItem() == vp.getAdapter().getCount() - 1) {
                                StatisticsDialogFragment dialog = new StatisticsDialogFragment();
                                dialog.show(getFragmentManager(), STAT_DIALOG);
                            } else
                                vp.setCurrentItem(vp.getCurrentItem() + 1);
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
            int a = r.nextInt(i + 1);

            int temp = BUTTON_ID[i];
            BUTTON_ID[i] = BUTTON_ID[a];
            BUTTON_ID[a] = temp;
        }
    }

    private void scoring(String userAnswer) {
        int testCount = mWord.getTestCount();
        int numCorrect = mWord.getNumCorrect();
        int todayTotal = mWord.getToday();

        String recentTestDate = mWord.getRecentTestDate();
        int dateDiff = Utils.getDateDiff(recentTestDate);

        int phaseIncrement = 0;

        int phase = mWord.getPhase();

        if (mWord.getWord().toLowerCase().equals(userAnswer.toLowerCase())) {
            numCorrect++;
            phaseIncrement = getPhaseIncrement(testCount, dateDiff, true, phase);
        } else {
            numCorrect--;
            phaseIncrement = getPhaseIncrement(testCount, dateDiff, false, phase);
        }

        if (todayTotal >= 2 && phaseIncrement > 0) {
            phaseIncrement = 0;
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

        if (phaseIncrement < 0)
            VocaLab.getVoca(getActivity()).addReviewWords(mWord);

        VocaLab.getVoca(getActivity()).updateWord(mWord);
        VocaLab.getVoca(getActivity()).addResultWord(mWord, phaseIncrement);
    }

    /**
     * 채점방식
     * Recent Test Date에 기반하여 phase +- 정도를 정한다.
     * 3일 이내 다시 나온 단어를 틀렸을 시 무조건 phase 0으로
     * 하루에 올라갈 수 있는 phase는 최대 2.
     * phase가 10이 되면 완료비트 설정하여 우선순위를 최하위로 한다.
     * (최소 5일은 맞춰야 완료가능)
     *
     * @param testCount  To check this is the first time
     * @param dateDiff   recent test date - today
     * @param correct    is correct answer
     * @param phase      current phase of the word
     * @return phase increment value
     */
    private int getPhaseIncrement(int testCount, int dateDiff, boolean correct, int phase) {
        if (correct) {
            Toast.makeText(getActivity(), "정답입니다!", Toast.LENGTH_SHORT).show();
            if (testCount == 0) {
                return 1;
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
        } else {
            Toast.makeText(getActivity(), "오답입니다!", Toast.LENGTH_SHORT).show();
            if (testCount == 0 || dateDiff < 3 || phase < 2) {
                return -phase;
            } else if (dateDiff < 5) {
                return -(int)(phase*0.8);
            } else if (dateDiff < 7) {
                return -(int)(phase*0.5);
            } else {
                return -(int)(phase*0.3);
            }
        }
    }

    private class AsyncTaskRunner extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                mSentences = DictionaryParser.getSentence(mWord.getWord());
            } catch (Throwable t) {
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
