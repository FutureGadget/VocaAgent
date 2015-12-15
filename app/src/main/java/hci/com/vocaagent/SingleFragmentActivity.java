package hci.com.vocaagent;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

public abstract class SingleFragmentActivity extends AppCompatActivity {
    private Toolbar toolbar;

    DrawerLayout mDrawer;
    ActionBarDrawerToggle mDrawerToggle;
    NavigationView mNavigationView;

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
                else item.setChecked(true);

                mDrawer.closeDrawers();

                switch (item.getItemId()) {
                    case R.id.drawer_item_study:
                        SelectBookFragment fragment = new SelectBookFragment();
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, fragment)
                                .commit();
                        return true;
                    case R.id.drawer_item_edit:
                        NoteManagerFragment fragment2 = new NoteManagerFragment();
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, fragment2)
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
