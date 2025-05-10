package com.fast.mentor;

import android.content.Context;
import android.util.Log;

import com.fast.mentor.DatabaseHelper;
import com.fast.mentor.Certificate;
import com.fast.mentor.Course;
import com.fast.mentor.User;

import java.util.Date;
import java.util.List;

/**
 * Manages the creation, storage, and retrieval of certificates
 */
public class CertificateManager {
    private static final String TAG = "CertificateManager";
    
    private static CertificateManager instance;
    private final Context context;
    private final DatabaseHelper dbHelper;
    private final CertificateGenerator certificateGenerator;
    
    private CertificateManager(Context context) {
        this.context = context.getApplicationContext();
        this.dbHelper = DatabaseHelper.getInstance(context);
        this.certificateGenerator = new CertificateGenerator(context);
    }
    
    public static synchronized CertificateManager getInstance(Context context) {
        if (instance == null && context != null) {
            instance = new CertificateManager(context);
        }
        return instance;
    }
    
    /**
     * Create a certificate for course completion
     * 
     * @param userId User ID who completed the course
     * @param courseId Course ID that was completed
     * @return The created certificate or null if creation failed
     */
    public Certificate createCertificate(int userId, int courseId) {
        Log.d(TAG, "Creating certificate for user " + userId + ", course " + courseId);
        
        // Check if certificate already exists
        Certificate existingCertificate = dbHelper.getCertificate(userId, courseId);
        if (existingCertificate != null) {
            Log.d(TAG, "Certificate already exists, returning existing one");
            return existingCertificate;
        }
        
        // Get user and course information
        User user = dbHelper.getUser(userId);
        Course course = dbHelper.getCourse(courseId);
        
        if (user == null || course == null) {
            Log.e(TAG, "Failed to retrieve user or course data");
            return null;
        }
        
        // Check if course is actually completed
        boolean isCompleted = dbHelper.isCourseCompleted(userId, courseId);
        if (!isCompleted) {
            Log.e(TAG, "Cannot create certificate for incomplete course");
            return null;
        }
        
        // Generate certificate
        Certificate certificate = certificateGenerator.generateCertificate(user, course, new Date());
        
        // Save to database
        dbHelper.saveCertificate(certificate);
        
        return certificate;
    }
    
    /**
     * Get all certificates for a user
     * 
     * @param userId User ID
     * @return List of certificates
     */
    public List<Certificate> getUserCertificates(int userId) {
        return dbHelper.getUserCertificates(userId);
    }
    
    /**
     * Get a specific certificate
     * 
     * @param userId User ID
     * @param courseId Course ID
     * @return Certificate or null if not found
     */
    public Certificate getCertificate(int userId, int courseId) {
        return dbHelper.getCertificate(userId, courseId);
    }
    
    /**
     * Mark a certificate as shared
     * 
     * @param certificateId Certificate ID
     * @return Updated certificate or null if not found
     */
    public Certificate shareCertificate(int certificateId) {
        Certificate certificate = dbHelper.getCertificateById(certificateId);
        if (certificate != null) {
            certificate.setShared(true);
            dbHelper.updateCertificate(certificate);
            return certificate;
        }
        return null;
    }
    
    /**
     * Check if a certificate can be created for a course
     * 
     * @param userId User ID
     * @param courseId Course ID
     * @return true if a certificate can be created
     */
    public boolean canCreateCertificate(int userId, int courseId) {
        // Check if course is completed
        boolean isCompleted = dbHelper.isCourseCompleted(userId, courseId);
        
        // Check if certificate already exists
        Certificate existingCertificate = dbHelper.getCertificate(userId, courseId);
        
        return isCompleted && existingCertificate == null;
    }
}