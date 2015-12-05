package hci.com.vocaagent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

// Activity to modify the book contents. (list of words)
public class BookActivity extends SingleFragmentActivity {
    private static final String EXTRA_BOOK_ID = "hci.com.vocaagent.book_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Fragment createFragment() {
        return BookFragment.newInstance(getIntent().getIntExtra(EXTRA_BOOK_ID, -1));
    }

    public static Intent newIntent(Context packageContext, int bid) {
        Intent intent = new Intent(packageContext, BookActivity.class);
        intent.putExtra(EXTRA_BOOK_ID, bid);
        return intent;
    }
}
