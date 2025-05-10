package com.fast.mentor;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fast.mentor.R;
import com.fast.mentor.DatabaseHelper;
import com.fast.mentor.Content;
import com.fast.mentor.ContentTracker;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Activity for viewing PDF documents with automatic completion tracking
 */
public class PDFViewerActivity extends AppCompatActivity implements 
        OnPageChangeListener, OnLoadCompleteListener, OnPageErrorListener {
    
    private static final String TAG = "PDFViewerActivity";
    private static final int POSITION_UPDATE_INTERVAL_MS = 10000; // Update every 10 seconds
    
    private PDFView pdfView;
    private TextView titleTextView;
    private TextView pageInfoTextView;
    private ImageButton backButton;
    private ImageButton downloadButton;
    
    private int contentId;
    private int userId;
    private String pdfUrl;
    private String pdfTitle;
    private int currentPage = 0;
    private int pageCount = 0;
    private boolean isPdfComplete = false;
    private boolean wasUserCompleting = false; // To prevent multiple completion events
    
    private ContentTracker contentTracker;
    private DatabaseHelper dbHelper;
    private Handler handler;
    private final Runnable positionUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            updatePdfPosition();
            // Schedule the next update
            handler.postDelayed(this, POSITION_UPDATE_INTERVAL_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);
        
        // Initialize UI components
        pdfView = findViewById(R.id.pdfView);
        titleTextView = findViewById(R.id.pdf_title);
        pageInfoTextView = findViewById(R.id.page_info);
        backButton = findViewById(R.id.back_button);
        downloadButton = findViewById(R.id.download_button);
        
        // Get data from intent
        Intent intent = getIntent();
        contentId = intent.getIntExtra("content_id", -1);
        userId = intent.getIntExtra("user_id", -1); // Get from login session in real app
        pdfUrl = intent.getStringExtra("pdf_url");
        pdfTitle = intent.getStringExtra("pdf_title");
        
        if (contentId == -1 || userId == -1 || pdfUrl == null) {
            Toast.makeText(this, "Error loading PDF", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Set title
        titleTextView.setText(pdfTitle != null ? pdfTitle : "PDF Document");
        
        // Initialize helpers
        contentTracker = ContentTracker.getInstance(this);
        dbHelper = DatabaseHelper.getInstance(this);
        handler = new Handler();
        
        // Setup button listeners
        backButton.setOnClickListener(v -> onBackPressed());
        downloadButton.setOnClickListener(v -> downloadPdf());
        
        // Load and display PDF
        loadPdfFromUrl(pdfUrl);
        
        // Start tracking content consumption
        contentTracker.startTracking(userId, contentId, "pdf");
        
        // Schedule regular position updates
        handler.postDelayed(positionUpdateRunnable, POSITION_UPDATE_INTERVAL_MS);
    }
    
    /**
     * Download PDF file from URL and display it
     */
    private void loadPdfFromUrl(String pdfUrl) {
        // Show loading state
        Toast.makeText(this, "Loading PDF...", Toast.LENGTH_SHORT).show();
        
        // In a real app, this would download the PDF to cache and load it
        // For demonstration, let's assume we have the PDF already downloaded
        // and load it from local resources
        new Thread(() -> {
            try {
                // Create a temporary file
                File pdfFile = new File(getCacheDir(), "temp.pdf");
                
                // Download the file
                URL url = new URL(pdfUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    showError("Error downloading PDF: " + connection.getResponseMessage());
                    return;
                }
                
                // Create output stream
                InputStream input = connection.getInputStream();
                OutputStream output = new FileOutputStream(pdfFile);
                
                // Copy the file
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
                
                output.close();
                input.close();
                
                // Display PDF on the UI thread
                runOnUiThread(() -> displayPdf(pdfFile));
                
            } catch (IOException e) {
                e.printStackTrace();
                showError("Error loading PDF: " + e.getMessage());
            }
        }).start();
    }
    
    /**
     * Display PDF file
     */
    private void displayPdf(File pdfFile) {
        pdfView.fromFile(pdfFile)
                .defaultPage(0)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .onPageError(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .load();
    }
    
    /**
     * Download the PDF file for offline viewing
     */
    private void downloadPdf() {
        // In a real app, this would save the PDF to external storage with proper name
        Toast.makeText(this, "PDF downloaded", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Update PDF position in tracker
     */
    private void updatePdfPosition() {
        if (pageCount > 0) {
            // Update tracker with current page and total pages
            contentTracker.updatePosition(userId, contentId, currentPage, pageCount);
            
            // Check for auto-completion (if user has reached the last page)
            if (!isPdfComplete && currentPage == pageCount - 1) {
                checkForCompletion();
            }
        }
    }
    
    /**
     * Check if PDF should be marked as complete
     */
    private void checkForCompletion() {
        if (!isPdfComplete && !wasUserCompleting && currentPage == pageCount - 1) {
            wasUserCompleting = true;
            
            // The ContentTracker handles completion checks based on position
            // but for PDFs we also want to confirm the user stayed on the last page
            // for at least a few seconds
            new Handler().postDelayed(() -> {
                // Only mark as complete if still on the last page
                if (currentPage == pageCount - 1) {
                    markPdfComplete();
                }
                wasUserCompleting = false;
            }, 3000); // Wait 3 seconds on last page to confirm completion
        }
    }
    
    /**
     * Mark PDF as completed
     */
    private void markPdfComplete() {
        if (!isPdfComplete) {
            // Mark as completed in tracker
            contentTracker.markAsCompleted(userId, contentId);
            
            // Show completion toast
            Toast.makeText(this, "PDF completed!", Toast.LENGTH_SHORT).show();
            
            // Update state
            isPdfComplete = true;
            
            // In a real app, you might navigate to the next lesson
            // or show a completion dialog with options
        }
    }
    
    /**
     * Show error message on UI thread
     */
    private void showError(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_LONG).show());
    }
    
    //
    // PDF Viewer Listener Methods
    //
    
    @Override
    public void onPageChanged(int page, int pageCount) {
        currentPage = page;
        this.pageCount = pageCount;
        
        // Update page info text
        pageInfoTextView.setText((page + 1) + " / " + pageCount);
        
        // Log page change for debugging
        Log.d(TAG, "Page changed to " + (page + 1) + " of " + pageCount);
        
        // Update content tracker with new position
        contentTracker.updatePosition(userId, contentId, page, pageCount);
        
        // Check for completion
        if (page == pageCount - 1) {
            checkForCompletion();
        }
    }
    
    @Override
    public void loadComplete(int nbPages) {
        pageCount = nbPages;
        
        // Update page info text
        pageInfoTextView.setText((currentPage + 1) + " / " + pageCount);
        
        // Log for debugging
        Log.d(TAG, "PDF loaded with " + nbPages + " pages");
        
        // Update content tracker with total pages
        contentTracker.updatePosition(userId, contentId, currentPage, pageCount);
    }
    
    @Override
    public void onPageError(int page, Throwable t) {
        Log.e(TAG, "Error loading page " + page, t);
        Toast.makeText(this, "Error loading page " + (page + 1), Toast.LENGTH_SHORT).show();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Update position when pausing
        updatePdfPosition();
    }
    
    @Override
    protected void onDestroy() {
        // Clean up
        if (handler != null) {
            handler.removeCallbacks(positionUpdateRunnable);
        }
        
        // Final position update and stop tracking
        updatePdfPosition();
        contentTracker.stopTracking(userId, contentId);
        
        super.onDestroy();
    }
}