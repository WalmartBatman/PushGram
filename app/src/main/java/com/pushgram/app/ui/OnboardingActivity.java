package com.pushgram.app.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.pushgram.app.R;

/**
 * Change #2 — First-launch onboarding.
 * Shown exactly once (SharedPreferences "onboarding_done" flag).
 * Offers Create Account (email + username + password) or Login.
 */
public class OnboardingActivity extends AppCompatActivity {

    private static final String PREFS      = "pushgram_prefs";
    private static final String KEY_DONE   = "onboarding_done";
    private static final String KEY_EMAIL  = "user_email";
    private static final String KEY_USER   = "username";
    private static final String KEY_PASS   = "password_hash";

    private SharedPreferences prefs;

    // Views
    private LinearLayout layoutLanding, layoutCreate, layoutLogin, layoutVerify;
    private EditText etCreateEmail, etCreateUser, etCreatePass;
    private EditText etLoginUser, etLoginPass;
    private EditText etOtp;
    private TextView  tvError;
    private Button   btnCreate, btnLogin, btnSubmitCreate, btnSubmitLogin, btnVerifyOtp;

    private String pendingEmail;
    private String mockOtp; // simulated OTP for demo

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);

        // Skip onboarding if already done
        if (prefs.getBoolean(KEY_DONE, false)) {
            launchMain();
            return;
        }

        setContentView(R.layout.activity_onboarding);
        bindViews();
        showLanding();
    }

    private void bindViews() {
        layoutLanding      = findViewById(R.id.layoutLanding);
        layoutCreate       = findViewById(R.id.layoutCreate);
        layoutLogin        = findViewById(R.id.layoutLogin);
        layoutVerify       = findViewById(R.id.layoutVerify);

        btnCreate          = findViewById(R.id.btnCreate);
        btnLogin           = findViewById(R.id.btnLogin);
        btnSubmitCreate    = findViewById(R.id.btnSubmitCreate);
        btnSubmitLogin     = findViewById(R.id.btnSubmitLogin);
        btnVerifyOtp       = findViewById(R.id.btnVerifyOtp);
        tvError            = findViewById(R.id.tvError);

        etCreateEmail      = findViewById(R.id.etCreateEmail);
        etCreateUser       = findViewById(R.id.etCreateUser);
        etCreatePass       = findViewById(R.id.etCreatePass);
        etLoginUser        = findViewById(R.id.etLoginUser);
        etLoginPass        = findViewById(R.id.etLoginPass);
        etOtp              = findViewById(R.id.etOtp);

        btnCreate.setOnClickListener(v -> showCreate());
        btnLogin.setOnClickListener(v  -> showLoginForm());

        btnSubmitCreate.setOnClickListener(v -> handleCreate());
        btnSubmitLogin.setOnClickListener(v  -> handleLogin());
        btnVerifyOtp.setOnClickListener(v    -> handleVerifyOtp());

        // Back arrows
        findViewById(R.id.btnBackFromCreate).setOnClickListener(v -> showLanding());
        findViewById(R.id.btnBackFromLogin).setOnClickListener(v  -> showLanding());
        findViewById(R.id.btnBackFromVerify).setOnClickListener(v -> showCreate());
    }

    // ── Screen transitions ──────────────────────────────────────────────────

    private void showLanding() {
        show(layoutLanding); hide(layoutCreate, layoutLogin, layoutVerify);
    }
    private void showCreate() {
        show(layoutCreate); hide(layoutLanding, layoutLogin, layoutVerify);
        clearError();
    }
    private void showLoginForm() {
        show(layoutLogin); hide(layoutLanding, layoutCreate, layoutVerify);
        clearError();
    }
    private void showVerify() {
        show(layoutVerify); hide(layoutLanding, layoutCreate, layoutLogin);
        clearError();
    }

    // ── Handlers ────────────────────────────────────────────────────────────

    private void handleCreate() {
        String email = etCreateEmail.getText().toString().trim();
        String user  = etCreateUser.getText().toString().trim();
        String pass  = etCreatePass.getText().toString();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            setError("Enter a valid email address."); return;
        }
        if (user.length() < 3) {
            setError("Username must be at least 3 characters."); return;
        }
        if (pass.length() < 6) {
            setError("Password must be at least 6 characters."); return;
        }

        pendingEmail = email;
        mockOtp      = String.valueOf(100000 + (int)(Math.random() * 900000));

        // Save pending account details
        prefs.edit()
             .putString(KEY_EMAIL, email)
             .putString(KEY_USER,  user)
             .putString(KEY_PASS,  String.valueOf(pass.hashCode()))
             .apply();

        // Simulate sending OTP
        Toast.makeText(this,
            "Verification code sent to " + email + "\n(Demo OTP: " + mockOtp + ")",
            Toast.LENGTH_LONG).show();

        showVerify();
    }

    private void handleVerifyOtp() {
        String entered = etOtp.getText().toString().trim();
        if (entered.equals(mockOtp)) {
            finishOnboarding();
        } else {
            setError("Incorrect code. Please try again.");
        }
    }

    private void handleLogin() {
        String user = etLoginUser.getText().toString().trim();
        String pass = etLoginPass.getText().toString();

        if (user.isEmpty() || pass.isEmpty()) {
            setError("Enter your username and password."); return;
        }

        // Validate against stored credentials
        String savedUser = prefs.getString(KEY_USER,  null);
        String savedHash = prefs.getString(KEY_PASS,  null);

        if (savedUser == null) {
            // No account yet — still allow login (first-time convenience)
            prefs.edit()
                 .putString(KEY_USER, user)
                 .putString(KEY_PASS, String.valueOf(pass.hashCode()))
                 .apply();
            finishOnboarding();
            return;
        }

        boolean userMatch = user.equalsIgnoreCase(savedUser) ||
                            user.equalsIgnoreCase(prefs.getString(KEY_EMAIL, ""));
        boolean passMatch = String.valueOf(pass.hashCode()).equals(savedHash);

        if (userMatch && passMatch) {
            finishOnboarding();
        } else {
            setError("Incorrect username or password.");
        }
    }

    private void finishOnboarding() {
        prefs.edit().putBoolean(KEY_DONE, true).apply();
        Toast.makeText(this, "Welcome to PushGram! 💪", Toast.LENGTH_SHORT).show();
        launchMain();
    }

    private void launchMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private void setError(String msg) {
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
    }
    private void clearError() { tvError.setVisibility(View.GONE); }

    private void show(View v)          { v.setVisibility(View.VISIBLE); }
    private void hide(View... views)   { for (View v : views) v.setVisibility(View.GONE); }
}
