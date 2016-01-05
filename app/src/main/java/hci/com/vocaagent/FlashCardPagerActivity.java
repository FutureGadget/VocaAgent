package hci.com.vocaagent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;

public class FlashCardPagerActivity extends AppCompatActivity {
    private static final String TEST_TYPE = "test_type";
    private static final String EXAM_WORDS = "exam_words";

    private int mTestType;
    private ArrayList<Word> mTestWords;
    private ViewPager mViewPager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam_pager);

        // set toolbar
        Toolbar toolbar = (Toolbar)findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        // set up button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // set ads
        AdView mAdView = (AdView) findViewById(R.id.exam_pager_adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // set title
        toolbar.setTitle("플래쉬카드");

        // get Intent package values
        if (savedInstanceState == null) {
            mTestWords = getIntent().getParcelableArrayListExtra(EXAM_WORDS);
            mTestType = getIntent().getIntExtra(TEST_TYPE, 0);
        } else {
            mTestWords = savedInstanceState.getParcelableArrayList(EXAM_WORDS);
            mTestType = savedInstanceState.getInt(TEST_TYPE);
        }

        // set view pager adapter
        mViewPager = (ViewPager)findViewById(R.id.activity_exam_pager);
        final FragmentManager manager = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentPagerAdapter(manager) {
            @Override
            public Fragment getItem(int position) {
                if (mTestType == SelectBookFragment.EXAM_FLASH_TYPE_MEM) {
                    return FlashCardFragment.createFragment(mTestWords.get(position),
                            FlashCardFragment.MEMORIZE_MODE);
                } else { // mTestType == SelectBookFragment.EXAM_FLASH_TYPE_TEST
                    return FlashCardFragment.createFragment(mTestWords.get(position),
                            FlashCardFragment.TEST_MODE);
                }
            }

            @Override
            public int getCount() {
                return mTestWords.size();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(EXAM_WORDS, mTestWords);
        outState.putInt(TEST_TYPE, mTestType);
        super.onSaveInstanceState(outState);
    }

    public static Intent newIntent(Context context, int testType, ArrayList<Word> words) {
        Intent intent = new Intent(context, FlashCardPagerActivity.class);
        intent.putParcelableArrayListExtra(EXAM_WORDS, words);
        intent.putExtra(TEST_TYPE, testType);
        return intent;
    }
}
