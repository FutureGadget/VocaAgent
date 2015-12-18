package hci.com.vocaagent;

import android.support.v4.app.Fragment;

public class NoteManagerActivity extends NoneDrawerSingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return NoteManagerFragment.newInstance(NoteManagerFragment.NOTE_MANAGER_MODE);
    }
}
