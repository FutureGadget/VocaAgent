package hci.com.vocaagent;

import android.support.v4.app.Fragment;

public class ListNotesActivity extends SingleFragmentActivity {
    @Override
    public Fragment createFragment() {
        return new NoteManagerFragment();
    }
}
