package hci.com.vocaagent;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.List;

public class ExamPagerActivity extends AppCompatActivity {
    private ViewPager mViewPager;
    private ArrayList<Word> mWords;
    private ArrayList<Book> mExamBooks;
    private boolean mPageEnd;
    private boolean alreadySeen;
    private int saveOption;
    private static final String DIALOG_STATS = "DialogStats";
    private static final String SAVE_STATE_SEEN = "SAVE_STATE_SEEN";
    private static final String SAVE_STATE_OPTION = "SAVE_STATE_OPTION";
    private static final String EXAM_TYPE_OPTION = "option";
    private static final String EXAM_BOOKS = "exam_books";
    private static final String EXAM_WORDS = "exam_words";
    private static final int offScreenPageLimit = 1;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(SAVE_STATE_SEEN, alreadySeen);
        outState.putInt(SAVE_STATE_OPTION, saveOption);
        outState.putParcelableArrayList(EXAM_WORDS, mWords);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        StatisticsDialogFragment dialog = new StatisticsDialogFragment();
        dialog.show(getSupportFragmentManager(), DIALOG_STATS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam_pager);

        // check internet status
        final ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
        if (activeNetwork == null || !activeNetwork.isConnected()) {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setTitle("No internet connection")
                    .setMessage("시험보기는 인터넷 연결이 필요합니다.")
                    .create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        // set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // set ads
        AdView mAdView = (AdView) findViewById(R.id.exam_pager_adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        if (savedInstanceState == null) {
            saveOption = getIntent().getIntExtra(EXAM_TYPE_OPTION, 0);
            mExamBooks = getIntent().getParcelableArrayListExtra(EXAM_BOOKS);
            // GET WORDS FOR THE TEST
            if (saveOption == SelectBookFragment.EXAM_TYPE_NORMAL) {
                // Get words for exam from the exam book list
                mWords = VocaLab.getVoca(ExamPagerActivity.this).getTestWords(mExamBooks);
            } else if (saveOption == SelectBookFragment.EXAM_TYPE_REVIEW) {
                // get review words
                mWords = new ArrayList<>();
                for (Word w : VocaLab.getVoca(ExamPagerActivity.this).getReviewWords())
                    mWords.add(w);
            } else if (saveOption == SelectBookFragment.EXAM_TYPE_COMPLETED) {
                // get completed words
                mWords = new ArrayList<>();
                List<Word> completedWords = VocaLab.getVoca(ExamPagerActivity.this).getCompletedWords();

                for (int i = 0; i < completedWords.size() && i < 10; ++i) {
                    mWords.add(completedWords.get(i));
                }
            }
        } else {
            alreadySeen = savedInstanceState.getBoolean(SAVE_STATE_SEEN);
            saveOption = savedInstanceState.getInt(SAVE_STATE_OPTION);
            mWords = savedInstanceState.getParcelableArrayList(EXAM_WORDS);
        }

        // Exception : No words in the list.
        if (mWords.size() == 0) {
            finish();
        }

        if (!alreadySeen) {
            alreadySeen = true;
            // init words today Count
            for (Word w : mWords) {
                initTodayCount(w);
            }
            // prepare to save tested words and words to review after the test
            VocaLab.getVoca(ExamPagerActivity.this).initResultWords();
            VocaLab.getVoca(ExamPagerActivity.this).initReviewWords();
        }

        mViewPager = (ViewPager) findViewById(R.id.activity_exam_pager);

        final FragmentManager fragmentManager = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentPagerAdapter(fragmentManager) {

            @Override
            public Fragment getItem(int position) {
                // get a word
                Word word = mWords.get(position);

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
        mViewPager.setOffscreenPageLimit(offScreenPageLimit);
        mViewPager.addOnPageChangeListener(mListener);
    }

    private void initTodayCount(Word w) {
        int diff = Utils.getDateDiff(w.getRecentTestDate());
        if (diff != 0) {
            w.setToday(0);
            VocaLab.getVoca(this).updateWord(w);
        }
    }

    public static Intent newIntent(Context context, int option, ArrayList<Book> examBooks) {
        Intent newIntent = new Intent(context, ExamPagerActivity.class);
        newIntent.putParcelableArrayListExtra(EXAM_BOOKS, examBooks);
        newIntent.putExtra(EXAM_TYPE_OPTION, option);
        return newIntent;
    }

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
}
