package com.fast.mentor;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fast.mentor.R;
import com.fast.mentor.Assignment;
import com.fast.mentor.AssignmentSubmission;
import com.fast.mentor.SubmissionFile;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Activity for assignment-based lessons.
 */
public class AssignmentLessonActivity extends LessonContentActivity 
        implements SubmissionFileAdapter.SubmissionFileListener {

    private TextView assignmentTitleTextView;
    private TextView assignmentDescriptionTextView;
    private TextView pointsTextView;
    private TextView deadlineTextView;
    private TextView statusTextView;
    private WebView instructionsWebView;
    private ViewFlipper submissionViewFlipper;
    private Button uploadButton;
    private RecyclerView submissionFilesRecyclerView;
    private EditText submissionCommentEditText;
    private Button addFilesButton;
    private Button submitButton;
    private TextView gradeTextView;
    private TextView maxPointsTextView;
    private TextView submissionDateTextView;
    private TextView feedbackTextView;
    private RecyclerView gradedFilesRecyclerView;
    
    private SubmissionFileAdapter submissionFileAdapter;
    private SubmissionFileAdapter gradedFileAdapter;
    
    private Assignment assignment;
    private AssignmentSubmission submission;
    private List<Uri> selectedFileUris = new ArrayList<>();
    
    private static final int SUBMISSION_VIEW_NOT_SUBMITTED = 0;
    private static final int SUBMISSION_VIEW_EDIT = 1;
    private static final int SUBMISSION_VIEW_GRADED = 2;
    
    private static final int REQUEST_CODE_SELECT_FILES = 101;

    /**
     * Create intent to launch this activity
     */
    public static Intent createIntent(Context context, String lessonId, String moduleId, 
                                     String courseId, boolean isFirstLesson, boolean isLastLesson,
                                     String previousLessonId, String nextLessonId) {
        Intent intent = new Intent(context, AssignmentLessonActivity.class);
        intent.putExtra(EXTRA_LESSON_ID, lessonId);
        intent.putExtra(EXTRA_MODULE_ID, moduleId);
        intent.putExtra(EXTRA_COURSE_ID, courseId);
        intent.putExtra(EXTRA_IS_FIRST_LESSON, isFirstLesson);
        intent.putExtra(EXTRA_IS_LAST_LESSON, isLastLesson);
        intent.putExtra(EXTRA_PREVIOUS_LESSON_ID, previousLessonId);
        intent.putExtra(EXTRA_NEXT_LESSON_ID, nextLessonId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Inflate assignment content layout into content container
        LayoutInflater.from(this).inflate(R.layout.content_assignment_lesson, contentContainer, true);
        
        // Initialize assignment-specific views
        initializeAssignmentViews();
        
        // Restore state if needed
        if (savedInstanceState != null) {
            ArrayList<String> savedUriStrings = savedInstanceState.getStringArrayList("selectedFileUris");
            if (savedUriStrings != null) {
                selectedFileUris.clear();
                for (String uriString : savedUriStrings) {
                    selectedFileUris.add(Uri.parse(uriString));
                }
            }
        }
    }

    /**
     * Initialize assignment-specific views
     */
    private void initializeAssignmentViews() {
        assignmentTitleTextView = findViewById(R.id.assignmentTitleTextView);
        assignmentDescriptionTextView = findViewById(R.id.assignmentDescriptionTextView);
        pointsTextView = findViewById(R.id.pointsTextView);
        deadlineTextView = findViewById(R.id.deadlineTextView);
        statusTextView = findViewById(R.id.statusTextView);
        instructionsWebView = findViewById(R.id.instructionsWebView);
        submissionViewFlipper = findViewById(R.id.submissionViewFlipper);
        uploadButton = findViewById(R.id.uploadButton);
        submissionFilesRecyclerView = findViewById(R.id.submissionFilesRecyclerView);
        submissionCommentEditText = findViewById(R.id.submissionCommentEditText);
        addFilesButton = findViewById(R.id.addFilesButton);
        submitButton = findViewById(R.id.submitButton);
        gradeTextView = findViewById(R.id.gradeTextView);
        maxPointsTextView = findViewById(R.id.maxPointsTextView);
        submissionDateTextView = findViewById(R.id.submissionDateTextView);
        feedbackTextView = findViewById(R.id.feedbackTextView);
        gradedFilesRecyclerView = findViewById(R.id.gradedFilesRecyclerView);
        
        // Setup WebView for instructions
        WebSettings webSettings = instructionsWebView.getSettings();
        webSettings.setJavaScriptEnabled(false);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        
        // Setup recycler views
        submissionFilesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        submissionFileAdapter = new SubmissionFileAdapter(this, true, this);
        submissionFilesRecyclerView.setAdapter(submissionFileAdapter);
        
        gradedFilesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        gradedFileAdapter = new SubmissionFileAdapter(this, false, null);
        gradedFilesRecyclerView.setAdapter(gradedFileAdapter);
        
        // Setup buttons
        uploadButton.setOnClickListener(v -> selectFiles());
        addFilesButton.setOnClickListener(v -> selectFiles());
        submitButton.setOnClickListener(v -> submitAssignment());
    }

    @Override
    protected void initializeLessonContent() {
        // Set lesson title
        setTitle(lesson.getTitle());
        
        // Load assignment
        loadAssignment();
    }

    /**
     * Load assignment data from Firestore
     */
    private void loadAssignment() {
        showLoading();
        
        // Load assignment
        courseService.getAssignment(lessonId, loadedAssignment -> {
            assignment = loadedAssignment;
            
            // Set assignment info
            setupAssignmentInfo();
            
            // Load submission if exists
            loadSubmission();
            
            // Hide loading
            hideLoading();
        }, e -> {
            showError("Failed to load assignment: " + e.getMessage());
        });
    }

    /**
     * Load submission if it exists
     */
    private void loadSubmission() {
        String userId = getCurrentUserId();
        
        courseService.getAssignmentSubmission(userId, assignment.getId(), loadedSubmission -> {
            submission = loadedSubmission;
            
            // Update submission view based on status
            updateSubmissionView();
            
            // Update action button
            updateActionButton();
            
        }, e -> {
            // No submission yet or error loading
            submission = null;
            
            // Show not submitted view
            submissionViewFlipper.setDisplayedChild(SUBMISSION_VIEW_NOT_SUBMITTED);
            
            // Update action button
            updateActionButton();
        });
    }

    /**
     * Setup assignment info in UI
     */
    private void setupAssignmentInfo() {
        assignmentTitleTextView.setText(assignment.getTitle());
        assignmentDescriptionTextView.setText(assignment.getDescription());
        
        // Set points
        pointsTextView.setText(getString(R.string.points_value, assignment.getPoints()));
        
        // Set deadline
        Date deadline = assignment.getDeadline();
        if (deadline != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
            deadlineTextView.setText(getString(R.string.due_date, dateFormat.format(deadline)));
        } else {
            deadlineTextView.setText(R.string.no_deadline);
        }
        
        // Load instructions
        String instructions = assignment.getInstructions();
        if (instructions != null && !instructions.isEmpty()) {
            // Wrap content in basic HTML for proper formatting
            String formattedInstructions = "<html><head>" +
                    "<style>body{color:#e1e1e6;font-size:14px;line-height:1.5;padding:0;margin:0;}" +
                    "h1,h2,h3{color:#ffffff;}code{background-color:#272731;padding:2px 4px;border-radius:4px;}" +
                    "pre{background-color:#272731;padding:8px;border-radius:4px;overflow-x:auto;}" +
                    "a{color:#ff3d71;}</style></head><body>" +
                    instructions +
                    "</body></html>";
            
            instructionsWebView.loadDataWithBaseURL(null, formattedInstructions, "text/html", "UTF-8", null);
        }
    }

    /**
     * Update submission view based on status
     */
    private void updateSubmissionView() {
        if (submission == null) {
            // No submission yet
            submissionViewFlipper.setDisplayedChild(SUBMISSION_VIEW_NOT_SUBMITTED);
            statusTextView.setText(R.string.not_submitted);
            statusTextView.setBackgroundResource(R.drawable.bg_status_pending);
            
        } else if (submission.getGraded()) {
            // Graded submission
            submissionViewFlipper.setDisplayedChild(SUBMISSION_VIEW_GRADED);
            
            // Update status
            boolean isPassed = submission.getGrade() >= assignment.getPassingGrade();
            if (isPassed) {
                statusTextView.setText(R.string.passed);
                statusTextView.setBackgroundResource(R.drawable.bg_status_passed);
            } else {
                statusTextView.setText(R.string.failed);
                statusTextView.setBackgroundResource(R.drawable.bg_status_failed);
            }
            
            // Update grade info
            gradeTextView.setText(String.valueOf(submission.getGrade()));
            maxPointsTextView.setText(getString(R.string.out_of_points, assignment.getPoints()));
            
            // Set submission date
            if (submission.getSubmittedAt() != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
                submissionDateTextView.setText(getString(R.string.submitted_date, 
                        dateFormat.format(submission.getSubmittedAt())));
            }
            
            // Set feedback
            feedbackTextView.setText(submission.getFeedback());
            
            // Set files
            List<SubmissionFile> files = submission.getFiles();
            if (files != null && !files.isEmpty()) {
                gradedFileAdapter.setFiles(files);
            }
            
        } else {
            // Submitted but not graded
            submissionViewFlipper.setDisplayedChild(SUBMISSION_VIEW_EDIT);
            statusTextView.setText(R.string.submitted);
            statusTextView.setBackgroundResource(R.drawable.bg_status_submitted);
            
            // Set files
            List<SubmissionFile> files = submission.getFiles();
            if (files != null && !files.isEmpty()) {
                submissionFileAdapter.setFiles(files);
            }
            
            // Set comment
            submissionCommentEditText.setText(submission.getComment());
        }
    }

    /**
     * Update action button based on submission status
     */
    private void updateActionButton() {
        if (submission == null) {
            // No submission yet
            actionButton.setText(R.string.submit_assignment);
            actionButton.setEnabled(true);
        } else if (submission.getGraded()) {
            // Graded submission
            boolean isPassed = submission.getGrade() >= assignment.getPassingGrade();
            if (isPassed) {
                // Passed, can continue
                actionButton.setText(R.string.continue_to_next);
                actionButton.setEnabled(true);
            } else {
                // Failed, can resubmit
                actionButton.setText(R.string.resubmit_assignment);
                actionButton.setEnabled(true);
            }
        } else {
            // Submitted but not graded
            actionButton.setText(R.string.update_submission);
            actionButton.setEnabled(true);
        }
    }

    /**
     * Select files for submission
     */
    private void selectFiles() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        
        startActivityForResult(intent, REQUEST_CODE_SELECT_FILES);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_CODE_SELECT_FILES && resultCode == RESULT_OK && data != null) {
            // Show edit submission view
            submissionViewFlipper.setDisplayedChild(SUBMISSION_VIEW_EDIT);
            
            // Handle single selection
            if (data.getData() != null) {
                Uri uri = data.getData();
                selectedFileUris.add(uri);
                
                // Get persistable URI permission
                getContentResolver().takePersistableUriPermission(uri, 
                        Intent.FLAG_GRANT_READ_URI_PERMISSION);
                
                // Add to adapter
                submissionFileAdapter.addFileUri(uri);
            }
            // Handle multiple selection
            else if (data.getClipData() != null) {
                for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                    Uri uri = data.getClipData().getItemAt(i).getUri();
                    selectedFileUris.add(uri);
                    
                    // Get persistable URI permission
                    getContentResolver().takePersistableUriPermission(uri, 
                            Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    
                    // Add to adapter
                    submissionFileAdapter.addFileUri(uri);
                }
            }
        }
    }

    /**
     * Submit assignment
     */
    private void submitAssignment() {
        // Check if files are selected
        if (submissionFileAdapter.getItemCount() == 0) {
            Toast.makeText(this, R.string.select_at_least_one_file, Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show loading
        showLoading();
        
        // Get user ID
        String userId = getCurrentUserId();
        
        // Get comment
        String comment = submissionCommentEditText.getText().toString().trim();
        
        // Submit files to Firebase Storage and create submission
        courseService.submitAssignment(
            userId,
            assignment.getId(),
            submission != null ? submission.getId() : null,
            submissionFileAdapter.getFiles(),
            comment,
            newSubmission -> {
                // Success
                submission = newSubmission;
                
                // Hide loading
                hideLoading();
                
                // Show success message
                Toast.makeText(this, R.string.assignment_submitted, Toast.LENGTH_SHORT).show();
                
                // Update view
                updateSubmissionView();
                updateActionButton();
            },
            e -> {
                // Error
                hideLoading();
                Toast.makeText(this, getString(R.string.error_submitting_assignment, 
                        e.getMessage()), Toast.LENGTH_SHORT).show();
            }
        );
    }

    @Override
    protected void onActionButtonClicked() {
        if (submission == null) {
            // No submission yet, start submission process
            selectFiles();
        } else if (submission.getGraded()) {
            // Graded submission
            boolean isPassed = submission.getGrade() >= assignment.getPassingGrade();
            if (isPassed) {
                // Passed, continue to next lesson
                markLessonComplete();
            } else {
                // Failed, resubmit
                selectFiles();
            }
        } else {
            // Submitted but not graded, can update
            submitAssignment();
        }
    }

    @Override
    protected void updateResourcesView() {
        // Assignment doesn't show resources in the same way
    }

    @Override
    public void onRemoveFile(int position) {
        // Remove file from selected files
        if (position < selectedFileUris.size()) {
            selectedFileUris.remove(position);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        
        // Save selected file URIs
        ArrayList<String> uriStrings = new ArrayList<>();
        for (Uri uri : selectedFileUris) {
            uriStrings.add(uri.toString());
        }
        outState.putStringArrayList("selectedFileUris", uriStrings);
    }
}