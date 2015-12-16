package hci.com.vocaagent;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public abstract class NoneDrawerSingleFragmentActivity extends AppCompatActivity {
    private Toolbar mToolbar;

    protected abstract Fragment createFragment();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_non_drawer);
        mToolbar = (Toolbar)findViewById(R.id.non_drawer_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FragmentManager fm = getSupportFragmentManager();
        // fragments are located by their container id.
        Fragment fragment = fm.findFragmentById(R.id.non_drawer_container);

        if (fragment == null) {
            fragment = createFragment();
            fm.beginTransaction()
                    .add(R.id.non_drawer_container, fragment)
                    .commit();
        }
    }
}
