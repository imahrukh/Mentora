package com.fast.mentor;

import java.text.DecimalFormat;

/**
 * Utility class for file-related operations.
 */
public class FileUtils {

    private static final long KB = 1024;
    private static final long MB = KB * 1024;
    private static final long GB = MB * 1024;
    
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");

    /**
     * Format file size in bytes to human-readable format
     */
    public static String formatFileSize(long size) {
        if (size < KB) {
            return size + " B";
        } else if (size < MB) {
            return DECIMAL_FORMAT.format((double) size / KB) + " KB";
        } else if (size < GB) {
            return DECIMAL_FORMAT.format((double) size / MB) + " MB";
        } else {
            return DECIMAL_FORMAT.format((double) size / GB) + " GB";
        }
    }
    
    /**
     * Get file extension from URL or filename
     */
    public static String getFileExtension(String path) {
        if (path == null) {
            return "";
        }
        
        int lastDotIndex = path.lastIndexOf('.');
        if (lastDotIndex >= 0 && lastDotIndex < path.length() - 1) {
            return path.substring(lastDotIndex + 1).toLowerCase();
        }
        
        return "";
    }
    
    /**
     * Get MIME type based on file extension
     */
    public static String getMimeType(String filename) {
        String extension = getFileExtension(filename);
        
        switch (extension) {
            case "pdf":
                return "application/pdf";
            case "doc":
            case "docx":
                return "application/msword";
            case "xls":
            case "xlsx":
                return "application/vnd.ms-excel";
            case "ppt":
            case "pptx":
                return "application/vnd.ms-powerpoint";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "mp4":
                return "video/mp4";
            case "mov":
                return "video/quicktime";
            case "mp3":
                return "audio/mpeg";
            case "txt":
                return "text/plain";
            case "html":
            case "htm":
                return "text/html";
            case "zip":
                return "application/zip";
            default:
                return "application/octet-stream";
        }
    }
    
    /**
     * Get resource type based on file extension
     */
    public static String getResourceTypeFromExtension(String filename) {
        String extension = getFileExtension(filename);
        
        switch (extension) {
            case "pdf":
                return "pdf";
            case "doc":
            case "docx":
            case "txt":
            case "rtf":
                return "document";
            case "jpg":
            case "jpeg":
            case "png":
            case "gif":
            case "bmp":
                return "image";
            case "mp4":
            case "mov":
            case "avi":
            case "wmv":
                return "video";
            case "mp3":
            case "wav":
            case "ogg":
            case "m4a":
                return "audio";
            case "java":
            case "kt":
            case "js":
            case "py":
            case "html":
            case "css":
            case "xml":
            case "json":
                return "code";
            default:
                return "file";
        }
    }
    
    /**
     * Generate a safe file name by removing invalid characters
     */
    public static String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return "file";
        }
        
        // Replace invalid characters with underscore
        return fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}