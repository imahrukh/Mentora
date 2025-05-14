package com.fast.mentor;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.fast.mentor.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Activity for submitting assignments
 */
public class AssignmentSubmissionActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PICK_FILE = 101;

    private ContentItem contentItem;
    private String courseId;

    private Toolbar toolbar;
    private TextView tvTitle;
    private TextView tvDescription;
    private TextView tvInstructions;
    private EditText etAnswer;
    private Button btnAttachFile;
    private TextView tvFilename;
    private Button btnSubmit;
    private ProgressBar progressBar;
    private TextView tvCompletionStatus;

    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;
    private FirebaseStorage storage;

    private Uri selectedFileUri;
    private boolean isAssignmentSubmitted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment_submission);

        // Get data from intent
        contentItem = (ContentItem) getIntent().getSerializableExtra("contentItem");
        courseId = getIntent().getStringExtra("courseId");

        if (contentItem == null) {
            finish();
            return;
        }

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        storage = FirebaseStorage.getInstance();

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        tvTitle = findViewById(R.id.tvTitle);
        tvDescription = findViewById(R.id.tvDescription);
        tvInstructions = findViewById(R.id.tvInstructions);
        etAnswer = findViewById(R.id.etAnswer);
        btnAttachFile = findViewById(R.id.btnAttachFile);
        tvFilename = findViewById(R.id.tvFilename);
        btnSubmit = findViewById(R.id.btnSubmit);
        progressBar = findViewById(R.id.progressBar);
        tvCompletionStatus = findViewById(R.id.tvCompletionStatus);

        // Setup toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        // Set assignment details
        tvTitle.setText(contentItem.getTitle());
        if (contentItem.getDescription() != null && !contentItem.getDescription().isEmpty()) {
            tvDescription.setText(contentItem.getDescription());
            tvDescription.setVisibility(View.VISIBLE);
        } else {
            tvDescription.setVisibility(View.GONE);
        }

        // Setup file attachment button
        btnAttachFile.setOnClickListener(v -> openFilePicker());

        // Setup submit button
        btnSubmit.setOnClickListener(v -> {
            if (isAssignmentSubmitted) {
                finish();
            } else {
                submitAssignment();
            }
        });

        // Load assignment details
        loadAssignmentDetails();

        // Check if assignment is already submitted
        checkSubmissionStatus();
    }
    
    private void loadAssignmentDetails() {
        showLoading(true);
        
        firestore.collection("assignments")
                .document(contentItem.getId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    showLoading(false);
                    
                    if (documentSnapshot.exists()) {
                        String instructions = documentSnapshot.getString("instructions");
                        if (instructions != null && !instructions.isEmpty()) {
                            tvInstructions.setText(instructions);
                            tvInstructions.setVisibility(View.VISIBLE);
                        } else {
                            tvInstructions.setVisibility(View.GONE);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    tvInstructions.setVisibility(View.GONE);
                });
    }

    private void checkSubmissionStatus() {
        if (currentUser == null) {
            return;
        }

        firestore.collection("submissions")
                .whereEqualTo("userId", currentUser.getUid())
                .whereEqualTo("assignmentId", contentItem.getId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Assignment already submitted
                        DocumentSnapshot submission = queryDocumentSnapshots.getDocuments().get(0);

                        // Check if approved
                        Boolean isApproved = submission.getBoolean("isApproved");
                        if (isApproved != null && isApproved) {
                            // Assignment is approved
                            contentItem.setCompleted(true);
                            tvCompletionStatus.setText(R.string.assignment_approved);
                            tvCompletionStatus.setTextColor(getResources().getColor(R.color.colorSuccess, null));
                        } else {
                            // Assignment is submitted but pending approval
                            tvCompletionStatus.setText(R.string.assignment_submitted);
                            tvCompletionStatus.setTextColor(getResources().getColor(R.color.colorWarning, null));
                        }

                        tvCompletionStatus.setVisibility(View.VISIBLE);
                        isAssignmentSubmitted = true;

                        // Disable input fields
                        etAnswer.setEnabled(false);
                        btnAttachFile.setEnabled(false);
                        btnSubmit.setText(R.string.close);

                        // Show submitted answer
                        String answer = submission.getString("answer");
                        if (answer != null && !answer.isEmpty()) {
                            etAnswer.setText(answer);
                        }

                        // Show submitted file name if any
                        String fileName = submission.getString("fileName");
                        if (fileName != null && !fileName.isEmpty()) {
                            tvFilename.setText(fileName);
                            tvFilename.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }
    
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        
        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a file to upload"),
                    REQUEST_CODE_PICK_FILE);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_CODE_PICK_FILE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                selectedFileUri = data.getData();
                
                // Display file name
                String fileName = getFileNameFromUri(selectedFileUri);
                tvFilename.setText(fileName);
                tvFilename.setVisibility(View.VISIBLE);
            }
        }
    }
    
    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (android.database.Cursor cursor = 
                     getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (columnIndex != -1) {
                        result = cursor.getString(columnIndex);
                    }
                }
            }
        }
        
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        
        return result;
    }
    
    private void submitAssignment() {
        String answer = etAnswer.getText().toString().trim();
        
        if (answer.isEmpty() && selectedFileUri == null) {
            Toast.makeText(this, "Please provide an answer or attach a file", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to submit", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showLoading(true);
        
        // First, upload the file if selected
        if (selectedFileUri != null) {
            uploadFile(answer);
        } else {
            // If no file, just save the submission data
            saveSubmission(answer, null, null);
        }
    }
    
    private void uploadFile(String answer) {
        String fileName = getFileNameFromUri(selectedFileUri);
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileId = currentUser.getUid() + "_" + timestamp + "_" + fileName;
        
        StorageReference fileRef = storage.getReference("submissions").child(fileId);
        
        fileRef.putFile(selectedFileUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get the download URL
                    fileRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                // Save the submission with the file URL
                                saveSubmission(answer, uri.toString(), fileName);
                            })
                            .addOnFailureListener(e -> {
                                showLoading(false);
                                Toast.makeText(AssignmentSubmissionActivity.this, 
                                        "Failed to get file URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(AssignmentSubmissionActivity.this, 
                            "Failed to upload file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                })
                .addOnProgressListener((UploadTask.TaskSnapshot taskSnapshot) -> {
                    // Progress tracking could be added here
                });
    }

    private void saveSubmission(String answer, String fileUrl, String fileName) {
        String userId = currentUser.getUid();
        String assignmentId = contentItem.getId();

        // Create submission document
        Map<String, Object> submissionData = new HashMap<>();
        submissionData.put("userId", userId);
        submissionData.put("courseId", courseId);
        submissionData.put("assignmentId", assignmentId);
        submissionData.put("answer", answer);
        submissionData.put("submittedAt", System.currentTimeMillis());
        submissionData.put("isApproved", false); // Initially not approved

        if (fileUrl != null) {
            submissionData.put("fileUrl", fileUrl);
            submissionData.put("fileName", fileName);
        }

        // Add to Firestore
        firestore.collection("submissions")
                .document(userId + "_" + assignmentId)
                .set(submissionData)
                .addOnSuccessListener(aVoid -> {
                    // Set the progress record even though it's not yet approved
                    updateProgressRecord();

                    // Update UI
                    isAssignmentSubmitted = true;
                    showLoading(false);

                    // Show submitted status
                    tvCompletionStatus.setText(R.string.assignment_submitted);
                    tvCompletionStatus.setTextColor(getResources().getColor(R.color.colorWarning, null));
                    tvCompletionStatus.setVisibility(View.VISIBLE);

                    // Disable inputs
                    etAnswer.setEnabled(false);
                    btnAttachFile.setEnabled(false);
                    btnSubmit.setText(R.string.close);

                    Toast.makeText(AssignmentSubmissionActivity.this,
                            "Assignment submitted successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(AssignmentSubmissionActivity.this,
                            "Failed to submit assignment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    
    private void updateProgressRecord() {
        if (currentUser == null || contentItem == null) {
            return;
        }
        
        String userId = currentUser.getUid();
        String lessonId = contentItem.getId();
        
        // Create progress document - initially not completed since it needs approval
        Map<String, Object> progressData = new HashMap<>();
        progressData.put("userId", userId);
        progressData.put("courseId", courseId);
        progressData.put("lessonId", lessonId);
        progressData.put("isCompleted", false); // Not completed until approved
        progressData.put("submittedAt", System.currentTimeMillis());
        
        // Add to Firestore
        firestore.collection("progress")
                .document(userId + "_" + lessonId)
                .set(progressData);
    }
    
    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSubmit.setEnabled(!isLoading);
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}