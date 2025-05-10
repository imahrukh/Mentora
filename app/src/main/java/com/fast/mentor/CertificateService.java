package com.fast.mentor;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.fast.mentor.R;
import com.fast.mentor.DatabaseHelper;
import com.fast.mentor.Certificate;
import com.fast.mentor.Course;
import com.fast.mentor.CertificatesActivity;

/**
 * Service for automatically generating and awarding certificates to users
 * when they complete courses
 */
public class CertificateService {
    private static final String TAG = "CertificateService";
    private static final String CHANNEL_ID = "certificate_channel";
    private static final int NOTIFICATION_ID = 1001;
    
    private static CertificateService instance;
    private final Context context;
    private final CertificateManager certificateManager;
    private final DatabaseHelper dbHelper;
    
    private CertificateService(Context context) {
        this.context = context.getApplicationContext();
        this.certificateManager = CertificateManager.getInstance(context);
        this.dbHelper = DatabaseHelper.getInstance(context);
        
        // Create notification channel for certificate awards
        createNotificationChannel();
    }
    
    public static synchronized CertificateService getInstance(Context context) {
        if (instance == null && context != null) {
            instance = new CertificateService(context);
        }
        return instance;
    }
    
    /**
     * Process course completion and award certificate if eligible
     * 
     * @param userId User ID
     * @param courseId Course ID
     * @return true if certificate was awarded
     */
    public boolean processCourseCompletion(int userId, int courseId) {
        Log.d(TAG, "Processing course completion for user " + userId + ", course " + courseId);
        
        // Check if course is actually completed
        boolean isCompleted = dbHelper.isCourseCompleted(userId, courseId);
        if (!isCompleted) {
            Log.d(TAG, "Course not fully completed yet");
            return false;
        }
        
        // Check if certificate already exists
        Certificate existingCertificate = certificateManager.getCertificate(userId, courseId);
        if (existingCertificate != null) {
            Log.d(TAG, "Certificate already awarded");
            return false;
        }
        
        // Generate and award certificate
        Certificate certificate = certificateManager.createCertificate(userId, courseId);
        if (certificate != null) {
            // Show notification to user
            showCertificateNotification(certificate);
            return true;
        }
        
        return false;
    }
    
    /**
     * Create notification channel for certificate awards (required for Android 8.0+)
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Certificates";
            String description = "Notifications for course completion certificates";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    
    /**
     * Show notification when a certificate is awarded
     */
    private void showCertificateNotification(Certificate certificate) {
        Course course = certificate.getCourse();
        if (course == null) {
            Log.e(TAG, "Missing course information for certificate notification");
            return;
        }
        
        // Create intent to open CertificatesActivity
        Intent intent = new Intent(context, CertificatesActivity.class);
        intent.putExtra("user_id", certificate.getUserId());
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        
        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_certificate)
                .setContentTitle("Congratulations!")
                .setContentText("You earned a certificate for completing " + course.getTitle())
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("You've successfully completed the course \"" + 
                                course.getTitle() + "\" and earned a certificate. " +
                                "Tap to view and share your achievement!"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setColor(0xFFD11F5D) // Pink-red color
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        
        // Show notification
        NotificationManager notificationManager = 
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
        
        Log.d(TAG, "Certificate notification shown for " + course.getTitle());
    }
}