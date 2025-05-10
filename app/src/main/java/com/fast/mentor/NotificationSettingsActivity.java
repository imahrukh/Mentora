package com.fast.mentor;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.fast.mentor.R;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Activity for managing notification preferences
 */
public class NotificationSettingsActivity extends AppCompatActivity {

    // UI components
    private SwitchMaterial switchPushNotifications;
    private SwitchMaterial switchEmailNotifications;
    private SwitchMaterial switchCourseUpdates;
    private SwitchMaterial switchNewLessons;
    private SwitchMaterial switchDeadlines;
    private SwitchMaterial switchDiscounts;
    private SwitchMaterial switchNewCourseRecommendations;
    private SwitchMaterial switchEventsWebinars;
    private Button btnSaveNotificationSettings;
    private View progressBar;

    // Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    // Store original settings to detect changes
    private Map<String, Boolean> originalSettings = new HashMap<>();
    private boolean hasChanges = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Initialize UI components
        initializeViews();
        setupToolbar();
        setupListeners();

        // Load notification settings
        loadNotificationSettings();
    }

    private void initializeViews() {
        switchPushNotifications = findViewById(R.id.switchPushNotifications);
        switchEmailNotifications = findViewById(R.id.switchEmailNotifications);
        switchCourseUpdates = findViewById(R.id.switchCourseUpdates);
        switchNewLessons = findViewById(R.id.switchNewLessons);
        switchDeadlines = findViewById(R.id.switchDeadlines);
        switchDiscounts = findViewById(R.id.switchDiscounts);
        switchNewCourseRecommendations = findViewById(R.id.switchNewCourseRecommendations);
        switchEventsWebinars = findViewById(R.id.switchEventsWebinars);
        btnSaveNotificationSettings = findViewById(R.id.btnSaveNotificationSettings);
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
        // Set change listeners for switches
        SwitchMaterial[] switches = {
                switchPushNotifications, switchEmailNotifications, 
                switchCourseUpdates, switchNewLessons, switchDeadlines,
                switchDiscounts, switchNewCourseRecommendations, switchEventsWebinars
        };

        for (SwitchMaterial switchView : switches) {
            switchView.setOnCheckedChangeListener((buttonView, isChecked) -> hasChanges = true);
        }

        // Save button listener
        btnSaveNotificationSettings.setOnClickListener(v -> saveNotificationSettings());
    }

    private void loadNotificationSettings() {
        showLoading(true);

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String userId = currentUser.getUid();
        firestore.collection("userNotificationSettings").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> data = documentSnapshot.getData();
                        if (data != null) {
                            // Load general notifications
                            switchPushNotifications.setChecked(getBoolean(data, "pushNotifications", true));
                            switchEmailNotifications.setChecked(getBoolean(data, "emailNotifications", true));
                            
                            // Load course notifications
                            switchCourseUpdates.setChecked(getBoolean(data, "courseUpdates", true));
                            switchNewLessons.setChecked(getBoolean(data, "newLessons", true));
                            switchDeadlines.setChecked(getBoolean(data, "deadlines", true));
                            
                            // Load promotional notifications
                            switchDiscounts.setChecked(getBoolean(data, "discounts", true));
                            switchNewCourseRecommendations.setChecked(getBoolean(data, "newCourseRecommendations", true));
                            switchEventsWebinars.setChecked(getBoolean(data, "eventsWebinars", true));
                            
                            // Store original settings
                            storeOriginalSettings();
                        }
                    } else {
                        // Create default settings
                        storeOriginalSettings();
                    }
                    showLoading(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(NotificationSettingsActivity.this, 
                            "Error loading notification settings: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    showLoading(false);
                });
    }

    private boolean getBoolean(Map<String, Object> data, String key, boolean defaultValue) {
        if (data.containsKey(key)) {
            Object value = data.get(key);
            if (value instanceof Boolean) {
                return (Boolean) value;
            }
        }
        return defaultValue;
    }

    private void storeOriginalSettings() {
        originalSettings.put("pushNotifications", switchPushNotifications.isChecked());
        originalSettings.put("emailNotifications", switchEmailNotifications.isChecked());
        originalSettings.put("courseUpdates", switchCourseUpdates.isChecked());
        originalSettings.put("newLessons", switchNewLessons.isChecked());
        originalSettings.put("deadlines", switchDeadlines.isChecked());
        originalSettings.put("discounts", switchDiscounts.isChecked());
        originalSettings.put("newCourseRecommendations", switchNewCourseRecommendations.isChecked());
        originalSettings.put("eventsWebinars", switchEventsWebinars.isChecked());
        
        // Reset change flag
        hasChanges = false;
    }

    private void saveNotificationSettings() {
        if (!hasChanges) {
            Toast.makeText(this, "No changes to save", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            showLoading(false);
            return;
        }

        String userId = currentUser.getUid();
        Map<String, Object> notificationSettings = new HashMap<>();
        
        // General notifications
        notificationSettings.put("pushNotifications", switchPushNotifications.isChecked());
        notificationSettings.put("emailNotifications", switchEmailNotifications.isChecked());
        
        // Course notifications
        notificationSettings.put("courseUpdates", switchCourseUpdates.isChecked());
        notificationSettings.put("newLessons", switchNewLessons.isChecked());
        notificationSettings.put("deadlines", switchDeadlines.isChecked());
        
        // Promotional notifications
        notificationSettings.put("discounts", switchDiscounts.isChecked());
        notificationSettings.put("newCourseRecommendations", switchNewCourseRecommendations.isChecked());
        notificationSettings.put("eventsWebinars", switchEventsWebinars.isChecked());

        // Also update main profile settings
        firestore.collection("userProfiles").document(userId)
                .update(
                        "pushNotifications", switchPushNotifications.isChecked(),
                        "emailNotifications", switchEmailNotifications.isChecked()
                );

        // Save notification settings
        firestore.collection("userNotificationSettings").document(userId)
                .set(notificationSettings)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(NotificationSettingsActivity.this, 
                            "Notification settings saved", Toast.LENGTH_SHORT).show();
                    storeOriginalSettings();
                    showLoading(false);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(NotificationSettingsActivity.this, 
                            "Error saving notification settings: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    showLoading(false);
                });
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            btnSaveNotificationSettings.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            btnSaveNotificationSettings.setEnabled(true);
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

    @Override
    public void onBackPressed() {
        if (hasChanges) {
            // Show confirmation dialog
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
            builder.setTitle(R.string.confirm_discard_changes)
                    .setMessage(R.string.confirm_discard_message)
                    .setPositiveButton(R.string.discard, (dialog, which) -> super.onBackPressed())
                    .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                    .show();
        } else {
            super.onBackPressed();
        }
    }
}