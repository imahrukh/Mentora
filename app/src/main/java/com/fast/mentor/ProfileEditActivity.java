package com.fast.mentor;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.fast.mentor.R;
import com.fast.mentor.models.UserProfile;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Activity for editing user profile information
 */
public class ProfileEditActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_GALLERY_PERMISSION = 101;

    // UI components
    private CircleImageView ivProfilePicture;
    private TextInputEditText etFullName, etUsername, etEmail, etBio;
    private AutoCompleteTextView actvLanguage;
    private SwitchMaterial switchEmailNotifications, switchPushNotifications;
    private ChipGroup chipGroupInterests;
    private Button btnSaveChanges;
    private View progressBar;

    // Firebase components
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    // Data
    private UserProfile userProfile;
    private boolean hasChanges = false;
    private Uri currentPhotoUri;
    private boolean isPhotoChanged = false;
    private List<String> selectedInterests = new ArrayList<>();

    // Activity result launchers
    private final ActivityResultLauncher<Intent> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), currentPhotoUri);
                        ivProfilePicture.setImageBitmap(bitmap);
                        isPhotoChanged = true;
                        hasChanges = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                            ivProfilePicture.setImageBitmap(bitmap);
                            currentPhotoUri = imageUri;
                            isPhotoChanged = true;
                            hasChanges = true;
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        // Initialize Firebase instances
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        // Initialize UI components
        initializeViews();
        setupToolbar();
        setupListeners();
        
        // Load user profile data
        loadUserProfile();
    }

    private void initializeViews() {
        ivProfilePicture = findViewById(R.id.ivProfilePicture);
        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etBio = findViewById(R.id.etBio);
        actvLanguage = findViewById(R.id.actvLanguage);
        switchEmailNotifications = findViewById(R.id.switchEmailNotifications);
        switchPushNotifications = findViewById(R.id.switchPushNotifications);
        chipGroupInterests = findViewById(R.id.chipGroupInterests);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        progressBar = findViewById(R.id.progressBar);
        FloatingActionButton fabChangePhoto = findViewById(R.id.fabChangePhoto);

        // Setup language dropdown
        String[] languages = {"English", "Spanish", "French", "German", "Chinese", "Japanese", "Arabic", "Russian"};
        ArrayAdapter<String> languageAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, languages);
        actvLanguage.setAdapter(languageAdapter);

        // Setup profile picture change button
        fabChangePhoto.setOnClickListener(v -> showPhotoOptionsDialog());
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
        // Text change listeners to detect changes
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                hasChanges = true;
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        etFullName.addTextChangedListener(textWatcher);
        etUsername.addTextChangedListener(textWatcher);
        etBio.addTextChangedListener(textWatcher);

        // Switch change listeners
        switchEmailNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> hasChanges = true);
        switchPushNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> hasChanges = true);

        // Language selection listener
        actvLanguage.setOnItemClickListener((parent, view, position, id) -> hasChanges = true);

        // Save button listener
        btnSaveChanges.setOnClickListener(v -> saveUserProfile());
    }

    private void loadUserProfile() {
        showLoading(true);
        
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String userId = currentUser.getUid();
        firestore.collection("userProfiles").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Create user profile from Firestore document
                        Map<String, Object> data = documentSnapshot.getData();
                        if (data != null) {
                            userProfile = new UserProfile();
                            userProfile.setId(Integer.parseInt(userId));
                            userProfile.setFullName((String) data.get("fullName"));
                            userProfile.setUsername((String) data.get("username"));
                            userProfile.setEmail((String) data.get("email"));
                            userProfile.setBio((String) data.get("bio"));
                            userProfile.setProfilePictureUrl((String) data.get("profilePictureUrl"));
                            userProfile.setPreferredLanguage((String) data.get("preferredLanguage"));
                            userProfile.setEmailNotifications((Boolean) data.getOrDefault("emailNotifications", true));
                            userProfile.setPushNotifications((Boolean) data.getOrDefault("pushNotifications", true));
                            
                            // Get interests
                            List<String> interests = (List<String>) data.get("interests");
                            if (interests != null) {
                                userProfile.setInterests(interests);
                                selectedInterests = new ArrayList<>(interests);
                            }
                            
                            // Other stats
                            if (data.get("coursesCompleted") != null) {
                                userProfile.setCoursesCompleted(((Long) data.get("coursesCompleted")).intValue());
                            }
                            if (data.get("hoursSpent") != null) {
                                userProfile.setHoursSpent(((Long) data.get("hoursSpent")).intValue());
                            }
                            if (data.get("certificatesEarned") != null) {
                                userProfile.setCertificatesEarned(((Long) data.get("certificatesEarned")).intValue());
                            }
                            
                            // Populate UI with data
                            populateUI();
                        }
                    } else {
                        // Create a new profile if it doesn't exist
                        userProfile = new UserProfile();
                        userProfile.setId(Integer.parseInt(userId));
                        userProfile.setEmail(currentUser.getEmail());
                        userProfile.setFullName(currentUser.getDisplayName());
                        userProfile.setUsername(currentUser.getEmail() != null ? 
                                currentUser.getEmail().split("@")[0] : "user" + userId);
                    }
                    showLoading(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ProfileEditActivity.this, 
                            "Error loading profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    showLoading(false);
                });
    }

    private void populateUI() {
        if (userProfile == null) return;

        etFullName.setText(userProfile.getFullName());
        etUsername.setText(userProfile.getUsername());
        etEmail.setText(userProfile.getEmail());
        etBio.setText(userProfile.getBio());
        actvLanguage.setText(userProfile.getPreferredLanguage(), false);
        switchEmailNotifications.setChecked(userProfile.isEmailNotifications());
        switchPushNotifications.setChecked(userProfile.isPushNotifications());

        // Load profile picture if available
        String profilePictureUrl = userProfile.getProfilePictureUrl();
        if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
            // In a real app, you would use a library like Glide or Picasso to load the image
            // For example:
            // Glide.with(this).load(profilePictureUrl).into(ivProfilePicture);
        }

        // Load interests chips
        updateInterestsChips();
    }

    private void updateInterestsChips() {
        chipGroupInterests.removeAllViews();
        
        // Add all selected interests as chips
        if (selectedInterests != null && !selectedInterests.isEmpty()) {
            for (String interest : selectedInterests) {
                Chip chip = new Chip(this);
                chip.setText(interest);
                chip.setCloseIconVisible(true);
                chip.setOnCloseIconClickListener(v -> {
                    selectedInterests.remove(interest);
                    chipGroupInterests.removeView(chip);
                    hasChanges = true;
                });
                chipGroupInterests.addView(chip);
            }
        }

        // Add an "Add" chip at the end
        Chip addChip = new Chip(this);
        addChip.setText("+ Add");
        addChip.setChipBackgroundColorResource(R.color.surfaceColor);
        addChip.setTextColor(getResources().getColor(R.color.primaryColor, null));
        addChip.setOnClickListener(v -> showSelectInterestsDialog());
        chipGroupInterests.addView(addChip);
    }

    private void showSelectInterestsDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_select_interests);

        // Get dialog views
        ChipGroup dialogChipGroup = dialog.findViewById(R.id.chipGroupInterests);
        Button btnApply = dialog.findViewById(R.id.btnApply);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        // Prepare interest categories and interests
        Map<String, List<String>> interestCategories = new HashMap<>();
        interestCategories.put("Technology", Arrays.asList("Programming", "Mobile Development", 
                "Web Development", "Artificial Intelligence", "Data Science"));
        interestCategories.put("Business", Arrays.asList("Marketing", "Finance", 
                "Entrepreneurship", "Leadership"));
        interestCategories.put("Design", Arrays.asList("Graphic Design", "UX Design", "Web Design"));
        interestCategories.put("Languages", Arrays.asList("English", "Spanish", "French", "Chinese"));
        interestCategories.put("Science", Arrays.asList("Physics", "Mathematics", "Biology", "Chemistry"));

        // Set initial state based on already selected interests
        for (int i = 0; i < dialogChipGroup.getChildCount(); i++) {
            View view = dialogChipGroup.getChildAt(i);
            if (view instanceof Chip) {
                Chip chip = (Chip) view;
                if (selectedInterests.contains(chip.getText().toString())) {
                    chip.setChecked(true);
                }
            }
        }

        // Setup button click listeners
        btnApply.setOnClickListener(v -> {
            List<String> newInterests = new ArrayList<>();
            for (int i = 0; i < dialogChipGroup.getChildCount(); i++) {
                View view = dialogChipGroup.getChildAt(i);
                if (view instanceof Chip) {
                    Chip chip = (Chip) view;
                    if (chip.isChecked()) {
                        newInterests.add(chip.getText().toString());
                    }
                }
            }
            selectedInterests = newInterests;
            updateInterestsChips();
            hasChanges = true;
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showPhotoOptionsDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_profile_photo_options);
        
        // Get dialog views
        LinearLayout llTakePhoto = dialog.findViewById(R.id.llTakePhoto);
        LinearLayout llChooseFromGallery = dialog.findViewById(R.id.llChooseFromGallery);
        LinearLayout llRemovePhoto = dialog.findViewById(R.id.llRemovePhoto);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        
        // Setup click listeners
        llTakePhoto.setOnClickListener(v -> {
            if (checkCameraPermission()) {
                takePhoto();
            }
            dialog.dismiss();
        });
        
        llChooseFromGallery.setOnClickListener(v -> {
            if (checkGalleryPermission()) {
                chooseFromGallery();
            }
            dialog.dismiss();
        });
        
        llRemovePhoto.setOnClickListener(v -> {
            removeProfilePhoto();
            dialog.dismiss();
        });
        
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }
    
    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            return false;
        }
        return true;
    }
    
    private boolean checkGalleryPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_GALLERY_PERMISSION);
            return false;
        }
        return true;
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                          @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePhoto();
            } else {
                Toast.makeText(this, "Camera permission is required to take photos", 
                        Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_GALLERY_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                chooseFromGallery();
            } else {
                Toast.makeText(this, "Storage permission is required to access gallery", 
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                String imageFileName = "JPEG_" + timeStamp + "_";
                File storageDir = getExternalFilesDir(null);
                photoFile = File.createTempFile(imageFileName, ".jpg", storageDir);
            } catch (IOException ex) {
                Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
            }
            
            if (photoFile != null) {
                currentPhotoUri = FileProvider.getUriForFile(this,
                        "com.fast.mentor.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri);
                takePictureLauncher.launch(takePictureIntent);
            }
        }
    }
    
    private void chooseFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }
    
    private void removeProfilePhoto() {
        ivProfilePicture.setImageResource(R.drawable.ic_launcher_foreground); // Default image
        currentPhotoUri = null;
        isPhotoChanged = true;
        hasChanges = true;
    }

    private void saveUserProfile() {
        if (!validateInputs()) return;

        showLoading(true);
        
        // Get current user
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            showLoading(false);
            return;
        }
        
        String userId = currentUser.getUid();
        
        // Prepare profile data
        Map<String, Object> profileData = new HashMap<>();
        profileData.put("fullName", Objects.requireNonNull(etFullName.getText()).toString().trim());
        profileData.put("username", Objects.requireNonNull(etUsername.getText()).toString().trim());
        profileData.put("email", Objects.requireNonNull(etEmail.getText()).toString().trim());
        profileData.put("bio", Objects.requireNonNull(etBio.getText()).toString().trim());
        profileData.put("preferredLanguage", actvLanguage.getText().toString().trim());
        profileData.put("emailNotifications", switchEmailNotifications.isChecked());
        profileData.put("pushNotifications", switchPushNotifications.isChecked());
        profileData.put("interests", selectedInterests);
        
        // Update email in Firebase Auth if changed
        if (!profileData.get("email").equals(currentUser.getEmail())) {
            currentUser.updateEmail((String) profileData.get("email"))
                    .addOnSuccessListener(aVoid -> {
                        // Continue with profile update
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ProfileEditActivity.this, 
                                "Failed to update email: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        showLoading(false);
                    });
        }
        
        // Upload profile picture if changed
        if (isPhotoChanged && currentPhotoUri != null) {
            uploadProfilePicture(userId, profileData);
        } else if (isPhotoChanged) {
            // Remove profile picture
            profileData.put("profilePictureUrl", "");
            updateFirestoreProfile(userId, profileData);
        } else {
            // No picture change, update profile directly
            updateFirestoreProfile(userId, profileData);
        }
    }
    
    private void uploadProfilePicture(String userId, Map<String, Object> profileData) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), currentPhotoUri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
            byte[] data = baos.toByteArray();
            
            StorageReference imageRef = storageRef.child("profile_pictures/" + userId + ".jpg");
            UploadTask uploadTask = imageRef.putBytes(data);
            uploadTask.addOnSuccessListener(taskSnapshot -> 
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        profileData.put("profilePictureUrl", uri.toString());
                        updateFirestoreProfile(userId, profileData);
                    }))
                    .addOnFailureListener(e -> {
                        Toast.makeText(ProfileEditActivity.this, 
                                R.string.image_upload_failed, Toast.LENGTH_SHORT).show();
                        showLoading(false);
                    });
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.image_upload_failed, Toast.LENGTH_SHORT).show();
            showLoading(false);
        }
    }
    
    private void updateFirestoreProfile(String userId, Map<String, Object> profileData) {
        firestore.collection("userProfiles").document(userId)
                .set(profileData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ProfileEditActivity.this, 
                            R.string.profile_updated, Toast.LENGTH_SHORT).show();
                    hasChanges = false;
                    showLoading(false);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ProfileEditActivity.this, 
                            R.string.error_updating_profile, Toast.LENGTH_SHORT).show();
                    showLoading(false);
                });
    }
    
    private boolean validateInputs() {
        String fullName = Objects.requireNonNull(etFullName.getText()).toString().trim();
        String username = Objects.requireNonNull(etUsername.getText()).toString().trim();
        String email = Objects.requireNonNull(etEmail.getText()).toString().trim();
        
        if (fullName.isEmpty()) {
            TextInputLayout tilFullName = findViewById(R.id.tilFullName);
            tilFullName.setError("Full name is required");
            return false;
        }
        
        if (username.isEmpty()) {
            TextInputLayout tilUsername = findViewById(R.id.tilUsername);
            tilUsername.setError("Username is required");
            return false;
        }
        
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            TextInputLayout tilEmail = findViewById(R.id.tilEmail);
            tilEmail.setError("Valid email is required");
            return false;
        }
        
        return true;
    }
    
    private void showLoading(boolean isLoading) {
        if (isLoading) {
            btnSaveChanges.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            btnSaveChanges.setEnabled(true);
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
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
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