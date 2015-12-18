package hci.com.vocaagent;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.DecimalFormat;

public abstract class SingleFragmentActivity extends AppCompatActivity {
    private Toolbar toolbar;

    DrawerLayout mDrawer;
    ActionBarDrawerToggle mDrawerToggle;
    NavigationView mNavigationView;

    private static final String DIALOG_IMPORT = "DIALOG_IMPORT";
    private static final String DIALOG_EXPORT = "DIALOG_EXPORT";

    protected abstract Fragment createFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_fragment);

        // set toolbar
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                if (item.isChecked()) item.setChecked(false);

                mDrawer.closeDrawers();

                switch (item.getItemId()) {
                    case R.id.drawer_item_study:
                        SelectBookFragment fragment = new SelectBookFragment();
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, fragment)
                                .commit();
                        return true;
                    case R.id.drawer_item_edit:
                        NoteManagerFragment managerFragment = NoteManagerFragment.newInstance(NoteManagerFragment.NOTE_MANAGER_MODE);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, managerFragment)
                                .commit();
                        return true;
                    case R.id.drawer_item_import:
                        ExternalStorageListFragment importList = new ExternalStorageListFragment();
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, importList)
                                .commit();
                        return true;
                    case R.id.drawer_item_export:
                        NoteManagerFragment exportFragment = NoteManagerFragment.newInstance(NoteManagerFragment.EXPORT_BOOK_MODE);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, exportFragment)
                                .commit();
                        return true;
                    default:
                        return true;
                }
            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.openDrawer, R.string.closeDrawer) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                Menu menu = mNavigationView.getMenu();

                // streak view
                MenuItem view = menu.findItem(R.id.drawer_item_streak);
                LinearLayout test = (LinearLayout)view.getActionView();
                TextView text = (TextView) test.findViewById(R.id.drawer_streak_text_view);

                VocaLab.getVoca(SingleFragmentActivity.this).updateMetaInfo(0,0,0);

                Meta meta = VocaLab.getVoca(SingleFragmentActivity.this).getLatestMeta();
                String streakStr = getString(R.string.meta_streak_days, meta.getStreak());
                text.setText(streakStr);

                // ratio view
                view = menu.findItem(R.id.drawer_item_ratio);
                test = (LinearLayout)view.getActionView();
                text = (TextView)test.findViewById(R.id.drawer_ratio_text_view);
                double ratio = VocaLab.getVoca(SingleFragmentActivity.this).getCorrectRatio();
                DecimalFormat df = new DecimalFormat("#.##%");
                String ratioStr = getString(R.string.meta_correct_ratio, df.format(ratio));
                text.setText(ratioStr);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };

        mDrawer.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        FragmentManager fm = getSupportFragmentManager();
        // fragments are located by their container id.
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            fragment = createFragment();
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
    }
}
