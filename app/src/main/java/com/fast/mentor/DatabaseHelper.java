package com.fast.mentor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "mentor.db";
    private static final int DATABASE_VERSION = 1;

    // Courses Table
    private static final TABLE_COURSES = "courses";
    private static final String COLUMN_COURSE_ID = "id";
    private static final String COLUMN_COURSE_IMAGE = "image";
    private static final String COLUMN_COURSE_TITLE = "title";
    private static final String COLUMN_COURSE_PROVIDER = "provider";

    // Projects Table
    private static final String TABLE_PROJECTS = "projects";
    private static final String COLUMN_PROJECT_ID = "id";
    private static final String COLUMN_PROJECT_IMAGE = "image";
    private static final String COLUMN_PROJECT_TITLE = "title";
    private static final String COLUMN_PROJECT_ORGANIZATION = "organization";
    private static final String COLUMN_PROJECT_TYPE = "type";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createCoursesTable(db);
        createProjectsTable(db);
        insertSampleData(db);
    }

    private void createCoursesTable(SQLiteDatabase db) {
        String CREATE_COURSES_TABLE = "CREATE TABLE " + TABLE_COURSES + "("
                + COLUMN_COURSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_COURSE_IMAGE + " INTEGER,"
                + COLUMN_COURSE_TITLE + " TEXT,"
                + COLUMN_COURSE_PROVIDER + " TEXT)";
        db.execSQL(CREATE_COURSES_TABLE);
    }

    private void createProjectsTable(SQLiteDatabase db) {
        String CREATE_PROJECTS_TABLE = "CREATE TABLE " + TABLE_PROJECTS + "("
                + COLUMN_PROJECT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_PROJECT_IMAGE + " INTEGER,"
                + COLUMN_PROJECT_TITLE + " TEXT,"
                + COLUMN_PROJECT_ORGANIZATION + " TEXT,"
                + COLUMN_PROJECT_TYPE + " TEXT)";
        db.execSQL(CREATE_PROJECTS_TABLE);
    }

    private void insertSampleData(SQLiteDatabase db) {
        // Insert sample courses
        insertCourse(db, new Course(R.drawable.course1, "Intro to AI", "Coursera"));
        insertCourse(db, new Course(R.drawable.course1, "Data Science Basics", "edX"));
        insertCourse(db, new Course(R.drawable.course1, "Machine Learning", "Udacity"));

        // Insert sample projects
        insertProject(db, new Project(R.drawable.project1, "Stock Predictor", "FAST-NUCES", "Guided Project"));
        insertProject(db, new Project(R.drawable.project1, "AI Chatbot", "GLI", "Guided Project"));
        insertProject(db, new Project(R.drawable.project1, "Resume Parser", "Mentora", "Guided Project"));
    }

    public List<Course> getAllCourses() {
        List<Course> courses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_COURSES, null);

        if (cursor.moveToFirst()) {
            do {
                Course course = new Course(
                        cursor.getInt(1),
                        cursor.getString(2),
                        cursor.getString(3)
                );
                courses.add(course);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return courses;
    }

    public List<Project> getAllProjects() {
        List<Project> projects = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PROJECTS, null);

        if (cursor.moveToFirst()) {
            do {
                Project project = new Project(
                        cursor.getInt(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4)
                );
                projects.add(project);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return projects;
    }

    private void insertCourse(SQLiteDatabase db, Course course) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_COURSE_IMAGE, course.getImageResource());
        values.put(COLUMN_COURSE_TITLE, course.getTitle());
        values.put(COLUMN_COURSE_PROVIDER, course.getProvider());
        db.insert(TABLE_COURSES, null, values);
    }

    private void insertProject(SQLiteDatabase db, Project project) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_PROJECT_IMAGE, project.getImageResource());
        values.put(COLUMN_PROJECT_TITLE, project.getTitle());
        values.put(COLUMN_PROJECT_ORGANIZATION, project.getOrganization());
        values.put(COLUMN_PROJECT_TYPE, project.getType());
        db.insert(TABLE_PROJECTS, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COURSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROJECTS);
        onCreate(db);
    }
}
