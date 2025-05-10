package com.fast.mentor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.util.Log;

import com.fast.mentor.Certificate;
import com.fast.mentor.Course;
import com.fast.mentor.User;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Generates PDF certificates when users complete courses
 */
public class CertificateGenerator {
    private static final String TAG = "CertificateGenerator";
    
    // PDF page dimensions (A4 landscape, in points)
    private static final int PAGE_WIDTH = 842;  // 11.69 inches * 72 dpi
    private static final int PAGE_HEIGHT = 595; // 8.27 inches * 72 dpi
    
    private final Context context;
    
    public CertificateGenerator(Context context) {
        this.context = context.getApplicationContext();
    }
    
    /**
     * Generate a certificate PDF for a completed course
     * 
     * @param user The user who completed the course
     * @param course The completed course
     * @param completionDate When the course was completed
     * @return Certificate object with the file path
     */
    public Certificate generateCertificate(User user, Course course, Date completionDate) {
        Log.d(TAG, "Generating certificate for user " + user.getId() + ", course " + course.getId());
        
        // Create new certificate object
        Certificate certificate = new Certificate(user.getId(), course.getId());
        certificate.setCourse(course);
        certificate.setIssuedAt(completionDate);
        
        try {
            // Create PDF document
            PdfDocument document = new PdfDocument();
            
            // Create a page
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            
            // Get canvas for drawing
            Canvas canvas = page.getCanvas();
            
            // Draw certificate content
            drawCertificate(canvas, user, course, certificate);
            
            // Finish the page
            document.finishPage(page);
            
            // Save the document
            String filePath = savePdfDocument(document, certificate.getCertificateId());
            document.close();
            
            if (filePath != null) {
                certificate.setDownloadUrl(filePath);
                Log.d(TAG, "Certificate generated successfully: " + filePath);
            } else {
                Log.e(TAG, "Failed to save certificate PDF");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error generating certificate", e);
        }
        
        return certificate;
    }
    
    /**
     * Draw the certificate content on the canvas
     */
    private void drawCertificate(Canvas canvas, User user, Course course, Certificate certificate) {
        // Fill background
        Paint backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.WHITE);
        canvas.drawRect(0, 0, PAGE_WIDTH, PAGE_HEIGHT, backgroundPaint);
        
        // Draw border
        Paint borderPaint = new Paint();
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(8);
        borderPaint.setColor(Color.rgb(209, 31, 93)); // Pink-red color
        canvas.drawRect(40, 40, PAGE_WIDTH - 40, PAGE_HEIGHT - 40, borderPaint);
        
        // Draw decorative corners
        int cornerSize = 40;
        Paint cornerPaint = new Paint();
        cornerPaint.setStyle(Paint.Style.FILL);
        cornerPaint.setColor(Color.rgb(209, 31, 93)); // Pink-red color
        
        // Top-left corner
        canvas.drawRect(40, 40, 40 + cornerSize, 40 + 8, cornerPaint);
        canvas.drawRect(40, 40, 40 + 8, 40 + cornerSize, cornerPaint);
        
        // Top-right corner
        canvas.drawRect(PAGE_WIDTH - 40 - cornerSize, 40, PAGE_WIDTH - 40, 40 + 8, cornerPaint);
        canvas.drawRect(PAGE_WIDTH - 40 - 8, 40, PAGE_WIDTH - 40, 40 + cornerSize, cornerPaint);
        
        // Bottom-left corner
        canvas.drawRect(40, PAGE_HEIGHT - 40 - 8, 40 + cornerSize, PAGE_HEIGHT - 40, cornerPaint);
        canvas.drawRect(40, PAGE_HEIGHT - 40 - cornerSize, 40 + 8, PAGE_HEIGHT - 40, cornerPaint);
        
        // Bottom-right corner
        canvas.drawRect(PAGE_WIDTH - 40 - cornerSize, PAGE_HEIGHT - 40 - 8, PAGE_WIDTH - 40, PAGE_HEIGHT - 40, cornerPaint);
        canvas.drawRect(PAGE_WIDTH - 40 - 8, PAGE_HEIGHT - 40 - cornerSize, PAGE_WIDTH - 40, PAGE_HEIGHT - 40, cornerPaint);
        
        // Draw logo/title
        Paint titlePaint = new Paint();
        titlePaint.setColor(Color.rgb(209, 31, 93)); // Pink-red color
        titlePaint.setTextSize(60);
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        titlePaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("MENTORA", PAGE_WIDTH / 2, 120, titlePaint);
        
        // Draw "Certificate of Completion" text
        Paint certificateTextPaint = new Paint();
        certificateTextPaint.setColor(Color.rgb(60, 60, 60)); // Dark gray
        certificateTextPaint.setTextSize(40);
        certificateTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        certificateTextPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Certificate of Completion", PAGE_WIDTH / 2, 180, certificateTextPaint);
        
        // Draw decorative line
        Paint linePaint = new Paint();
        linePaint.setColor(Color.rgb(209, 31, 93)); // Pink-red color
        linePaint.setStrokeWidth(3);
        canvas.drawLine(PAGE_WIDTH / 2 - 120, 200, PAGE_WIDTH / 2 + 120, 200, linePaint);
        
        // Draw certificate text
        Paint textPaint = new Paint();
        textPaint.setColor(Color.rgb(60, 60, 60)); // Dark gray
        textPaint.setTextSize(24);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        textPaint.setTextAlign(Paint.Align.CENTER);
        
        canvas.drawText("This is to certify that", PAGE_WIDTH / 2, 260, textPaint);
        
        // Draw user name
        Paint namePaint = new Paint();
        namePaint.setColor(Color.rgb(50, 50, 50)); // Almost black
        namePaint.setTextSize(44);
        namePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        namePaint.setTextAlign(Paint.Align.CENTER);
        
        canvas.drawText(user.getFullName(), PAGE_WIDTH / 2, 320, namePaint);
        
        // Draw completion text
        canvas.drawText("has successfully completed the course", PAGE_WIDTH / 2, 380, textPaint);
        
        // Draw course name
        Paint courseNamePaint = new Paint();
        courseNamePaint.setColor(Color.rgb(209, 31, 93)); // Pink-red color
        courseNamePaint.setTextSize(36);
        courseNamePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        courseNamePaint.setTextAlign(Paint.Align.CENTER);
        
        canvas.drawText(course.getTitle(), PAGE_WIDTH / 2, 440, courseNamePaint);
        
        // Draw date
        String dateStr = new SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(certificate.getIssuedAt());
        canvas.drawText("Date: " + dateStr, PAGE_WIDTH / 2, 500, textPaint);
        
        // Draw certificate ID
        Paint idPaint = new Paint();
        idPaint.setColor(Color.rgb(120, 120, 120)); // Medium gray
        idPaint.setTextSize(14);
        idPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("Certificate ID: " + certificate.getCertificateId(), PAGE_WIDTH - 60, PAGE_HEIGHT - 60, idPaint);
    }
    
    /**
     * Save the PDF document to a file
     * 
     * @param document The PDF document to save
     * @param certificateId The unique certificate ID
     * @return The file path or null if saving failed
     */
    private String savePdfDocument(PdfDocument document, String certificateId) {
        try {
            // Create directory if it doesn't exist
            File dir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "certificates");
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.e(TAG, "Failed to create certificates directory");
                    return null;
                }
            }
            
            // Create file
            File file = new File(dir, "certificate_" + certificateId + ".pdf");
            FileOutputStream fos = new FileOutputStream(file);
            document.writeTo(fos);
            fos.close();
            
            return file.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "Error writing PDF file", e);
            return null;
        }
    }
}