package hci.com.vocaagent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by dw on 2015-11-21.
 */

// Activity to modify the book contents. (list of words)
public class BookActivity extends SingleFragmentActivity{
    private static String EXTRA_BOOK_ID = "hci.com.vocaagent.book_id";
    private int mBookId;
    @Override
    protected void onCreate(Bundle savedInstnaceState) {
        super.onCreate(savedInstnaceState);
        mBookId = getIntent().getIntExtra(EXTRA_BOOK_ID, -1);
    }
    @Override
    public Fragment createFragment() {
        return BookFragment.newInstance(mBookId);
    }
    public static Intent newIntent(Context packageContext, int bid) {
        Intent intent = new Intent(packageContext, BookActivity.class);
        intent.putExtra(EXTRA_BOOK_ID, bid);
        return intent;
    }
}
