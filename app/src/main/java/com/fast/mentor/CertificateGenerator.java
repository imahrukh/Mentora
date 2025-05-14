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
        Log.d(TAG, "Generating certificate for user " + user.getUserId() + ", course " + course.getCourseId());

        // Create new certificate object
        String certId = user.getUserId() + "_" + course.getCourseId() + "_" + completionDate.getTime();
        Certificate certificate = new Certificate(certId, user.getUserId(), course.getCourseId());
        certificate.setCourse(course);

        // Set issuedAt as timestamp string
        certificate.setIssuedAt(String.valueOf(completionDate.getTime()));

        try {
            PdfDocument document = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            Canvas canvas = page.getCanvas();

            drawCertificate(canvas, user, course, certificate);
            document.finishPage(page);

            String filePath = savePdfDocument(document, certificate.getCertificateId());
            document.close();

            if (filePath != null) {
                certificate.setCertificateUrl(filePath);
                Log.d(TAG, "Certificate generated successfully: " + filePath);
            } else {
                Log.e(TAG, "Failed to save certificate PDF");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error generating certificate", e);
        }

        return certificate;
    }

    private void drawCertificate(Canvas canvas, User user, Course course, Certificate certificate) {
        Paint paint = new Paint();
        // Background
        paint.setColor(Color.WHITE);
        canvas.drawRect(0, 0, PAGE_WIDTH, PAGE_HEIGHT, paint);

        // Border
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(8);
        paint.setColor(Color.rgb(209,31,93));
        canvas.drawRect(40,40, PAGE_WIDTH-40, PAGE_HEIGHT-40, paint);

        // Corners
        paint.setStyle(Paint.Style.FILL);
        int cs = 40;
        // TL
        canvas.drawRect(40,40,40+cs,48, paint);
        canvas.drawRect(40,40,48,40+cs, paint);
        // TR
        canvas.drawRect(PAGE_WIDTH-40-cs,40, PAGE_WIDTH-40,48, paint);
        canvas.drawRect(PAGE_WIDTH-48,40, PAGE_WIDTH-40,40+cs, paint);
        // BL
        canvas.drawRect(40, PAGE_HEIGHT-48,40+cs,PAGE_HEIGHT-40, paint);
        canvas.drawRect(40, PAGE_HEIGHT-40-cs,48, PAGE_HEIGHT-40, paint);
        // BR
        canvas.drawRect(PAGE_WIDTH-40-cs, PAGE_HEIGHT-48, PAGE_WIDTH-40, PAGE_HEIGHT-40, paint);
        canvas.drawRect(PAGE_WIDTH-48, PAGE_HEIGHT-40-cs, PAGE_WIDTH-40, PAGE_HEIGHT-40, paint);

        // Title
        paint.setColor(Color.rgb(209,31,93));
        paint.setTextSize(60);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("MENTORA", PAGE_WIDTH/2f,120, paint);

        // Subtitle
        paint.setColor(Color.DKGRAY);
        paint.setTextSize(40);
        paint.setTypeface(Typeface.DEFAULT);
        canvas.drawText("Certificate of Completion", PAGE_WIDTH/2f,180, paint);

        // Divider
        paint.setColor(Color.rgb(209,31,93));
        paint.setStrokeWidth(3);
        canvas.drawLine(PAGE_WIDTH/2f-120,200, PAGE_WIDTH/2f+120,200, paint);

        // Body text
        paint.setColor(Color.DKGRAY);
        paint.setTextSize(24);
        canvas.drawText("This is to certify that", PAGE_WIDTH/2f,260, paint);

        // Name
        paint.setColor(Color.BLACK);
        paint.setTextSize(44);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText(user.getFullName(), PAGE_WIDTH/2f,320, paint);

        // Completed
        paint.setColor(Color.DKGRAY);
        paint.setTextSize(24);
        paint.setTypeface(Typeface.DEFAULT);
        canvas.drawText("has successfully completed the course", PAGE_WIDTH/2f,380, paint);

        // Course title
        paint.setColor(Color.rgb(209,31,93));
        paint.setTextSize(36);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText(course.getTitle(), PAGE_WIDTH/2f,440, paint);

        // Date
        String dateStr = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                .format(new Date(Long.parseLong(certificate.getIssuedAt())));
        paint.setColor(Color.DKGRAY);
        paint.setTextSize(24);
        paint.setTypeface(Typeface.DEFAULT);
        canvas.drawText("Date: " + dateStr, PAGE_WIDTH/2f,500, paint);

        // ID
        paint.setColor(Color.GRAY);
        paint.setTextSize(14);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("Certificate ID: " + certificate.getCertificateId(), PAGE_WIDTH-60, PAGE_HEIGHT-60, paint);
    }

    private String savePdfDocument(PdfDocument document, String certificateId) {
        try {
            File dir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "certificates");
            if (!dir.exists() && !dir.mkdirs()) {
                Log.e(TAG, "Failed to create certificates directory");
                return null;
            }
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
