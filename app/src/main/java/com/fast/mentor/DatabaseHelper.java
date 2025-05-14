package com.fast.mentor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    private static final String DB_NAME = "mentora.db";
    private static final int DB_VER = 1;
    private static DatabaseHelper instance;

    // Table names
    private static final String T_CONTENT      = "contents";
    private static final String T_PROGRESS     = "content_progress";
    private static final String T_METRICS      = "user_metrics";
    private static final String T_USER         = "users";
    private static final String T_COURSE       = "courses";
    private static final String T_MODULE       = "modules";
    private static final String T_CERT         = "certificates";
    private static final String T_RECOMMEND    = "recommendations";
    private static final String T_ENROLL       = "enrollments";

    private DatabaseHelper(Context ctx) {
        super(ctx, DB_NAME, null, DB_VER);
    }
    public static synchronized DatabaseHelper getInstance(Context ctx) {
        if (instance == null) {
            instance = new DatabaseHelper(ctx.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Contents table
        db.execSQL("CREATE TABLE " + T_CONTENT + " (" +
                "id INTEGER PRIMARY KEY, " +
                "module_id INTEGER, " +
                "course_id TEXT, " +
                "title TEXT, " +
                "description TEXT, " +
                "content_url TEXT, " +
                "content_type TEXT, " +
                "duration INTEGER, " +
                "display_order INTEGER" +
                ")");

        // Content progress table
        db.execSQL("CREATE TABLE " + T_PROGRESS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "content_id INTEGER, " +
                "completed INTEGER, " +
                "completion_timestamp INTEGER, " +
                "last_access_timestamp INTEGER, " +
                "time_spent_seconds INTEGER, " +
                "last_position INTEGER, " +
                "content_type TEXT, " +
                "UNIQUE(user_id, content_id)" +
                ")");

        // User metrics table
        db.execSQL("CREATE TABLE " + T_METRICS + " (" +
                "user_id INTEGER PRIMARY KEY, " +
                "average_completion_rate REAL, " +
                "preferred_content_type TEXT, " +
                "learning_pace TEXT" +
                ")");

        // Users table
        db.execSQL("CREATE TABLE " + T_USER + " (" +
                "user_id INTEGER PRIMARY KEY, " +
                "full_name TEXT, " +
                "email TEXT" +
                ")");

        // Courses table
        db.execSQL("CREATE TABLE " + T_COURSE + " (" +
                "course_id TEXT PRIMARY KEY, " +
                "title TEXT, " +
                "duration INTEGER, " +
                "difficulty INTEGER, " +
                "isPopular INTEGER, " +
                "isNew INTEGER" +
                ")");

        // Modules table
        db.execSQL("CREATE TABLE " + T_MODULE + " (" +
                "module_id INTEGER PRIMARY KEY, " +
                "week_id TEXT, " +
                "course_id TEXT, " +
                "title TEXT, " +
                "description TEXT, " +
                "display_order INTEGER" +
                ")");

        // Certificates table
        db.execSQL("CREATE TABLE " + T_CERT + " (" +
                "certificate_id TEXT PRIMARY KEY, " +
                "user_id INTEGER, " +
                "course_id TEXT, " +
                "issued_at INTEGER, " +
                "certificate_url TEXT, " +
                "shared INTEGER" +
                ")");

        // Recommendations table
        db.execSQL("CREATE TABLE " + T_RECOMMEND + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "course_id TEXT, " +
                "reason TEXT, " +
                "strength INTEGER, " +
                "timestamp INTEGER, " +
                "viewed INTEGER, " +
                "clicked INTEGER" +
                ")");
        // enrollments: user_id TEXT, course_id TEXT, progress INTEGER, isCompleted INTEGER
        db.execSQL("CREATE TABLE " + T_ENROLL + "(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id TEXT," +
                "course_id TEXT," +
                "progress INTEGER," +
                "isCompleted INTEGER" +
                ")");

    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + T_RECOMMEND);
        db.execSQL("DROP TABLE IF EXISTS " + T_CERT);
        db.execSQL("DROP TABLE IF EXISTS " + T_ENROLL);
        db.execSQL("DROP TABLE IF EXISTS " + T_MODULE);
        db.execSQL("DROP TABLE IF EXISTS " + T_COURSE);
        db.execSQL("DROP TABLE IF EXISTS " + T_USER);
        db.execSQL("DROP TABLE IF EXISTS " + T_METRICS);
        db.execSQL("DROP TABLE IF EXISTS " + T_PROGRESS);
        db.execSQL("DROP TABLE IF EXISTS " + T_CONTENT);
        onCreate(db);
    }

    // --- Content CRUD ---
    public List<Content> getAllContentForModule(int moduleId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(T_CONTENT, null, "module_id=?",
                new String[]{String.valueOf(moduleId)}, null, null, "display_order");
        List<Content> list = new ArrayList<>();
        while (c.moveToNext()) {
            Content ct = new Content();
            ct.setId(c.getInt(c.getColumnIndexOrThrow("id")));
            ct.setModuleId(c.getInt(c.getColumnIndexOrThrow("module_id")));
            ct.setCourseId(c.getInt(c.getColumnIndexOrThrow("course_id")));
            ct.setTitle(c.getString(c.getColumnIndexOrThrow("title")));
            ct.setDescription(c.getString(c.getColumnIndexOrThrow("description")));
            ct.setContentUrl(c.getString(c.getColumnIndexOrThrow("content_url")));
            ct.setContentType(c.getString(c.getColumnIndexOrThrow("content_type")));
            ct.setDuration(c.getInt(c.getColumnIndexOrThrow("duration")));
            // if your Content model has setOrderIndex(int), uncomment:
            // ct.setOrderIndex(c.getInt(c.getColumnIndexOrThrow("display_order")));
            list.add(ct);
        }
        c.close();
        return list;
    }

    public void saveContent(Content ct) {
        ContentValues cv = new ContentValues();
        cv.put("id", ct.getId());
        cv.put("module_id", ct.getModuleId());
        cv.put("course_id", ct.getCourseId());
        cv.put("title", ct.getTitle());
        cv.put("description", ct.getDescription());
        cv.put("content_url", ct.getContentUrl());
        cv.put("content_type", ct.getContentType());
        cv.put("duration", ct.getDuration());
        // cv.put("display_order", ct.getOrderIndex()); // remove if no setter
        getWritableDatabase().insertWithOnConflict(
                T_CONTENT, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public boolean updateContent(Content ct) {
        ContentValues cv = new ContentValues();
        cv.put("title", ct.getTitle());
        cv.put("description", ct.getDescription());
        cv.put("content_url", ct.getContentUrl());
        cv.put("content_type", ct.getContentType());
        cv.put("duration", ct.getDuration());
        // cv.put("display_order", ct.getOrderIndex());
        int rows = getWritableDatabase().update(
                T_CONTENT, cv, "id=?", new String[]{String.valueOf(ct.getId())});
        return rows > 0;
    }

    // --- ContentProgress CRUD ---
    public ContentProgress getContentProgress(int userId, int contentId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(T_PROGRESS, null,
                "user_id=? AND content_id=?", new String[]{
                        String.valueOf(userId), String.valueOf(contentId)}, null, null, null);
        if (!c.moveToFirst()) { c.close(); return null; }
        ContentProgress p = new ContentProgress();
        p.setId(c.getInt(c.getColumnIndexOrThrow("id")));
        p.setUserId(userId);
        p.setContentId(contentId);
        p.setCompleted(c.getInt(c.getColumnIndexOrThrow("completed"))==1);
        // convert long→Date
        p.setCompletionTimestamp(new Date(c.getLong(c.getColumnIndexOrThrow("completion_timestamp"))));
        p.setLastAccessTimestamp(new Date(c.getLong(c.getColumnIndexOrThrow("last_access_timestamp"))));
        p.setTimeSpentSeconds(c.getInt(c.getColumnIndexOrThrow("time_spent_seconds")));
        p.setLastPosition(c.getInt(c.getColumnIndexOrThrow("last_position")));
        p.setContentType(c.getString(c.getColumnIndexOrThrow("content_type")));
        c.close();
        return p;
    }

    public long saveContentProgress(ContentProgress prog) {
        ContentValues cv = new ContentValues();
        cv.put("user_id", prog.getUserId());
        cv.put("content_id", prog.getContentId());
        cv.put("completed", prog.isCompleted() ? 1 : 0);
        cv.put("completion_timestamp", prog.getCompletionTimestamp().getTime());
        cv.put("last_access_timestamp", prog.getLastAccessTimestamp().getTime());
        cv.put("time_spent_seconds", prog.getTimeSpentSeconds());
        cv.put("last_position", prog.getLastPosition());
        cv.put("content_type", prog.getContentType());
        return getWritableDatabase().insertWithOnConflict(
                T_PROGRESS, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public boolean updateContentProgress(ContentProgress prog) {
        ContentValues cv = new ContentValues();
        cv.put("completed", prog.isCompleted() ? 1 : 0);
        cv.put("completion_timestamp", prog.getCompletionTimestamp().getTime());
        cv.put("last_access_timestamp", prog.getLastAccessTimestamp().getTime());
        cv.put("time_spent_seconds", prog.getTimeSpentSeconds());
        cv.put("last_position", prog.getLastPosition());
        int rows = getWritableDatabase().update(
                T_PROGRESS, cv,
                "user_id=? AND content_id=?",
                new String[]{String.valueOf(prog.getUserId()), String.valueOf(prog.getContentId())});
        return rows > 0;
    }

    // --- UserMetrics CRUD ---
    public UserMetrics getUserMetrics(int userId) {
        Cursor c = getReadableDatabase().query(
                T_METRICS, null, "user_id=?",
                new String[]{String.valueOf(userId)}, null, null, null);
        if (!c.moveToFirst()) { c.close(); return null; }
        UserMetrics m = new UserMetrics();
        m.setUserId(userId);
        // cast double→int if needed
        m.setAverageCompletionRate((int)c.getDouble(c.getColumnIndexOrThrow("average_completion_rate")));
        m.setPreferredContentType(c.getString(c.getColumnIndexOrThrow("preferred_content_type")));
        m.setLearningPace(c.getString(c.getColumnIndexOrThrow("learning_pace")));
        c.close();
        return m;
    }

    public long saveUserMetrics(UserMetrics m) {
        ContentValues cv = new ContentValues();
        cv.put("user_id", m.getUserId());
        cv.put("average_completion_rate", (double)m.getAverageCompletionRate());
        cv.put("preferred_content_type", m.getPreferredContentType());
        cv.put("learning_pace", m.getLearningPace());
        return getWritableDatabase().insertWithOnConflict(
                T_METRICS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public boolean updateUserMetrics(UserMetrics m) {
        ContentValues cv = new ContentValues();
        cv.put("average_completion_rate", (double)m.getAverageCompletionRate());
        cv.put("preferred_content_type", m.getPreferredContentType());
        cv.put("learning_pace", m.getLearningPace());
        int rows = getWritableDatabase().update(
                T_METRICS, cv, "user_id=?", new String[]{String.valueOf(m.getUserId())});
        return rows > 0;
    }

    // --- User CRUD ---
    public User getUser(int userId) {
        Cursor c = getReadableDatabase().query(
                T_USER, null, "user_id=?",
                new String[]{String.valueOf(userId)}, null, null, null);
        if (!c.moveToFirst()) { c.close(); return null; }
        User u = new User();
        // model wants String ID
        u.setUserId(String.valueOf(userId));
        u.setFullName(c.getString(c.getColumnIndexOrThrow("full_name")));
        u.setEmail(c.getString(c.getColumnIndexOrThrow("email")));
        c.close();
        return u;
    }

    public void saveUser(User u) {
        ContentValues cv = new ContentValues();
        cv.put("user_id", Integer.parseInt(u.getUserId()));
        cv.put("full_name", u.getFullName());
        cv.put("email", u.getEmail());
        getWritableDatabase().insertWithOnConflict(
                T_USER, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public boolean updateUser(User u) {
        ContentValues cv = new ContentValues();
        cv.put("full_name", u.getFullName());
        cv.put("email", u.getEmail());
        int rows = getWritableDatabase().update(
                T_USER, cv, "user_id=?", new String[]{u.getUserId()});
        return rows > 0;
    }

    // --- Course CRUD ---
    public Course getCourse(int courseId) {
        Cursor c = getReadableDatabase().query(
                T_COURSE, null,"course_id=?",
                new String[]{String.valueOf(courseId)},null,null,null);
        if (!c.moveToFirst()) { c.close(); return null; }
        Course co = new Course();
        co.setCourseId(String.valueOf(courseId));
        co.setTitle(c.getString(c.getColumnIndexOrThrow("title")));
        co.setDurationInHours(c.getInt(c.getColumnIndexOrThrow("duration")));
        c.close();
        return co;
    }

    public void saveCourse(Course co) {
        ContentValues cv = new ContentValues();
        cv.put("course_id", co.getCourseId());
        cv.put("title", co.getTitle());
        cv.put("duration", co.getDurationInHours());
        getWritableDatabase().insertWithOnConflict(
                T_COURSE,null,cv,SQLiteDatabase.CONFLICT_REPLACE);
    }

    public boolean updateCourse(Course co) {
        ContentValues cv = new ContentValues();
        cv.put("title", co.getTitle());
        cv.put("duration", co.getDurationInHours());
        int rows = getWritableDatabase().update(
                T_COURSE,cv,"course_id=?",new String[]{String.valueOf(co.getCourseId())});
        return rows>0;
    }

    // --- Certificate CRUD ---
    public void saveCertificate(Certificate cert) {
        ContentValues cv = new ContentValues();
        cv.put("certificate_id", cert.getCertificateId());
        cv.put("user_id", Integer.parseInt(cert.getUserId()));
        cv.put("course_id", Integer.parseInt(cert.getCourseId()));
        cv.put("issued_at", Long.parseLong(cert.getIssuedAt()));
        cv.put("certificate_url", cert.getCertificateUrl());
        cv.put("shared", cert.isShared()?1:0);
        getWritableDatabase().insertWithOnConflict(
                T_CERT,null,cv,SQLiteDatabase.CONFLICT_REPLACE);
    }

    public Certificate getCertificate(String userId, String courseId) {
        Cursor c = getReadableDatabase().query(
                T_CERT,null,"user_id=? AND course_id=?",
                new String[]{userId,courseId},null,null,null);
        if (!c.moveToFirst()) {c.close();return null;}
        Certificate cert = new Certificate();
        cert.setCertificateId(c.getString(c.getColumnIndexOrThrow("certificate_id")));
        cert.setUserId(c.getString(c.getColumnIndexOrThrow("user_id")));
        cert.setCourseId(c.getString(c.getColumnIndexOrThrow("course_id")));
        cert.setIssuedAt(c.getString(c.getColumnIndexOrThrow("issued_at")));
        cert.setCertificateUrl(c.getString(c.getColumnIndexOrThrow("certificate_url")));
        cert.setShared(c.getInt(c.getColumnIndexOrThrow("shared"))==1);
        c.close();return cert;
    }

    public Certificate getCertificateById(String certificateId) {
        Cursor c = getReadableDatabase().query(
                T_CERT,null,"certificate_id=?",
                new String[]{certificateId},null,null,null);
        if (!c.moveToFirst()) {c.close();return null;}
        Certificate cert = new Certificate();
        cert.setCertificateId(c.getString(c.getColumnIndexOrThrow("certificate_id")));
        cert.setUserId(c.getString(c.getColumnIndexOrThrow("user_id")));
        cert.setCourseId(c.getString(c.getColumnIndexOrThrow("course_id")));
        cert.setIssuedAt(c.getString(c.getColumnIndexOrThrow("issued_at")));
        cert.setCertificateUrl(c.getString(c.getColumnIndexOrThrow("certificate_url")));
        cert.setShared(c.getInt(c.getColumnIndexOrThrow("shared"))==1);
        c.close();return cert;
    }

    public List<Certificate> getUserCertificates(String userId) {
        List<Certificate> out = new ArrayList<>();
        Cursor c = getReadableDatabase().query(
                T_CERT,null,"user_id=?",
                new String[]{userId},null,null,"issued_at DESC");
        while(c.moveToNext()){
            out.add(getCertificateById(
                    c.getString(c.getColumnIndexOrThrow("certificate_id"))));
        }
        c.close();return out;
    }

    public boolean updateCertificate(Certificate cert) {
        ContentValues cv = new ContentValues();
        cv.put("shared", cert.isShared()?1:0);
        int rows = getWritableDatabase().update(
                T_CERT,cv,"certificate_id=?",
                new String[]{cert.getCertificateId()});
        return rows>0;
    }

    // --- Module & Course Progress hooks ---
    public boolean isModuleCompleted(int userId, int moduleId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor total = db.rawQuery(
                "SELECT COUNT(*) FROM " + T_CONTENT + " WHERE module_id=?",
                new String[]{String.valueOf(moduleId)});
        total.moveToFirst();
        int totalCount = total.getInt(0);
        total.close();
        if (totalCount == 0) return false;

        Cursor completed = db.rawQuery(
                "SELECT COUNT(*) FROM " + T_PROGRESS + " p " +
                        "JOIN " + T_CONTENT + " c ON p.content_id = c.id " +
                        "WHERE p.user_id = ? AND c.module_id = ? AND p.completed = 1",
                new String[]{String.valueOf(userId), String.valueOf(moduleId)});
        completed.moveToFirst();
        int doneCount = completed.getInt(0);
        completed.close();
        return doneCount == totalCount;
    }

    public void updateCourseProgress(int userId, int courseId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor total = db.rawQuery(
                "SELECT COUNT(*) FROM " + T_CONTENT + " WHERE course_id=?",
                new String[]{String.valueOf(courseId)});
        total.moveToFirst();
        int totalCount = total.getInt(0);
        total.close();
        if (totalCount == 0) return;

        Cursor completed = db.rawQuery(
                "SELECT COUNT(*) FROM " + T_PROGRESS + " p " +
                        "JOIN " + T_CONTENT + " c ON p.content_id = c.id " +
                        "WHERE p.user_id = ? AND c.course_id = ? AND p.completed = 1",
                new String[]{String.valueOf(userId), String.valueOf(courseId)});
        completed.moveToFirst();
        int doneCount = completed.getInt(0);
        completed.close();

        if (doneCount == totalCount) {
            CertificateManager.getInstance(null)
                    .createCertificate(String.valueOf(userId), String.valueOf(courseId));
        }
    }
    /**
     * Fetch a single Content by its ID.
     */
    public Content getContent(int contentId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(
                T_CONTENT,
                null,
                "id = ?",
                new String[]{ String.valueOf(contentId) },
                null, null, null
        );
        if (!c.moveToFirst()) {
            c.close();
            return null;
        }
        Content ct = new Content();
        ct.setId( c.getInt(c.getColumnIndexOrThrow("id")) );
        ct.setModuleId( c.getInt(c.getColumnIndexOrThrow("module_id")) );
        ct.setCourseId( c.getInt(c.getColumnIndexOrThrow("course_id")) );
        ct.setTitle( c.getString(c.getColumnIndexOrThrow("title")) );
        ct.setDescription( c.getString(c.getColumnIndexOrThrow("description")) );
        ct.setContentUrl( c.getString(c.getColumnIndexOrThrow("content_url")) );
        ct.setContentType( c.getString(c.getColumnIndexOrThrow("content_type")) );
        ct.setDuration( c.getInt(c.getColumnIndexOrThrow("duration")) );
        // if you have orderIndex in your model:
        // ct.setOrderIndex(c.getInt(c.getColumnIndexOrThrow("display_order")));
        c.close();
        return ct;
    }

    /**
     * Called whenever a piece of content is completed.
     * If the entire module is now complete, advance the course‐level progress.
     */
    public void updateModuleProgress(int userId, int moduleId) {
        // 1) Only proceed if every content in this module is marked completed
        if (!isModuleCompleted(userId, moduleId)) {
            return;
        }

        // 2) Lookup which course this module belongs to
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(
                T_MODULE,
                new String[]{ "course_id" },
                "module_id = ?",
                new String[]{ String.valueOf(moduleId) },
                null, null, null
        );

        if (!c.moveToFirst()) {
            c.close();
            return;  // module not found
        }
        int courseId = c.getInt(c.getColumnIndexOrThrow("course_id"));
        c.close();

        // 3) Advance course‐level progress
        updateCourseProgress(userId, courseId);
    }

    // --- Courses ---
    public List<Course> getAllCourses() {
        List<Course> out = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(T_COURSE, null, null, null, null, null, null);
        while(c.moveToNext()) {
            Course co = new Course();
            co.setCourseId(c.getString(c.getColumnIndexOrThrow("course_id")));
            co.setTitle(c.getString(c.getColumnIndexOrThrow("title")));
            co.setDurationInHours(c.getInt(c.getColumnIndexOrThrow("duration")));
            // assume difficulty column exists:
            co.setDifficulty(c.getInt(c.getColumnIndexOrThrow("difficulty")));
            co.setPopular(c.getInt(c.getColumnIndexOrThrow("isPopular"))==1);
            out.add(co);
        }
        c.close();
        return out;
    }

    // --- Enrollments ---
    public List<String> getEnrolledCourseIds(String userId) {
        List<String> out = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(T_ENROLL,
                new String[]{"course_id"},
                "user_id=?",
                new String[]{String.valueOf(userId)},
                null, null, null);
        while(c.moveToNext()) {
            out.add(c.getString(c.getColumnIndexOrThrow("course_id")));
        }
        c.close();
        return out;
    }

    // --- Recommendations CRUD ---
    public void saveRecommendation(Recommendation rec) {
        ContentValues cv = new ContentValues();
        cv.put("user_id", rec.getUserId());
        cv.put("course_id", rec.getCourseId());
        cv.put("reason", rec.getReason());
        cv.put("strength", rec.getStrength());
        cv.put("timestamp", rec.getTimestamp());
        cv.put("viewed", rec.isViewed() ? 1 : 0);
        cv.put("clicked", rec.isClicked() ? 1 : 0);
        getWritableDatabase().insertWithOnConflict(
                T_RECOMMEND, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public List<Recommendation> getUserRecommendations(String userId) {
        List<Recommendation> out = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(T_RECOMMEND, null,
                "user_id=?", new String[]{String.valueOf(userId)},
                null, null, "strength DESC");
        while(c.moveToNext()) {
            Recommendation r = new Recommendation();
            r.setId(c.getInt(c.getColumnIndexOrThrow("id")));
            r.setUserId(c.getColumnIndexOrThrow("user_id"));
            r.setCourseId(Integer.parseInt(c.getString(c.getColumnIndexOrThrow("course_id"))));
            r.setReason(c.getString(c.getColumnIndexOrThrow("reason")));
            r.setStrength(c.getInt(c.getColumnIndexOrThrow("strength")));
            r.setTimestamp(c.getLong(c.getColumnIndexOrThrow("timestamp")));
            r.setViewed(c.getInt(c.getColumnIndexOrThrow("viewed"))==1);
            r.setClicked(c.getInt(c.getColumnIndexOrThrow("clicked"))==1);
            out.add(r);
        }
        c.close();
        return out;
    }

    public Recommendation getRecommendation(int recommendationId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(T_RECOMMEND, null,
                "id=?", new String[]{String.valueOf(recommendationId)},
                null, null, null);
        if (!c.moveToFirst()) { c.close(); return null; }
        Recommendation r = new Recommendation();
        r.setId(c.getInt(c.getColumnIndexOrThrow("id")));
        r.setUserId(c.getInt(c.getColumnIndexOrThrow("user_id")));
        r.setCourseId(Integer.parseInt(c.getString(c.getColumnIndexOrThrow("course_id"))));
        r.setReason(c.getString(c.getColumnIndexOrThrow("reason")));
        r.setStrength(c.getInt(c.getColumnIndexOrThrow("strength")));
        r.setTimestamp(c.getLong(c.getColumnIndexOrThrow("timestamp")));
        r.setViewed(c.getInt(c.getColumnIndexOrThrow("viewed"))==1);
        r.setClicked(c.getInt(c.getColumnIndexOrThrow("clicked"))==1);
        c.close();
        return r;
    }

    public boolean updateRecommendation(Recommendation rec) {
        ContentValues cv = new ContentValues();
        cv.put("viewed", rec.isViewed() ? 1 : 0);
        cv.put("clicked", rec.isClicked() ? 1 : 0);
        int rows = getWritableDatabase().update(
                T_RECOMMEND, cv, "id=?", new String[]{String.valueOf(rec.getId())});
        return rows>0;
    }
    public boolean isCourseCompleted(String userId, String courseId) {
        SQLiteDatabase db = this.getReadableDatabase();

        // 1. Count total modules in the course
        String totalModulesQuery = "SELECT COUNT(*) FROM Modules WHERE courseId = ?";
        Cursor totalCursor = db.rawQuery(totalModulesQuery, new String[]{courseId});

        int totalModules = 0;
        if (totalCursor.moveToFirst()) {
            totalModules = totalCursor.getInt(0);
        }
        totalCursor.close();

        if (totalModules == 0) {
            return false; // No modules in course → can't be considered complete
        }

        // 2. Count completed modules for the user in that course
        String completedQuery = "SELECT COUNT(*) FROM Progress " +
                "WHERE userId = ? AND courseId = ? AND isCompleted = 1";
        Cursor completedCursor = db.rawQuery(completedQuery, new String[]{userId, courseId});

        int completedModules = 0;
        if (completedCursor.moveToFirst()) {
            completedModules = completedCursor.getInt(0);
        }
        completedCursor.close();

        return completedModules == totalModules;
    }

}
