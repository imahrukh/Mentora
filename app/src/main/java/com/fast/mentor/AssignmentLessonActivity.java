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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fast.mentor.R;
import com.fast.mentor.Assignment;
import com.fast.mentor.AssignmentSubmission;
import com.fast.mentor.SubmissionFile;
import com.fast.mentor.*;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * Activity for displaying and submitting assignments.
 */
public class AssignmentLessonActivity extends AppCompatActivity {

    private static final int REQUEST_PICK_FILES = 1001;
    private static final int SUBMISSION_VIEW_NOT_SUBMITTED = 0;
    private static final int SUBMISSION_VIEW_SUBMITTED = 1;
    private static final int SUBMISSION_VIEW_GRADED = 2;

    // Views
    private TextView titleTextView;
    private TextView descriptionTextView;
    private TextView instructionsTextView;
    private TextView pointsTextView;
    private TextView deadlineTextView;
    private ViewFlipper submissionViewFlipper;
    private RecyclerView filesRecyclerView;
    private Button actionButton;
    private EditText commentEditText;
    private TextView submissionStatusTextView;
    private TextView submissionDateTextView;
    private TextView gradeTextView;
    private RecyclerView submittedFilesRecyclerView;
    private ProgressBar loadingProgressBar;
    private View contentLayout;
    private View errorLayout;
    private Button retryButton;

    // Data
    private String lessonId;
    private String userId;
    private Lesson lesson;
    private Assignment assignment;
    private AssignmentSubmission submission;
    private List<Uri> selectedFiles = new ArrayList<>();
    private SubmissionFileAdapter fileAdapter;
    private SubmissionFileAdapter submittedFileAdapter;
    private CourseService courseService;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment_lesson);

        // Get lesson ID from intent
        lessonId = getIntent().getStringExtra("LESSON_ID");
        lesson = getIntent().getParcelableExtra("LESSON");

        if (lessonId == null && lesson != null) {
            lessonId = lesson.getId();
        }

        if (lessonId == null) {
            showError("Lesson ID not provided");
            return;
        }

        // Get current user ID
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Initialize services
        courseService = new CourseService();

        // Initialize views
        initViews();

        // Load lesson and assignment data
        loadData();
    }

    /**
     * Initialize views
     */
    private void initViews() {
        // Toolbar
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Assignment info views
        titleTextView = findViewById(R.id.titleTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        instructionsTextView = findViewById(R.id.instructionsTextView);
        pointsTextView = findViewById(R.id.pointsTextView);
        deadlineTextView = findViewById(R.id.deadlineTextView);

        // Submission views
        submissionViewFlipper = findViewById(R.id.submissionViewFlipper);
        filesRecyclerView = findViewById(R.id.filesRecyclerView);
        actionButton = findViewById(R.id.actionButton);
        commentEditText = findViewById(R.id.commentEditText);
        submissionStatusTextView = findViewById(R.id.submissionStatusTextView);
        submissionDateTextView = findViewById(R.id.submissionDateTextView);
        gradeTextView = findViewById(R.id.gradeTextView);
        submittedFilesRecyclerView = findViewById(R.id.submittedFilesRecyclerView);

        // Loading and error views
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        contentLayout = findViewById(R.id.contentLayout);
        errorLayout = findViewById(R.id.errorLayout);
        retryButton = findViewById(R.id.retryButton);

        // Set up file adapters
        fileAdapter = new SubmissionFileAdapter(this, true, position -> {
            // File removed by user, nothing else to do
        });

        submittedFileAdapter = new SubmissionFileAdapter(this, false, null);

        filesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        filesRecyclerView.setAdapter(fileAdapter);

        submittedFilesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        submittedFilesRecyclerView.setAdapter(submittedFileAdapter);

        // Set up click listeners
        findViewById(R.id.addFileButton).setOnClickListener(v -> selectFiles());
        actionButton.setOnClickListener(v -> handleActionButtonClick());
        retryButton.setOnClickListener(v -> loadData());

        // Initially show loading state
        showLoading();
    }

    /**
     * Load assignment data
     */
    private void loadData() {
        showLoading();

        // Set title from lesson if available
        if (lesson != null) {
            setTitle(lesson.getTitle());
        }

        // Load assignment data
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
     * Load user's submission if it exists
     */
    private void loadSubmission() {
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
     * Set up assignment info
     */
    private void setupAssignmentInfo() {
        titleTextView.setText(assignment.getTitle());
        descriptionTextView.setText(assignment.getDescription());
        instructionsTextView.setText(assignment.getInstructions());

        // Set points
        pointsTextView.setText(getString(R.string.points_value, assignment.getPoints()));

        // Set deadline
        if (assignment.getDeadline() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
            deadlineTextView.setText(getString(R.string.due_date, dateFormat.format(assignment.getDeadline())));
        } else {
            deadlineTextView.setText(R.string.no_deadline);
        }
    }

    /**
     * Update submission view based on status
     */
    private void updateSubmissionView() {
        if (submission == null) {
            // No submission yet
            submissionViewFlipper.setDisplayedChild(SUBMISSION_VIEW_NOT_SUBMITTED);
            return;
        }

        if (submission.getGraded()) {
            // Submission is graded
            submissionViewFlipper.setDisplayedChild(SUBMISSION_VIEW_GRADED);

            // Set submission date
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
            submissionDateTextView.setText(getString(R.string.submitted_date,
                    dateFormat.format(submission.getSubmittedAt())));

            // Set grade and status
            int grade = submission.getGrade();
            int points = assignment.getPoints();
            gradeTextView.setText(getString(R.string.out_of_points, grade, points));

            // Set status
            boolean passed = grade >= assignment.getPassingGrade();
            submissionStatusTextView.setText(passed ? R.string.passed : R.string.failed);
            submissionStatusTextView.setBackgroundResource(
                    passed ? R.drawable.bg_status_passed : R.drawable.bg_status_failed);
        } else {
            // Submission is made but not yet graded
            submissionViewFlipper.setDisplayedChild(SUBMISSION_VIEW_SUBMITTED);

            // Set submission date
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
            submissionDateTextView.setText(getString(R.string.submitted_date,
                    dateFormat.format(submission.getSubmittedAt())));

            // Set status as submitted
            submissionStatusTextView.setText(R.string.submitted);
            submissionStatusTextView.setBackgroundResource(R.drawable.bg_status_submitted);
        }

        // Set submitted files
        submittedFileAdapter.setFiles(submission.getFiles());
    }

    /**
     * Update action button based on submission status
     */
    private void updateActionButton() {
        if (isEditMode) {
            // In edit mode, show submit button
            actionButton.setText(submission == null ? R.string.submit : R.string.update_submission);
            return;
        }

        if (submission == null) {
            // No submission, show submit button
            actionButton.setText(R.string.submit);
        } else if (submission.getGraded()) {
            // Graded submission, show continue button
            actionButton.setText(R.string.continue_to_next);
        } else {
            // Ungraded submission, show resubmit button
            actionButton.setText(R.string.resubmit_assignment);
        }
    }

    /**
     * Handle action button click
     */
    private void handleActionButtonClick() {
        if (isEditMode) {
            // In edit mode, submit the assignment
            submitAssignment();
            return;
        }

        if (submission == null) {
            // No submission, enter edit mode
            enterEditMode();
        } else if (submission.getGraded()) {
            // Graded submission, navigate to next lesson
            navigateToNextLesson();
        } else {
            // Ungraded submission, enter edit mode
            enterEditMode();
        }
    }

    /**
     * Enter edit mode
     */
    private void enterEditMode() {
        isEditMode = true;

        // Show edit view
        submissionViewFlipper.setDisplayedChild(SUBMISSION_VIEW_NOT_SUBMITTED);

        // Set comment if exists
        if (submission != null && submission.getComment() != null) {
            commentEditText.setText(submission.getComment());
        }

        // Set files if exist
        if (submission != null && submission.getFiles() != null) {
            fileAdapter.setFiles(submission.getFiles());
        }

        // Update action button
        updateActionButton();
    }

    /**
     * Exit edit mode
     */
    private void exitEditMode() {
        isEditMode = false;

        // Clear selected files
        selectedFiles.clear();

        // Update view
        updateSubmissionView();

        // Update action button
        updateActionButton();
    }

    /**
     * Submit assignment
     */
    private void submitAssignment() {
        // Get files from adapter
        List<SubmissionFile> files = fileAdapter.getFiles();

        // Validate files
        if (files.isEmpty()) {
            Toast.makeText(this, R.string.select_at_least_one_file, Toast.LENGTH_SHORT).show();
            return;
        }

        // Get comment
        String comment = commentEditText.getText().toString().trim();

        // Get submission ID if updating
        String submissionId = submission != null ? submission.getId() : null;

        // Show loading
        showLoading();

        // Submit assignment
        courseService.submitAssignment(userId, assignment.getId(), comment, files, submissionId,
                newSubmission -> {
                    // Update submission
                    submission = newSubmission;

                    // Exit edit mode
                    exitEditMode();

                    // Hide loading
                    hideLoading();

                    // Show success message
                    Toast.makeText(this, R.string.assignment_submitted, Toast.LENGTH_SHORT).show();
                }, e -> {
                    // Hide loading
                    hideLoading();

                    // Show error message
                    Toast.makeText(this, R.string.error_submitting_assignment, Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Navigate to next lesson
     */
    private void navigateToNextLesson() {
        // This would navigate to the next lesson
        // For simplicity, we'll just finish this activity
        finish();
    }

    /**
     * Select files for submission
     */
    private void selectFiles() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Select Files"), REQUEST_PICK_FILES);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PICK_FILES && resultCode == RESULT_OK && data != null) {
            // Handle selected files
            if (data.getClipData() != null) {
                // Multiple files selected
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri uri = data.getClipData().getItemAt(i).getUri();
                    fileAdapter.addFileUri(uri);
                    selectedFiles.add(uri);
                }
            } else if (data.getData() != null) {
                // Single file selected
                Uri uri = data.getData();
                fileAdapter.addFileUri(uri);
                selectedFiles.add(uri);
            }
        }
    }

    /**
     * Show loading state
     */
    private void showLoading() {
        loadingProgressBar.setVisibility(View.VISIBLE);
        contentLayout.setVisibility(View.GONE);
        errorLayout.setVisibility(View.GONE);
    }

    /**
     * Hide loading state
     */
    private void hideLoading() {
        loadingProgressBar.setVisibility(View.GONE);
        contentLayout.setVisibility(View.VISIBLE);
        errorLayout.setVisibility(View.GONE);
    }

    /**
     * Show error state
     */
    private void showError(String errorMessage) {
        loadingProgressBar.setVisibility(View.GONE);
        contentLayout.setVisibility(View.GONE);
        errorLayout.setVisibility(View.VISIBLE);

        // Set error message
        TextView errorTextView = findViewById(R.id.errorTextView);
        if (errorTextView != null) {
            errorTextView.setText(errorMessage);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
