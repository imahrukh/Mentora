package com.fast.mentor;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SignUpActivity extends AppCompatActivity {

    private EditText emailInput, nameInput, passwordInput;
    private Button signupButton;
    private TextView loginRedirect;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Bind views
        emailInput    = findViewById(R.id.emailInput);
        nameInput     = findViewById(R.id.nameInput);
        passwordInput = findViewById(R.id.passwordInput);
        signupButton  = findViewById(R.id.signupButton);
        loginRedirect = findViewById(R.id.loginRedirect);

        // Sign Up logic
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email    = emailInput.getText().toString().trim();
                String name     = nameInput.getText().toString().trim();
                String password = passwordInput.getText().toString().trim();

                if (email.isEmpty() || name.isEmpty() || password.isEmpty()) {
                    Toast.makeText(SignUpActivity.this,
                            "Please fill in all fields.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (password.length() < 6) {
                    Toast.makeText(SignUpActivity.this,
                            "Password must be at least 6 characters.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create user
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Update profile with display name
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    UserProfileChangeRequest profileUpdates =
                                            new UserProfileChangeRequest.Builder()
                                                    .setDisplayName(name)
                                                    .build();
                                    user.updateProfile(profileUpdates);
                                }
                                Toast.makeText(SignUpActivity.this,
                                        "Registration successful!",
                                        Toast.LENGTH_SHORT).show();
                                // Redirect to Login or Dashboard
                                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(SignUpActivity.this,
                                        "Registration failed: " + task.getException().getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });

        // Redirect to Login
        loginRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
