package com.fast.mentor;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private String userId;

    private TextView tvName, tvEmail, tvPassword;
    private MaterialButton btnEditDetails, btnSignOut, btnDeleteAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();
        userId = user.getUid();

        initializeViews();
        loadUserData();
        setupListeners();
    }

    private void initializeViews() {
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPassword = findViewById(R.id.tvPassword);
        btnEditDetails = findViewById(R.id.btnEditDetails);
        btnSignOut = findViewById(R.id.btnSignOut);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
    }

    private void loadUserData() {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String email = documentSnapshot.getString("email");

                        tvName.setText("Name: " + name);
                        tvEmail.setText("Email: " + email);
                        tvPassword.setText("Password: ********");
                    }
                });
    }

    private void setupListeners() {
        btnEditDetails.setOnClickListener(v -> showEditOptionsDialog());
        btnSignOut.setOnClickListener(v -> showSignOutConfirmation());
        btnDeleteAccount.setOnClickListener(v -> initiateAccountDeletion());
    }

    private void showEditOptionsDialog() {
        String[] options = {"Name", "Email", "Password"};
        new MaterialAlertDialogBuilder(this)
                .setTitle("Edit Personal Details")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: showEditDialog("Name"); break;
                        case 1: showEditDialog("Email"); break;
                        case 2: showEditDialog("Password"); break;
                    }
                })
                .show();
    }

    private void showEditDialog(String field) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_field, null);
        TextInputLayout inputLayout = dialogView.findViewById(R.id.inputLayout);
        TextInputEditText inputField = dialogView.findViewById(R.id.inputField);

        inputLayout.setHint("New " + field);
        if (field.equals("Password")) inputField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Edit " + field)
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newValue = inputField.getText().toString().trim();
                    if (!newValue.isEmpty()) {
                        updateField(field, newValue);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateField(String field, String newValue) {
        Map<String, Object> updates = new HashMap<>();

        switch (field) {
            case "Name":
                updates.put("name", newValue);
                tvName.setText("Name: " + newValue);
                break;

            case "Email":
                user.updateEmail(newValue)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                updates.put("email", newValue);
                                tvEmail.setText("Email: " + newValue);
                                Toast.makeText(this, "Email updated", Toast.LENGTH_SHORT).show();
                            }
                        });
                break;

            case "Password":
                user.updatePassword(newValue)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(this, "Password updated", Toast.LENGTH_SHORT).show();
                            }
                        });
                break;
        }

        if (!updates.isEmpty()) {
            db.collection("users").document(userId).update(updates);
        }
    }

    private void showSignOutConfirmation() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    mAuth.signOut();
                    startActivity(new Intent(this, SplashActivity.class));
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void initiateAccountDeletion() {
        // Send verification email
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        showVerificationDialog();
                    }
                });
    }

    private void showVerificationDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_verify_code, null);
        TextInputEditText etCode = dialogView.findViewById(R.id.etCode);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Verify Account Deletion")
                .setMessage("A verification code has been sent to your email")
                .setView(dialogView)
                .setPositiveButton("Verify", (dialog, which) -> {
                    String code = etCode.getText().toString().trim();
                    if (!code.isEmpty()) {
                        verifyAndDeleteAccount(code);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void verifyAndDeleteAccount(String code) {
        // Implement your code verification logic here
        // For production use Firebase Auth's built-in verification system

        // For this example, we'll assume code is valid
        db.collection("users").document(userId).delete()
                .addOnSuccessListener(aVoid -> {
                    user.delete()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(this, "Account permanently deleted", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(this, SplashActivity.class));
                                    finish();
                                }
                            });
                });
    }
}