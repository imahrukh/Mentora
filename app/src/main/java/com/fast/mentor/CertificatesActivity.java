package com.fast.mentor;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.fast.mentor.R;
import com.fast.mentor.CertificateManager;
import com.fast.mentor.Certificate;
import com.fast.mentor.Course;
import com.fast.mentor.CertificateAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity for displaying and managing user certificates
 */
public class CertificatesActivity extends AppCompatActivity implements CertificateAdapter.CertificateClickListener {
    private static final String TAG = "CertificatesActivity";
    
    private RecyclerView certificatesRecyclerView;
    private TextView emptyStateTextView;
    private ImageButton backButton;
    
    private CertificateAdapter adapter;
    private CertificateManager certificateManager;
    private int userId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_certificates);
        
        // Initialize views
        certificatesRecyclerView = findViewById(R.id.certificates_recycler_view);
        emptyStateTextView = findViewById(R.id.empty_state_text);
        backButton = findViewById(R.id.back_button);
        
        // Set up back button listener
        backButton.setOnClickListener(v -> finish());
        
        // Get user ID from intent or login session
        userId = getIntent().getIntExtra("user_id", -1);
        if (userId == -1) {
            // In a real app, get from login session
            userId = 1; // Temporary default for demo
        }
        
        // Initialize certificate manager
        certificateManager = CertificateManager.getInstance(this);
        
        // Set up RecyclerView
        certificatesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CertificateAdapter(new ArrayList<>(), this);
        certificatesRecyclerView.setAdapter(adapter);
        
        // Load certificates
        loadCertificates();
    }
    
    /**
     * Load user certificates from database
     */
    private void loadCertificates() {
        List<Certificate> certificates = certificateManager.getUserCertificates(userId);
        
        if (certificates == null || certificates.isEmpty()) {
            showEmptyState(true);
        } else {
            showEmptyState(false);
            adapter.setCertificates(certificates);
        }
    }
    
    /**
     * Show or hide empty state message
     */
    private void showEmptyState(boolean show) {
        if (show) {
            emptyStateTextView.setVisibility(View.VISIBLE);
            certificatesRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateTextView.setVisibility(View.GONE);
            certificatesRecyclerView.setVisibility(View.VISIBLE);
        }
    }
    
    @Override
    public void onViewCertificate(Certificate certificate) {
        // Open certificate PDF in viewer
        try {
            File file = new File(certificate.getDownloadUrl());
            if (!file.exists()) {
                Toast.makeText(this, "Certificate file not found", Toast.LENGTH_SHORT).show();
                return;
            }
            
            Uri fileUri = FileProvider.getUriForFile(
                    this, 
                    getPackageName() + ".fileprovider", 
                    file);
            
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(fileUri, "application/pdf");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening certificate", e);
            Toast.makeText(this, "Error opening certificate: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onShareCertificate(Certificate certificate) {
        try {
            File file = new File(certificate.getDownloadUrl());
            if (!file.exists()) {
                Toast.makeText(this, "Certificate file not found", Toast.LENGTH_SHORT).show();
                return;
            }
            
            Uri fileUri = FileProvider.getUriForFile(
                    this, 
                    getPackageName() + ".fileprovider", 
                    file);
            
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            
            Course course = certificate.getCourse();
            String courseName = course != null ? course.getTitle() : "course";
            
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My Mentora Certificate");
            shareIntent.putExtra(Intent.EXTRA_TEXT, 
                    "I've completed the " + courseName + " course on Mentora! " +
                    "Check out my certificate of completion.");
            
            shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            // Mark certificate as shared
            certificateManager.shareCertificate(certificate.getId());
            
            startActivity(Intent.createChooser(shareIntent, "Share Certificate"));
        } catch (Exception e) {
            Log.e(TAG, "Error sharing certificate", e);
            Toast.makeText(this, "Error sharing certificate: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onDownloadCertificate(Certificate certificate) {
        // Since we already have the file locally, just show a toast
        // In a real app, this might copy to Downloads folder
        Toast.makeText(this, "Certificate downloaded successfully", Toast.LENGTH_SHORT).show();
    }
}