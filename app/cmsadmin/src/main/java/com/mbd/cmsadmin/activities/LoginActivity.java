package com.mbd.cmsadmin.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.mbd.cmsadmin.R;

/**
 * LoginActivity — Firebase email/password authentication
 * Package: com.mbd.cmsstudent
 *
 * Flow:
 *   1. User enters email + password
 *   2. Basic local validation (empty fields, email format)
 *   3. Firebase signInWithEmailAndPassword()
 *   4. Success → HomeActivity (clear back stack)
 *   5. Failure → show specific error message inline
 *
 * Also handles:
 *   - Password visibility toggle
 *   - "Forgot Password?" → Firebase sendPasswordResetEmail()
 *   - Loading state (button hidden, spinner shown)
 */
public class LoginActivity extends AppCompatActivity {

    // ── Views ────────────────────────────────────────────────────────────
    private EditText etEmail, etPassword;
    private ImageView ivPasswordToggle;
    private MaterialButton btnLogin;
    private ProgressBar progressLogin;
    private TextView tvError, tvForgotPassword;

    // ── Firebase ─────────────────────────────────────────────────────────
    private FirebaseAuth mAuth;

    // ── State ─────────────────────────────────────────────────────────────
    private boolean isPasswordVisible = false;

    // ─────────────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // If already logged in, skip straight to Home
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            goToMain();
            return;
        }

        bindViews();
        setListeners();
    }

    // ─────────────────────────────────────────────────────────────────────
    private void bindViews() {
        etEmail           = findViewById(R.id.etEmail);
        etPassword        = findViewById(R.id.etPassword);
        ivPasswordToggle  = findViewById(R.id.ivPasswordToggle);
        btnLogin          = findViewById(R.id.btnLogin);
        progressLogin     = findViewById(R.id.progressLogin);
        tvError           = findViewById(R.id.tvError);
        tvForgotPassword  = findViewById(R.id.tvForgotPassword);
    }

    // ─────────────────────────────────────────────────────────────────────
    private void setListeners() {

        // ── Login button ──────────────────────────────────────────────────
        btnLogin.setOnClickListener(v -> attemptLogin());

        // ── Password visibility toggle ────────────────────────────────────
        ivPasswordToggle.setOnClickListener(v -> togglePasswordVisibility());

        // ── Forgot password ───────────────────────────────────────────────
        tvForgotPassword.setOnClickListener(v -> handleForgotPassword());
    }

    // ─────────────────────────────────────────────────────────────────────
    /**
     * Validate inputs then call Firebase signIn.
     */
    private void attemptLogin() {
        hideError();
        dismissKeyboard();

        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // ── Local validation ──────────────────────────────────────────────
        if (TextUtils.isEmpty(email)) {
            showError("Please enter your email address.");
            etEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Please enter a valid email address.");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            showError("Please enter your password.");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            showError("Password must be at least 6 characters.");
            etPassword.requestFocus();
            return;
        }

        // ── Firebase sign-in ──────────────────────────────────────────────
        showLoading(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    showLoading(false);

                    if (task.isSuccessful()) {
                        // ── SUCCESS ──────────────────────────────────────
                        goToMain();

                    } else {
                        // ── FAILURE — show specific message ───────────────
                        String errorMsg = getFirebaseErrorMessage(task.getException());
                        showError(errorMsg);
                    }
                });
    }

    // ─────────────────────────────────────────────────────────────────────
    /**
     * Map Firebase exceptions to user-friendly messages.
     */
    private String getFirebaseErrorMessage(Exception exception) {
        if (exception instanceof FirebaseAuthInvalidUserException) {
            String errorCode = ((FirebaseAuthInvalidUserException) exception).getErrorCode();
            if ("ERROR_USER_NOT_FOUND".equals(errorCode)) {
                return "No account found with this email address.";
            } else if ("ERROR_USER_DISABLED".equals(errorCode)) {
                return "This account has been disabled. Contact your administrator.";
            }
        }

        if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            String errorCode = ((FirebaseAuthInvalidCredentialsException) exception).getErrorCode();
            if ("ERROR_WRONG_PASSWORD".equals(errorCode)) {
                return "Incorrect password. Please try again.";
            } else if ("ERROR_INVALID_EMAIL".equals(errorCode)) {
                return "The email address format is invalid.";
            } else if ("ERROR_INVALID_CREDENTIAL".equals(errorCode)) {
                return "Invalid credentials. Please check your email and password.";
            }
        }

        // Network or unknown error
        if (exception != null && exception.getMessage() != null) {
            if (exception.getMessage().contains("network")) {
                return "Network error. Please check your internet connection.";
            }
        }

        return "Authentication failed. Please try again.";
    }

    // ─────────────────────────────────────────────────────────────────────
    /**
     * Toggle password text between hidden (dots) and visible (plain text).
     */
    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;

        if (isPasswordVisible) {
            // Show password
            etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            ivPasswordToggle.setImageResource(R.drawable.ic_visibility);
        } else {
            // Hide password
            etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            ivPasswordToggle.setImageResource(R.drawable.ic_visibility_off);
        }

        // Keep cursor at end of text after toggling
        etPassword.setSelection(etPassword.getText().length());
    }

    // ─────────────────────────────────────────────────────────────────────
    /**
     * Send a password reset email via Firebase.
     */
    private void handleForgotPassword() {
        String email = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            showError("Enter your email address above, then tap 'Forgot Password?'");
            etEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Please enter a valid email address first.");
            etEmail.requestFocus();
            return;
        }

        showLoading(true);

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(
                                LoginActivity.this,
                                "Password reset email sent to " + email,
                                Toast.LENGTH_LONG
                        ).show();
                    } else {
                        showError("Could not send reset email. Please check the email address.");
                    }
                });
    }

    // ─────────────────────────────────────────────────────────────────────
    /** Navigate to HomeActivity and clear back stack. */
    private void goToMain() {
        Intent intent = new Intent(LoginActivity.this, Main.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    // ─────────────────────────────────────────────────────────────────────
    /** Show/hide loading state: spinner visible, button hidden. */
    private void showLoading(boolean loading) {
        if (loading) {
            btnLogin.setVisibility(View.INVISIBLE);
            progressLogin.setVisibility(View.VISIBLE);
        } else {
            progressLogin.setVisibility(View.GONE);
            btnLogin.setVisibility(View.VISIBLE);
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    /** Display an inline error message below the password field. */
    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    /** Clear the inline error message. */
    private void hideError() {
        tvError.setText("");
        tvError.setVisibility(View.GONE);
    }

    // ─────────────────────────────────────────────────────────────────────
    /** Dismiss the soft keyboard. */
    private void dismissKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm =
                    (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    /**
     * Prevent going back to the splash screen once on the login page.
     * The user must either log in or close the app.
     */
    @Override
    public void onBackPressed() {
        // Optionally: finishAffinity() to close the whole app
        // or just super.onBackPressed() to allow back to Splash
        super.onBackPressed();
    }
}