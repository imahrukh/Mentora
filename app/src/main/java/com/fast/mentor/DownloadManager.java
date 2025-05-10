package com.fast.mentor;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;

import com.fast.mentor.R;
import com.fast.mentor.model.Resource;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages downloading and storing of lesson resources.
 */
public class DownloadManager {

    private static final String DOWNLOADS_FOLDER = "Mentora";
    private static final String NOTIFICATION_CHANNEL_ID = "downloads_channel";
    private static final int NOTIFICATION_ID_BASE = 1000;

    private final Context context;
    private final NotificationManager notificationManager;
    private final Map<String, FileDownloadTask> activeDownloads = new HashMap<>();

    /**
     * Callback interface for download progress and completion
     */
    public interface DownloadCallback {
        void onDownloadStarted();
        void onDownloadProgress(int progress);
        void onDownloadComplete(Uri fileUri);
        void onDownloadFailed(Exception error);
    }

    /**
     * Constructor
     */
    public DownloadManager(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    /**
     * Create notification channel for download notifications (Android 8.0+)
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    context.getString(R.string.download_channel_name),
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription(context.getString(R.string.download_channel_description));
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Download a resource file from Firebase Storage
     */
    public void downloadResource(Resource resource, DownloadCallback callback) {
        // Check if download is already in progress
        if (activeDownloads.containsKey(resource.getId())) {
            // Download already active
            callback.onDownloadStarted();
            return;
        }
        
        // Create download directory if it doesn't exist
        File downloadDir = getDownloadDirectory();
        if (!downloadDir.exists()) {
            boolean created = downloadDir.mkdirs();
            if (!created) {
                callback.onDownloadFailed(new Exception("Could not create download directory"));
                return;
            }
        }
        
        // Create file to store download
        File downloadFile = new File(downloadDir, getFileName(resource));
        
        // Get reference to file in Firebase Storage
        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(resource.getUrl());
        
        // Start download
        FileDownloadTask downloadTask = storageRef.getFile(downloadFile);
        activeDownloads.put(resource.getId(), downloadTask);
        
        // Create and show notification
        int notificationId = NOTIFICATION_ID_BASE + resource.getId().hashCode();
        Notification notification = createDownloadNotification(resource.getTitle(), 0);
        notificationManager.notify(notificationId, notification);
        
        // Notify callback
        callback.onDownloadStarted();
        
        // Monitor download progress
        downloadTask.addOnProgressListener(taskSnapshot -> {
            int progress = (int) ((100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount());
            
            // Update notification
            Notification progressNotification = createDownloadNotification(resource.getTitle(), progress);
            notificationManager.notify(notificationId, progressNotification);
            
            // Notify callback
            callback.onDownloadProgress(progress);
        });
        
        // Handle download success
        downloadTask.addOnSuccessListener(taskSnapshot -> {
            // Remove from active downloads
            activeDownloads.remove(resource.getId());
            
            // Create success notification
            Notification successNotification = createDownloadCompleteNotification(resource.getTitle());
            notificationManager.notify(notificationId, successNotification);
            
            // Get content Uri using FileProvider
            Uri fileUri = FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".fileprovider",
                    downloadFile);
            
            // Notify callback
            callback.onDownloadComplete(fileUri);
        });
        
        // Handle download failure
        downloadTask.addOnFailureListener(exception -> {
            // Remove from active downloads
            activeDownloads.remove(resource.getId());
            
            // Create failure notification
            Notification failureNotification = createDownloadFailedNotification(resource.getTitle());
            notificationManager.notify(notificationId, failureNotification);
            
            // Delete partial file
            if (downloadFile.exists()) {
                downloadFile.delete();
            }
            
            // Notify callback
            callback.onDownloadFailed(exception);
        });
    }

    /**
     * Cancel an active download
     */
    public void cancelDownload(Resource resource) {
        FileDownloadTask task = activeDownloads.get(resource.getId());
        if (task != null) {
            task.cancel();
            activeDownloads.remove(resource.getId());
            
            // Update notification
            int notificationId = NOTIFICATION_ID_BASE + resource.getId().hashCode();
            Notification cancelledNotification = createDownloadCancelledNotification(resource.getTitle());
            notificationManager.notify(notificationId, cancelledNotification);
            
            // Delete partial file
            File downloadFile = new File(getDownloadDirectory(), getFileName(resource));
            if (downloadFile.exists()) {
                downloadFile.delete();
            }
        }
    }

    /**
     * Check if a resource has been downloaded
     */
    public boolean isResourceDownloaded(Resource resource) {
        File file = new File(getDownloadDirectory(), getFileName(resource));
        return file.exists() && file.length() > 0;
    }

    /**
     * Get Uri for a downloaded resource file
     */
    public Uri getResourceFileUri(Resource resource) {
        File file = new File(getDownloadDirectory(), getFileName(resource));
        if (file.exists()) {
            return FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".fileprovider",
                    file);
        }
        return null;
    }

    /**
     * Get download directory
     */
    private File getDownloadDirectory() {
        return new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), DOWNLOADS_FOLDER);
    }

    /**
     * Generate safe filename for a resource
     */
    private String getFileName(Resource resource) {
        // Use resource ID and original filename to create unique name
        String originalName = getOriginalFileName(resource.getUrl());
        String extension = getFileExtension(originalName);
        
        return resource.getId() + "_" + sanitizeFileName(resource.getTitle()) + extension;
    }

    /**
     * Extract original filename from URL
     */
    private String getOriginalFileName(String url) {
        int lastSlashIndex = url.lastIndexOf('/');
        if (lastSlashIndex != -1 && lastSlashIndex < url.length() - 1) {
            return url.substring(lastSlashIndex + 1);
        }
        return "file";
    }

    /**
     * Get file extension including dot
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex != -1) {
            return filename.substring(lastDotIndex);
        }
        return "";
    }

    /**
     * Sanitize filename by removing invalid characters
     */
    private String sanitizeFileName(String name) {
        return name.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    /**
     * Create download in progress notification
     */
    private Notification createDownloadNotification(String title, int progress) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(context.getString(R.string.downloading_file))
                .setContentText(title)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setProgress(100, progress, false)
                .setOngoing(true);
        
        return builder.build();
    }

    /**
     * Create download complete notification
     */
    private Notification createDownloadCompleteNotification(String title) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(context.getString(R.string.download_complete))
                .setContentText(title)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setAutoCancel(true);
        
        return builder.build();
    }

    /**
     * Create download failed notification
     */
    private Notification createDownloadFailedNotification(String title) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(context.getString(R.string.download_failed))
                .setContentText(title)
                .setSmallIcon(android.R.drawable.stat_notify_error)
                .setAutoCancel(true);
        
        return builder.build();
    }

    /**
     * Create download cancelled notification
     */
    private Notification createDownloadCancelledNotification(String title) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(context.getString(R.string.download_cancelled))
                .setContentText(title)
                .setSmallIcon(android.R.drawable.stat_notify_error)
                .setAutoCancel(true);
        
        return builder.build();
    }
}