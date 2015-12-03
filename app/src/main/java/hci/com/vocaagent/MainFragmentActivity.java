package hci.com.vocaagent;

import android.support.v4.app.Fragment;

/**
 * Created by dw on 2015-11-20.
 */
public class MainFragmentActivity extends SingleFragmentActivity {
    @Override
    public Fragment createFragment() {
        return new MainFragment();
    }
}
