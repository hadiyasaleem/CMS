package com.mbd.cmsadmin.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.mbd.cmsadmin.R;
import com.mbd.cmsadmin.fragments.AttendanceFragment;
import com.mbd.cmsadmin.fragments.ExamsFragment;
import com.mbd.cmsadmin.fragments.HomeFragment;
import com.mbd.cmsadmin.fragments.OthersFragment;


public class Main extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FragmentManager fragmentManager;

    // Keep fragment instances to avoid re-creation on tab switch
    private HomeFragment homeFragment;
    private AttendanceFragment attendanceFragment;
    private ExamsFragment examsFragment;
    private OthersFragment othersFragment;

    // Currently displayed fragment
    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupActionBar();
        setupFragments();
        setupBottomNavigation();
    }

    /**
     * Configures the custom ActionBar with the app branding (logo + title).
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);

            // Inflate custom action bar layout
            View customView = getLayoutInflater().inflate(R.layout.custom_actionbar, null);

            ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
                    ActionBar.LayoutParams.MATCH_PARENT,
                    ActionBar.LayoutParams.MATCH_PARENT
            );
            actionBar.setCustomView(customView, lp);
        }
    }

    /**
     * Initialises all four fragments and adds them to the back stack.
     * Only the HomeFragment is visible initially; others are hidden.
     */
    private void setupFragments() {
        fragmentManager = getSupportFragmentManager();

        homeFragment       = new HomeFragment();
        attendanceFragment = new AttendanceFragment();
        examsFragment      = new ExamsFragment();
        othersFragment     = new OthersFragment();

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.fragment_container, othersFragment,     "OTHERS").hide(othersFragment);
        transaction.add(R.id.fragment_container, examsFragment,      "EXAMS").hide(examsFragment);
        transaction.add(R.id.fragment_container, attendanceFragment, "ATTENDANCE").hide(attendanceFragment);
        transaction.add(R.id.fragment_container, homeFragment,       "HOME");
        transaction.commit();

        activeFragment = homeFragment;
    }

    /**
     * Sets up BottomNavigationView item selection listener.
     */
    private void setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    switchFragment(homeFragment);
                    return true;
                } else if (itemId == R.id.nav_attendance) {
                    switchFragment(attendanceFragment);
                    return true;
                } else if (itemId == R.id.nav_exams) {
                    switchFragment(examsFragment);
                    return true;
                } else if (itemId == R.id.nav_others) {
                    switchFragment(othersFragment);
                    return true;
                }
                return false;
            }
        });

        // Highlight Home by default
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
    }

    /**
     * Hides the currently active fragment and shows the target fragment.
     *
     * @param targetFragment The fragment to display.
     */
    private void switchFragment(Fragment targetFragment) {
        if (targetFragment == activeFragment) return;

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.hide(activeFragment);
        transaction.show(targetFragment);
        transaction.commit();

        activeFragment = targetFragment;
    }

    /**
     * Intercept back press: if not on Home, navigate back to Home;
     * otherwise let the system handle it (exit the app).
     */
    @Override
    public void onBackPressed() {
        if (activeFragment != homeFragment) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        } else {
            super.onBackPressed();
        }
    }
}