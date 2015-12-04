package hci.com.vocaagent;

import android.support.v4.app.Fragment;

public class SelectBookActivity extends SingleFragmentActivity{
    @Override
    public Fragment createFragment() {
        return new SelectBookFragment();
    }
}
