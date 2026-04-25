package com.mbd.cmsadmin.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mbd.cmsadmin.R;

/**
 * SplashActivity — Entry point of "The Academic Ledger"
 *
 * Logic:
 *   • On launch, wait SPLASH_DELAY ms while "Initializing Secure Portal…" is shown.
 *   • After the delay, check Firebase Authentication state:
 *       ▸ Admin already logged in  → navigate directly to HomeActivity (no login button shown)
 *       ▸ Not logged in            → reveal the "Login as Admin" button and "Request Access" link
 *
 * Dependencies (add to build.gradle :app):
 *   implementation 'com.google.firebase:firebase-auth:22.3.1'
 *   implementation 'com.google.android.material:material:1.11.0'
 *   implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
 */
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    // ── Config ──────────────────────────────────────────────────────────────
    /** Delay (ms) before auth check — long enough to feel intentional, short enough to not annoy */
    private static final long SPLASH_DELAY = 2000L;

    // ── Views ────────────────────────────────────────────────────────────────
    private MaterialButton btnLoginAdmin;
    private TextView tvRequestAccess;
    private TextView tvStatus;
    private android.widget.ProgressBar progressBar;

    // ── Firebase ─────────────────────────────────────────────────────────────
    private FirebaseAuth mAuth;

    // ────────────────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make the activity full-screen / edge-to-edge (matches the web design)
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );

        setContentView(R.layout.activity_splash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.splashRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Bind views
        btnLoginAdmin   = findViewById(R.id.btnLoginAdmin);
        tvRequestAccess = findViewById(R.id.tvRequestAccess);
        tvStatus        = findViewById(R.id.tvStatus);
        progressBar     = findViewById(R.id.progressBar);

        // Initialise Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Button listeners (registered now; visibility set later)
        btnLoginAdmin.setOnClickListener(v -> navigateToLogin());
        tvRequestAccess.setOnClickListener(v -> requestAccessCredentials());

        // Start the splash delay, then decide which path to take
        runSplashSequence();
    }

    // ────────────────────────────────────────────────────────────────────────
    /**
     * Animates the progress bar from 0 → 100 % over SPLASH_DELAY ms,
     * then checks the Firebase auth state.
     */
    private void runSplashSequence() {
        final Handler handler = new Handler(Looper.getMainLooper());
        final int steps        = 20;                        // number of progress updates
        final long interval    = SPLASH_DELAY / steps;     // ms between each update
        final int progressStep = 100 / steps;

        // Reset progress bar to 0
        progressBar.setProgress(0);

        Runnable progressRunnable = new Runnable() {
            int currentStep = 0;

            @Override
            public void run() {
                currentStep++;
                int newProgress = progressStep * currentStep;
                progressBar.setProgress(Math.min(newProgress, 100));

                if (currentStep < steps) {
                    handler.postDelayed(this, interval);
                } else {
                    // Progress complete — evaluate auth state
                    checkAuthAndProceed();
                }
            }
        };

        handler.postDelayed(progressRunnable, interval);
    }

    // ────────────────────────────────────────────────────────────────────────
    /**
     * Core routing logic:
     *   Logged-in admin  → HomeActivity  (splash never shows the button)
     *   No session       → Show the Login button
     */
    private void checkAuthAndProceed() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // ── Admin is already authenticated ──────────────────────────────
            // Optionally reload the token to ensure the session is still valid
            currentUser.reload().addOnCompleteListener(task -> {
                if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                    // Session valid → go straight to Home
                    navigateToHome();
                } else {
                    // Token expired / revoked → sign out and show login
                    mAuth.signOut();
                    showLoginButton();
                }
            });
        } else {
            // ── No active session → reveal login UI ─────────────────────────
            showLoginButton();
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    /** Make the Login button (and optional Request Access link) visible. */
    private void showLoginButton() {
        // Update status label
        tvStatus.setText("PORTAL READY");

        // Fade-in the button for a polished feel
        btnLoginAdmin.setAlpha(0f);
        btnLoginAdmin.setVisibility(View.VISIBLE);
        btnLoginAdmin.animate()
                .alpha(1f)
                .setDuration(400)
                .start();

        // Show the "Request Access" link with a slight delay
        tvRequestAccess.setAlpha(0f);
        tvRequestAccess.setVisibility(View.VISIBLE);
        tvRequestAccess.animate()
                .alpha(1f)
                .setStartDelay(150)
                .setDuration(400)
                .start();
    }

    // ────────────────────────────────────────────────────────────────────────
    /** Navigate to the main admin home screen and clear the back stack. */
    private void navigateToHome() {
        Intent intent = new Intent(SplashActivity.this, Main.class);
        // Clear back stack so pressing Back from Home doesn't return to Splash
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        // Smooth cross-fade transition
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    // ────────────────────────────────────────────────────────────────────────
    /** Navigate to the LoginActivity. */
    private void navigateToLogin() {
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    // ────────────────────────────────────────────────────────────────────────
    /** Placeholder for "Request Access Credentials" flow. */
    private void requestAccessCredentials() {
        // TODO: open a contact / request form, send an email intent, or navigate to
        //       a RequestAccessActivity. For now, show a simple toast.
        Toast.makeText(
                this,
                "Please contact the institution administrator for credentials.",
                Toast.LENGTH_LONG
        ).show();
    }

    // ────────────────────────────────────────────────────────────────────────
    /**
     * If the user returns to this screen (e.g., pressed Back from Login),
     * re-check auth state in case they just logged in.
     */
    @Override
    protected void onResume() {
        super.onResume();
        // Only re-check if the login button is not yet visible (i.e., we're mid-splash)
        // Avoid re-running the full sequence on every resume.
        if (btnLoginAdmin.getVisibility() != View.VISIBLE) {
            // Still in splash phase — nothing extra to do
            return;
        }
        // Button is already shown; if the user somehow authenticated externally, redirect
        if (mAuth.getCurrentUser() != null) {
            navigateToHome();
        }
    }
}