package com.mbd.cmsteacher;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.mbd.cmsteacher.fragments.AttendanceFragment;
import com.mbd.cmsteacher.fragments.ExamsFragment;
import com.mbd.cmsteacher.fragments.HomeFragment;
import com.mbd.cmsteacher.fragments.MenuFragment;
import com.mbd.cmsteacher.fragments.ScheduleFragment;

/**
 * Main — hosts the 5-tab BottomNavigationView for CMS Teacher.
 *
 * Tabs (matching the HTML reference):
 *   Home · Attendance · Exams · Schedule · Menu
 *
 * Uses hide/show pattern (not replace) so fragment state is preserved
 * across tab switches — same approach as CMS Admin.
 */
public class Main extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FragmentManager fragmentManager;

    // Fragment instances — kept alive for the activity lifetime
    private HomeFragment       homeFragment;
    private AttendanceFragment attendanceFragment;
    private ExamsFragment      examsFragment;
    private ScheduleFragment   scheduleFragment;
    private MenuFragment       menuFragment;

    private Fragment activeFragment;

    // ─────────────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupFragments();
        setupBottomNavigation();
    }

    // ─────────────────────────────────────────────────────────────────────
    /**
     * Add all five fragments in one transaction.
     * Only HomeFragment is visible initially; the rest are hidden.
     */
    private void setupFragments() {
        fragmentManager = getSupportFragmentManager();

        homeFragment       = new HomeFragment();
        attendanceFragment = new AttendanceFragment();
        examsFragment      = new ExamsFragment();
        scheduleFragment   = new ScheduleFragment();
        menuFragment       = new MenuFragment();

        FragmentTransaction tx = fragmentManager.beginTransaction();
        tx.add(R.id.fragment_container, menuFragment,       "MENU")      .hide(menuFragment);
        tx.add(R.id.fragment_container, scheduleFragment,   "SCHEDULE")  .hide(scheduleFragment);
        tx.add(R.id.fragment_container, examsFragment,      "EXAMS")     .hide(examsFragment);
        tx.add(R.id.fragment_container, attendanceFragment, "ATTENDANCE").hide(attendanceFragment);
        tx.add(R.id.fragment_container, homeFragment,       "HOME");
        tx.commit();

        activeFragment = homeFragment;
    }

    // ─────────────────────────────────────────────────────────────────────
    private void setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                switchFragment(homeFragment);
                return true;
            } else if (id == R.id.nav_attendance) {
                switchFragment(attendanceFragment);
                return true;
            } else if (id == R.id.nav_exams) {
                switchFragment(examsFragment);
                return true;
            } else if (id == R.id.nav_schedule) {
                switchFragment(scheduleFragment);
                return true;
            } else if (id == R.id.nav_menu) {
                switchFragment(menuFragment);
                return true;
            }
            return false;
        });

        bottomNavigationView.setSelectedItemId(R.id.nav_home);
    }

    // ─────────────────────────────────────────────────────────────────────
    /**
     * Hide the current fragment and show the target with a cross-fade.
     */
    private void switchFragment(Fragment target) {
        if (target == activeFragment) return;

        fragmentManager.beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .hide(activeFragment)
                .show(target)
                .commit();

        activeFragment = target;
    }

    // ─────────────────────────────────────────────────────────────────────
    /**
     * Back press: navigate to Home if not already there;
     * otherwise let the system exit the app.
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