package com.fast.mentor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fast.mentor.MainActivity;
import com.fast.mentor.R;
import com.fast.mentor.models.UserProfile;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Activity for user registration
 */
public class SignupActivity extends AppCompatActivity {

    private TextInputEditText etFullName, etEmail, etPassword, etConfirmPassword;
    private Button btnSignup, btnLogin;
    private View progressBar;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Initialize views
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignup = findViewById(R.id.btnSignup);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);

        // Set click listeners
        btnSignup.setOnClickListener(v -> registerUser());
        btnLogin.setOnClickListener(v -> navigateToLogin());
    }

    private void registerUser() {
        String fullName = Objects.requireNonNull(etFullName.getText()).toString().trim();
        String email = Objects.requireNonNull(etEmail.getText()).toString().trim();
        String password = Objects.requireNonNull(etPassword.getText()).toString().trim();
        String confirmPassword = Objects.requireNonNull(etConfirmPassword.getText()).toString().trim();

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords don't match");
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return;
        }

        showLoading(true);

        // Create user with email and password
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    // Set user display name
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(fullName)
                                .build();

                        user.updateProfile(profileUpdates)
                                .addOnSuccessListener(aVoid -> createUserProfile(user, fullName, email))
                                .addOnFailureListener(e -> {
                                    Toast.makeText(SignupActivity.this, 
                                            "Error updating profile: " + e.getMessage(), 
                                            Toast.LENGTH_SHORT).show();
                                    showLoading(false);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SignupActivity.this, 
                            "Registration failed: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    showLoading(false);
                });
    }

    private void createUserProfile(FirebaseUser user, String fullName, String email) {
        String username = email.split("@")[0];
        String userId = user.getUid();

        Map<String, Object> profileData = new HashMap<>();
        profileData.put("fullName", fullName);
        profileData.put("username", username);
        profileData.put("email", email);
        profileData.put("bio", "");
        profileData.put("profilePictureUrl", "");
        profileData.put("preferredLanguage", "English");
        profileData.put("emailNotifications", true);
        profileData.put("pushNotifications", true);
        profileData.put("interests", new String[]{});
        profileData.put("coursesCompleted", 0);
        profileData.put("hoursSpent", 0);
        profileData.put("certificatesEarned", 0);

        firestore.collection("userProfiles").document(userId)
                .set(profileData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(SignupActivity.this, 
                            "Registration successful", Toast.LENGTH_SHORT).show();
                    navigateToMain();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SignupActivity.this, 
                            "Error creating profile: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    showLoading(false);
                });
    }

    private void navigateToLogin() {
        finish();
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnSignup.setEnabled(false);
            btnLogin.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnSignup.setEnabled(true);
            btnLogin.setEnabled(true);
        }
    }
}