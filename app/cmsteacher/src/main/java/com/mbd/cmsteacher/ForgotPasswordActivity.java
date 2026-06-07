package com.mbd.cmsteacher;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

/**
 * ForgotPasswordActivity — Credential Recovery for CMS Teacher
 *
 * Flow:
 *   1. Teacher enters their institutional email address
 *   2. Basic local validation (empty field, email format)
 *   3. Firebase sendPasswordResetEmail()
 *   4. Success → show confirmation message, disable button
 *   5. Failure → show specific inline error message
 *
 * Navigation:
 *   "Return to Login" arrow → finishes this activity (back to LoginActivity)
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    // ── Views ────────────────────────────────────────────────────────────
    private EditText    etEmail;
    private MaterialButton btnSendReset;
    private ProgressBar progressBar;
    private TextView    tvError;
    private TextView    tvSuccess;
    private View        btnBack;

    // ── Firebase ─────────────────────────────────────────────────────────
    private FirebaseAuth mAuth;

    // ─────────────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();

        bindViews();
        setListeners();
    }

    // ─────────────────────────────────────────────────────────────────────
    private void bindViews() {
        etEmail      = findViewById(R.id.etEmail);
        btnSendReset = findViewById(R.id.btnSendReset);
        progressBar  = findViewById(R.id.progressBar);
        tvError      = findViewById(R.id.tvError);
        tvSuccess    = findViewById(R.id.tvSuccess);
        btnBack      = findViewById(R.id.btnReturnToLogin);
    }

    // ─────────────────────────────────────────────────────────────────────
    private void setListeners() {
        btnSendReset.setOnClickListener(v -> attemptReset());

        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(
                    android.R.anim.slide_in_left,
                    android.R.anim.slide_out_right
            );
        });
    }

    // ─────────────────────────────────────────────────────────────────────
    /**
     * Validate the email field and fire the Firebase password reset.
     */
    private void attemptReset() {
        hideError();
        hideSuccess();
        dismissKeyboard();

        String email = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            showError("Please enter your institutional email address.");
            etEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Please enter a valid email address.");
            etEmail.requestFocus();
            return;
        }

        showLoading(true);

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        showSuccess("Reset link transmitted to " + email
                                + ". Please check your inbox.");
                        // Disable button so it cannot be spammed
                        btnSendReset.setEnabled(false);
                        btnSendReset.setAlpha(0.5f);
                    } else {
                        String msg = "Could not send reset email. Please check the address and try again.";
                        if (task.getException() != null
                                && task.getException().getMessage() != null
                                && task.getException().getMessage().contains("network")) {
                            msg = "Network error. Please check your internet connection.";
                        }
                        showError(msg);
                    }
                });
    }

    // ─────────────────────────────────────────────────────────────────────
    /** Show/hide the loading spinner and button. */
    private void showLoading(boolean loading) {
        if (loading) {
            btnSendReset.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
            btnSendReset.setVisibility(View.VISIBLE);
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        tvError.setText("");
        tvError.setVisibility(View.GONE);
    }

    private void showSuccess(String message) {
        tvSuccess.setText(message);
        tvSuccess.setVisibility(View.VISIBLE);
    }

    private void hideSuccess() {
        tvSuccess.setText("");
        tvSuccess.setVisibility(View.GONE);
    }

    // ─────────────────────────────────────────────────────────────────────
    private void dismissKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm =
                    (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right
        );
    }
}