package hci.com.vocaagent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class ExamPagerActivity extends AppCompatActivity {
    private ViewPager mViewPager;
    private List<Word> mWords;
    private boolean mPageEnd;
    private boolean alreadySeen;
    private static final String DIALOG_STATS = "DialogStats";
    private static final String SAVE_STATE = "SAVE_STATE";
    private static final String EXAM_TYPE_OPTION = "option";

    private ViewPager.OnPageChangeListener mListener = new ViewPager.OnPageChangeListener() {
        int selectedIndex;

        @Override
        public void onPageSelected(int position) {
            selectedIndex = position;
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (selectedIndex == mViewPager.getAdapter().getCount() - 1)
                mPageEnd = true;
        }

        boolean callHappened;

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
            // TODO Auto-generated method stub
            if (mPageEnd && arg0 == mViewPager.getAdapter().getCount() - 1 && !callHappened) {
                // Scroll after end of the page
                FragmentManager manager = getSupportFragmentManager();
                StatisticsDialogFragment dialog = new StatisticsDialogFragment();
                dialog.show(manager, DIALOG_STATS);

                mPageEnd = false;//To avoid multiple calls.
                callHappened = true;
            } else {
                mPageEnd = false;
            }
        }
    };

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(SAVE_STATE, alreadySeen);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        StatisticsDialogFragment dialog = new StatisticsDialogFragment();
        dialog.show(getSupportFragmentManager(), DIALOG_STATS);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam_pager);

        if (savedInstanceState != null) {
            alreadySeen = savedInstanceState.getBoolean(SAVE_STATE);
        }

        mViewPager = (ViewPager) findViewById(R.id.activity_exam_pager);

        // new exam or review test
        int option = getIntent().getIntExtra(EXAM_TYPE_OPTION, 0);

        if (option == SelectBookFragment.EXAM_TYPE_NORMAL) {
            // Get words for exam from the exam book list
            mWords = VocaLab.getVoca(ExamPagerActivity.this).getTestWords();
        }
        else if (option == SelectBookFragment.EXAM_TYPE_REVIEW) {
            // get review words
            mWords = new ArrayList<>();
            for (Word w : VocaLab.getVoca(ExamPagerActivity.this).getReviewWords())
                mWords.add(w);
        } else if (option == SelectBookFragment.EXAM_TYPE_COMPLETED) {
            // get completed words
            mWords = new ArrayList<>();
            List<Word> completedWords = VocaLab.getVoca(ExamPagerActivity.this).getCompletedWords();

            for (int i = 0; i < completedWords.size() && i < 10; ++i) {
                mWords.add(completedWords.get(i));
            }
        }

        if (!alreadySeen) {
            alreadySeen = true;
            // prepare to save tested words and words to review after the test
            VocaLab.getVoca(ExamPagerActivity.this).initResultWords();
            VocaLab.getVoca(ExamPagerActivity.this).initReviewWords();
        }

        // Exception : No words in the list.
        if (mWords.size() == 0) {
            finish();
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentPagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                // get a word
                Word word = mWords.get(position);
                // init today count
                initTodayCount(word);
                // return a fragment based on the phase of the word
                if (word.getPhase() == 0) {
                    return Phase0Fragment.newInstance(word.getWordId());
                } else {
                    return Phase1Fragment.newInstance(word.getWordId());
                }
            }

            @Override
            public int getCount() {
                return mWords.size();
            }
        });
        mViewPager.addOnPageChangeListener(mListener);
    }

    private void initTodayCount(Word w) {
        int diff = Utils.getDateDiff(w.getRecentTestDate());
        if (diff != 0) {
            w.setToday(0);
            VocaLab.getVoca(this).updateWord(w);
        }
    }

    public static Intent newIntent(Context context, int option) {
        Intent newIntent = new Intent(context, ExamPagerActivity.class);
        newIntent.putExtra(EXAM_TYPE_OPTION, option);
        return newIntent;
    }
}
