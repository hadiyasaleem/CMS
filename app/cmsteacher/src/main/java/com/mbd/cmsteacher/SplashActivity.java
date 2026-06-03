package com.mbd.cmsteacher;

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

/**
 * SplashActivity — Entry point of CMS Teacher
 *
 * Logic:
 *   • On launch, wait SPLASH_DELAY ms while "Initializing Secure Portal…" is shown.
 *   • After the delay, check Firebase Authentication state:
 *       ▸ Teacher already logged in  → navigate directly to Main (no login button shown)
 *       ▸ Not logged in              → reveal the "Login as Teacher" button and "Request Access" link
 *
 * Dependencies (add to build.gradle :cmsteacher):
 *   implementation 'com.google.firebase:firebase-auth'
 *   implementation 'com.google.android.material:material'
 *   implementation 'androidx.constraintlayout:constraintlayout'
 */
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    // ── Config ──────────────────────────────────────────────────────────────
    /** Delay (ms) before auth check */
    private static final long SPLASH_DELAY = 2000L;

    // ── Views ────────────────────────────────────────────────────────────────
    private MaterialButton btnLoginTeacher;
    private TextView tvRequestAccess;
    private TextView tvStatus;
    private android.widget.ProgressBar progressBar;

    // ── Firebase ─────────────────────────────────────────────────────────────
    private FirebaseAuth mAuth;

    // ────────────────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Edge-to-edge
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
        btnLoginTeacher = findViewById(R.id.btnLoginTeacher);
        tvRequestAccess = findViewById(R.id.tvRequestAccess);
        tvStatus        = findViewById(R.id.tvStatus);
        progressBar     = findViewById(R.id.progressBar);

        // Initialise Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Button listeners (registered now; visibility set later)
        btnLoginTeacher.setOnClickListener(v -> navigateToLogin());
        tvRequestAccess.setOnClickListener(v -> requestAccessCredentials());

        // Start splash sequence
        runSplashSequence();
    }

    // ────────────────────────────────────────────────────────────────────────
    /**
     * Animates the progress bar from 0 → 100% over SPLASH_DELAY ms,
     * then checks the Firebase auth state.
     */
    private void runSplashSequence() {
        final Handler handler    = new Handler(Looper.getMainLooper());
        final int steps          = 20;
        final long interval      = SPLASH_DELAY / steps;
        final int progressStep   = 100 / steps;

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
                    checkAuthAndProceed();
                }
            }
        };

        handler.postDelayed(progressRunnable, interval);
    }

    // ────────────────────────────────────────────────────────────────────────
    /**
     * Core routing logic:
     *   Logged-in teacher → Main  (splash never shows the button)
     *   No session        → Show the Login button
     */
    private void checkAuthAndProceed() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            currentUser.reload().addOnCompleteListener(task -> {
                if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                    navigateToMain();
                } else {
                    mAuth.signOut();
                    showLoginButton();
                }
            });
        } else {
            showLoginButton();
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    /** Make the Login button (and Request Access link) visible with fade-in. */
    private void showLoginButton() {
        tvStatus.setText("PORTAL READY");

        btnLoginTeacher.setAlpha(0f);
        btnLoginTeacher.setVisibility(View.VISIBLE);
        btnLoginTeacher.animate()
                .alpha(1f)
                .setDuration(400)
                .start();

        tvRequestAccess.setAlpha(0f);
        tvRequestAccess.setVisibility(View.VISIBLE);
        tvRequestAccess.animate()
                .alpha(1f)
                .setStartDelay(150)
                .setDuration(400)
                .start();
    }

    // ────────────────────────────────────────────────────────────────────────
    /** Navigate to Main and clear back stack. */
    private void navigateToMain() {
        Intent intent = new Intent(SplashActivity.this, Main.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
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
        Toast.makeText(
                this,
                "Please contact the institution administrator for credentials.",
                Toast.LENGTH_LONG
        ).show();
    }

    // ────────────────────────────────────────────────────────────────────────
    @Override
    protected void onResume() {
        super.onResume();
        if (btnLoginTeacher.getVisibility() != View.VISIBLE) {
            return;
        }
        if (mAuth.getCurrentUser() != null) {
            navigateToMain();
        }
    }
}