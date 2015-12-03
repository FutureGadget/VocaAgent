package hci.com.vocaagent;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import java.util.List;

/**
 * Created by dw on 2015-11-22.
 */
public class ExamPagerActivity extends AppCompatActivity{
    private ViewPager mViewPager;
    private List<Word> mWords;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam_pager);
        mViewPager = (ViewPager)findViewById(R.id.activity_exam_pager);

        // Get words for exam.
        mWords = VocaLab.getVoca().getTestWords();
        // Exception : No words in the list.
        if (mWords.size() == 0) {
            finish();
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                // get a word
                Word word = mWords.get(position);
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
    }
}
