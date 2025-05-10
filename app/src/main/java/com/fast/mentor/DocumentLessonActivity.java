package com.fast.mentor;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fast.mentor.R;
import com.fast.mentor.Resource;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

/**
 * Activity for PDF document-based lessons.
 */
public class DocumentLessonActivity extends LessonContentActivity 
        implements OnPageChangeListener, OnLoadCompleteListener, OnPageErrorListener {

    private PDFView pdfView;
    private ProgressBar pdfLoadingProgressBar;
    private LinearLayout pdfErrorView;
    private TextView lessonTitleTextView;
    private TextView lessonDescriptionTextView;
    private TextView pagesTextView;
    private TextView zoomLevelTextView;
    private ImageButton zoomInButton;
    private ImageButton zoomOutButton;
    private RecyclerView resourcesRecyclerView;
    private TextView emptyResourcesTextView;
    
    private ResourceAdapter resourceAdapter;
    private File pdfFile;
    private int currentPage = 0;
    private int pageCount = 0;
    private float currentZoom = 1.0f;
    
    // Document completion tracking
    private boolean isLessonCompleted = false;
    private boolean hasReachedLastPage = false;
    
    private static final float MIN_ZOOM = 0.5f;
    private static final float MAX_ZOOM = 3.0f;
    private static final float ZOOM_STEP = 0.25f;

    /**
     * Create intent to launch this activity
     */
    public static Intent createIntent(Context context, String lessonId, String moduleId, 
                                     String courseId, boolean isFirstLesson, boolean isLastLesson,
                                     String previousLessonId, String nextLessonId) {
        Intent intent = new Intent(context, DocumentLessonActivity.class);
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
        
        // Inflate document content layout into content container
        LayoutInflater.from(this).inflate(R.layout.content_document_lesson, contentContainer, true);
        
        // Initialize document-specific views
        initializeDocumentViews();
        
        // Get any saved state
        if (savedInstanceState != null) {
            currentPage = savedInstanceState.getInt("current_page", 0);
            currentZoom = savedInstanceState.getFloat("current_zoom", 1.0f);
            isLessonCompleted = savedInstanceState.getBoolean("is_lesson_completed", false);
            hasReachedLastPage = savedInstanceState.getBoolean("has_reached_last_page", false);
        }
    }

    /**
     * Initialize document-specific views
     */
    private void initializeDocumentViews() {
        pdfView = findViewById(R.id.pdfView);
        pdfLoadingProgressBar = findViewById(R.id.pdfLoadingProgressBar);
        pdfErrorView = findViewById(R.id.pdfErrorView);
        lessonTitleTextView = findViewById(R.id.lessonTitleTextView);
        lessonDescriptionTextView = findViewById(R.id.lessonDescriptionTextView);
        pagesTextView = findViewById(R.id.pagesTextView);
        zoomLevelTextView = findViewById(R.id.zoomLevelTextView);
        zoomInButton = findViewById(R.id.zoomInButton);
        zoomOutButton = findViewById(R.id.zoomOutButton);
        resourcesRecyclerView = findViewById(R.id.resourcesRecyclerView);
        emptyResourcesTextView = findViewById(R.id.emptyResourcesTextView);
        
        // Setup resources recycler view
        resourcesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        resourceAdapter = new ResourceAdapter(this);
        resourcesRecyclerView.setAdapter(resourceAdapter);
        
        // Setup zoom controls
        zoomInButton.setOnClickListener(v -> zoomIn());
        zoomOutButton.setOnClickListener(v -> zoomOut());
        
        // Setup retry button in error view
        findViewById(R.id.pdfRetryButton).setOnClickListener(v -> {
            pdfErrorView.setVisibility(View.GONE);
            loadDocument();
        });
    }

    @Override
    protected void initializeLessonContent() {
        // Set lesson info
        lessonTitleTextView.setText(lesson.getTitle());
        lessonDescriptionTextView.setText(lesson.getDescription());
        
        // Load PDF document
        loadDocument();
        
        // Update action button text based on completion status
        checkLessonCompletionStatus();
    }

    /**
     * Check if this lesson has already been completed
     */
    private void checkLessonCompletionStatus() {
        String userId = getCurrentUserId();
        
        courseService.getLessonProgress(userId, lessonId, progress -> {
            if (progress != null && progress.getProgress() >= 100) {
                isLessonCompleted = true;
                actionButton.setText(R.string.continue_to_next);
            } else {
                isLessonCompleted = false;
                actionButton.setText(R.string.complete_lesson);
            }
        }, e -> {
            // If we can't get progress, assume not completed
            isLessonCompleted = false;
            actionButton.setText(R.string.complete_lesson);
        });
    }

    /**
     * Load PDF document from Firebase Storage
     */
    private void loadDocument() {
        if (lesson == null || lesson.getDocumentUrl() == null) {
            showError("Invalid document URL");
            return;
        }
        
        // Show loading state
        pdfLoadingProgressBar.setVisibility(View.VISIBLE);
        pdfView.setVisibility(View.INVISIBLE);
        pdfErrorView.setVisibility(View.GONE);
        
        try {
            // Create temporary file to store the PDF
            pdfFile = File.createTempFile("document_" + lessonId, ".pdf", getCacheDir());
            
            // Get reference to file in Firebase Storage
            StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(lesson.getDocumentUrl());
            
            // Download file to local storage
            storageRef.getFile(pdfFile)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Load PDF file
                        loadPdfFromFile();
                    })
                    .addOnFailureListener(exception -> {
                        // Handle download failure
                        pdfLoadingProgressBar.setVisibility(View.GONE);
                        pdfErrorView.setVisibility(View.VISIBLE);
                        exception.printStackTrace();
                    });
        } catch (IOException e) {
            // Handle error creating temporary file
            pdfLoadingProgressBar.setVisibility(View.GONE);
            pdfErrorView.setVisibility(View.VISIBLE);
            e.printStackTrace();
        }
    }

    /**
     * Load PDF from downloaded file
     */
    private void loadPdfFromFile() {
        if (pdfFile != null && pdfFile.exists()) {
            pdfView.fromFile(pdfFile)
                    .defaultPage(currentPage)
                    .onPageChange(this)
                    .enableAnnotationRendering(true)
                    .onLoad(this)
                    .onError(this)
                    .scrollHandle(new DefaultScrollHandle(this))
                    .spacing(10)
                    .load();
        } else {
            // Handle missing file
            pdfLoadingProgressBar.setVisibility(View.GONE);
            pdfErrorView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Zoom in PDF viewer
     */
    private void zoomIn() {
        if (currentZoom < MAX_ZOOM) {
            currentZoom += ZOOM_STEP;
            pdfView.zoomTo(currentZoom);
            updateZoomLevel();
        }
    }

    /**
     * Zoom out PDF viewer
     */
    private void zoomOut() {
        if (currentZoom > MIN_ZOOM) {
            currentZoom -= ZOOM_STEP;
            pdfView.zoomTo(currentZoom);
            updateZoomLevel();
        }
    }

    /**
     * Update zoom level display
     */
    private void updateZoomLevel() {
        int zoomPercent = (int) (currentZoom * 100);
        zoomLevelTextView.setText(String.format("%d%%", zoomPercent));
    }

    /**
     * Update page indicator
     */
    private void updatePageInfo() {
        pagesTextView.setText(getString(R.string.page_of_total, currentPage + 1, pageCount));
        
        // Check if we've reached the last page
        if (currentPage == pageCount - 1) {
            hasReachedLastPage = true;
            
            // Auto-complete lesson when reaching the last page
            if (!isLessonCompleted) {
                markLessonAsReadyToComplete();
            }
        }
        
        // Update progress on server
        updateProgressOnServer();
    }

    /**
     * Mark lesson as ready to complete (change button text)
     */
    private void markLessonAsReadyToComplete() {
        if (!isLessonCompleted) {
            actionButton.setText(R.string.complete_lesson);
            actionButton.setEnabled(true);
        }
    }

    /**
     * Update lesson progress on server
     */
    private void updateProgressOnServer() {
        if (pageCount > 0) {
            String userId = getCurrentUserId();
            int progress = (int) (((float) (currentPage + 1) / pageCount) * 100);
            
            progressManager.updateLessonProgress(userId, lessonId, courseId, progress, 
                () -> {
                    // Success - silent update
                },
                error -> {
                    // Failed to update progress - silent failure
                }
            );
        }
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        currentPage = page;
        this.pageCount = pageCount;
        updatePageInfo();
    }

    @Override
    public void loadComplete(int nbPages) {
        pageCount = nbPages;
        
        // Hide loading, show PDF
        pdfLoadingProgressBar.setVisibility(View.GONE);
        pdfView.setVisibility(View.VISIBLE);
        
        // Update page info
        updatePageInfo();
        
        // Set initial zoom
        pdfView.zoomTo(currentZoom);
        updateZoomLevel();
    }

    @Override
    public void onPageError(int page, Throwable t) {
        // Show error view
        pdfLoadingProgressBar.setVisibility(View.GONE);
        pdfErrorView.setVisibility(View.VISIBLE);
        
        // Log error
        t.printStackTrace();
        
        Toast.makeText(this, R.string.pdf_page_error, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActionButtonClicked() {
        if (isLessonCompleted) {
            // Already completed, navigate to next lesson
            navigateToNextLesson();
        } else if (hasReachedLastPage) {
            // Reached last page, mark as complete
            markLessonComplete();
        } else {
            // Not at last page yet, show message
            Toast.makeText(this, R.string.read_to_end_to_complete, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void updateResourcesView() {
        if (resources != null && !resources.isEmpty()) {
            resourceAdapter.setResources(resources);
            resourcesRecyclerView.setVisibility(View.VISIBLE);
            emptyResourcesTextView.setVisibility(View.GONE);
        } else {
            resourcesRecyclerView.setVisibility(View.GONE);
            emptyResourcesTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        
        // Save state
        outState.putInt("current_page", currentPage);
        outState.putFloat("current_zoom", currentZoom);
        outState.putBoolean("is_lesson_completed", isLessonCompleted);
        outState.putBoolean("has_reached_last_page", hasReachedLastPage);
    }

    @Override
    protected void onDestroy() {
        // Clean up temporary file
        if (pdfFile != null && pdfFile.exists()) {
            pdfFile.delete();
        }
        
        super.onDestroy();
    }
}