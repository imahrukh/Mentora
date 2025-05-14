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
    private final CertificateGenerator certGen;

    private CertificateManager(Context context) {
        this.context = context.getApplicationContext();
        this.dbHelper = DatabaseHelper.getInstance(context);
        this.certGen = new CertificateGenerator(context);
    }

    public static synchronized CertificateManager getInstance(Context ctx) {
        if (instance == null) {
            instance = new CertificateManager(ctx);
        }
        return instance;
    }

    public Certificate createCertificate(String userId, String courseId) {
        Log.d(TAG, "createCertificate for user " + userId + ", course " + courseId);
        Certificate existing = dbHelper.getCertificate(userId, courseId);
        if (existing != null) return existing;
        User user = dbHelper.getUser(Integer.parseInt(userId));
        Course course = dbHelper.getCourse(Integer.parseInt(courseId));
        if (user == null || course == null) return null;
        if (!dbHelper.isCourseCompleted(userId, courseId)) return null;
        Certificate cert = certGen.generateCertificate(user, course, new Date());
        dbHelper.saveCertificate(cert);
        return cert;
    }

    public List<Certificate> getUserCertificates(String userId) {
        return dbHelper.getUserCertificates(userId);
    }

    public Certificate getCertificate(String userId, String courseId) {
        return dbHelper.getCertificate(userId, courseId);
    }

    public Certificate shareCertificate(String certificateId) {
        Certificate cert = dbHelper.getCertificateById(certificateId);
        if (cert == null) return null;
        cert.setShared(true);
        dbHelper.updateCertificate(cert);
        return cert;
    }

    public boolean canCreateCertificate(String userId, String courseId) {
        return dbHelper.isCourseCompleted(userId, courseId)
                && dbHelper.getCertificate(userId, courseId) == null;
    }
}
