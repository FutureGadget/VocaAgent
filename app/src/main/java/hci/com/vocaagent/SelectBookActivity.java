package hci.com.vocaagent;

import android.support.v4.app.Fragment;

/**
 * Created by dw on 2015-11-22.
 */
public class SelectBookActivity extends SingleFragmentActivity{
    @Override
    public Fragment createFragment() {
        return new SelectBookFragment();
    }
}
