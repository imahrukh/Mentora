package com.fast.mentor;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.fast.mentor.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

/**
 * Activity for changing user password
 */
public class ChangePasswordActivity extends AppCompatActivity {

    // UI components
    private TextInputLayout tilCurrentPassword, tilNewPassword, tilConfirmNewPassword;
    private TextInputEditText etCurrentPassword, etNewPassword, etConfirmNewPassword;
    private Button btnUpdatePassword;
    private ProgressBar progressBar;

    // Firebase
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Initialize UI components
        initializeViews();
        setupToolbar();
        setupListeners();
    }

    private void initializeViews() {
        tilCurrentPassword = findViewById(R.id.tilCurrentPassword);
        tilNewPassword = findViewById(R.id.tilNewPassword);
        tilConfirmNewPassword = findViewById(R.id.tilConfirmNewPassword);
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmNewPassword = findViewById(R.id.etConfirmNewPassword);
        btnUpdatePassword = findViewById(R.id.btnUpdatePassword);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void setupListeners() {
        // Text change listeners for validation feedback
        TextWatcher passwordWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Clear errors when user starts typing
                if (s.hashCode() == etCurrentPassword.getText().hashCode()) {
                    tilCurrentPassword.setError(null);
                } else if (s.hashCode() == etNewPassword.getText().hashCode()) {
                    tilNewPassword.setError(null);
                    validatePasswordMatch();
                } else if (s.hashCode() == etConfirmNewPassword.getText().hashCode()) {
                    tilConfirmNewPassword.setError(null);
                    validatePasswordMatch();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        etCurrentPassword.addTextChangedListener(passwordWatcher);
        etNewPassword.addTextChangedListener(passwordWatcher);
        etConfirmNewPassword.addTextChangedListener(passwordWatcher);

        // Update password button click
        btnUpdatePassword.setOnClickListener(v -> changePassword());
    }

    private void validatePasswordMatch() {
        String newPassword = Objects.requireNonNull(etNewPassword.getText()).toString().trim();
        String confirmPassword = Objects.requireNonNull(etConfirmNewPassword.getText()).toString().trim();

        if (!newPassword.isEmpty() && !confirmPassword.isEmpty() && !newPassword.equals(confirmPassword)) {
            tilConfirmNewPassword.setError(getString(R.string.passwords_dont_match));
        } else {
            tilConfirmNewPassword.setError(null);
        }
    }

    private boolean validateInputs() {
        String currentPassword = Objects.requireNonNull(etCurrentPassword.getText()).toString().trim();
        String newPassword = Objects.requireNonNull(etNewPassword.getText()).toString().trim();
        String confirmPassword = Objects.requireNonNull(etConfirmNewPassword.getText()).toString().trim();

        boolean isValid = true;

        if (currentPassword.isEmpty()) {
            tilCurrentPassword.setError("Current password is required");
            isValid = false;
        }

        if (newPassword.isEmpty()) {
            tilNewPassword.setError("New password is required");
            isValid = false;
        } else if (newPassword.length() < 6) {
            tilNewPassword.setError(getString(R.string.password_too_short));
            isValid = false;
        }

        if (confirmPassword.isEmpty()) {
            tilConfirmNewPassword.setError("Please confirm your new password");
            isValid = false;
        } else if (!newPassword.equals(confirmPassword)) {
            tilConfirmNewPassword.setError(getString(R.string.passwords_dont_match));
            isValid = false;
        }

        return isValid;
    }

    private void changePassword() {
        if (!validateInputs()) return;

        showLoading(true);

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            showLoading(false);
            return;
        }

        String email = user.getEmail();
        if (email == null) {
            Toast.makeText(this, "User email not available", Toast.LENGTH_SHORT).show();
            showLoading(false);
            return;
        }

        String currentPassword = Objects.requireNonNull(etCurrentPassword.getText()).toString().trim();
        String newPassword = Objects.requireNonNull(etNewPassword.getText()).toString().trim();

        // Re-authenticate user before changing password
        AuthCredential credential = EmailAuthProvider.getCredential(email, currentPassword);
        user.reauthenticate(credential)
                .addOnSuccessListener(aVoid -> 
                        user.updatePassword(newPassword)
                                .addOnSuccessListener(aVoid1 -> {
                                    Toast.makeText(ChangePasswordActivity.this, 
                                            R.string.password_changed, Toast.LENGTH_SHORT).show();
                                    showLoading(false);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(ChangePasswordActivity.this, 
                                            "Error changing password: " + e.getMessage(), 
                                            Toast.LENGTH_SHORT).show();
                                    showLoading(false);
                                }))
                .addOnFailureListener(e -> {
                    // Current password is incorrect
                    tilCurrentPassword.setError("Current password is incorrect");
                    showLoading(false);
                });
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            btnUpdatePassword.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            btnUpdatePassword.setEnabled(true);
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}