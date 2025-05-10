package com.fast.mentor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.fast.mentor.R;
import com.fast.mentor.CourseService;
import com.fast.mentor.Course;
import com.fast.mentor.Enrollment;
import com.fast.mentor.Module;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CourseDetailActivity extends AppCompatActivity {

    public static final String EXTRA_COURSE_ID = "course_id";

    private CourseService courseService;
    private FirebaseAuth firebaseAuth;
    private String courseId;
    private Course course;
    private Enrollment enrollment;
    private boolean isEnrolled = false;

    // UI components
    private ImageView courseImageView;
    private TextView courseTitleTextView;
    private TextView authorTextView;
    private TextView ratingTextView;
    private TextView durationTextView;
    private TextView studentsTextView;
    private ProgressBar progressBar;
    private TextView progressTextView;
    private TextView descriptionTextView;
    private TextView difficultyTextView;
    private TextView languageTextView;
    private TextView priceTextView;
    private TextView requirementsTextView;
    private RecyclerView modulesRecyclerView;
    private Button enrollButton;
    private ProgressBar loadingProgressBar;
    private LinearLayout errorView;
    private TextView errorTextView;
    private Button retryButton;

    private ModuleAdapter moduleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail);

        courseService = new CourseService();
        firebaseAuth = FirebaseAuth.getInstance();

        // Initialize UI components
        initViews();
        setupToolbar();

        // Get course ID from intent
        courseId = getIntent().getStringExtra(EXTRA_COURSE_ID);
        if (courseId == null) {
            showError(getString(R.string.error_course_not_found));
            return;
        }

        // Setup RecyclerView for modules
        setupModulesRecyclerView();

        // Load course data
        loadCourseData();
    }

    private void initViews() {
        courseImageView = findViewById(R.id.courseImageView);
        courseTitleTextView = findViewById(R.id.courseTitleTextView);
        authorTextView = findViewById(R.id.authorTextView);
        ratingTextView = findViewById(R.id.ratingTextView);
        durationTextView = findViewById(R.id.durationTextView);
        studentsTextView = findViewById(R.id.studentsTextView);
        progressBar = findViewById(R.id.progressBar);
        progressTextView = findViewById(R.id.progressTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        difficultyTextView = findViewById(R.id.difficultyTextView);
        languageTextView = findViewById(R.id.languageTextView);
        priceTextView = findViewById(R.id.priceTextView);
        requirementsTextView = findViewById(R.id.requirementsTextView);
        modulesRecyclerView = findViewById(R.id.modulesRecyclerView);
        enrollButton = findViewById(R.id.enrollButton);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        errorView = findViewById(R.id.errorView);
        errorTextView = findViewById(R.id.errorTextView);
        retryButton = findViewById(R.id.retryButton);

        // Set retry button click listener
        retryButton.setOnClickListener(v -> loadCourseData());

        // Set enroll button click listener
        enrollButton.setOnClickListener(v -> enrollInCourse());
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsingToolbar);
        collapsingToolbar.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));
        
        // Set navigation click listener to finish activity
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupModulesRecyclerView() {
        moduleAdapter = new ModuleAdapter(this, new ArrayList<>(), isEnrolled);
        modulesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        modulesRecyclerView.setHasFixedSize(true);
        modulesRecyclerView.setAdapter(moduleAdapter);
    }

    private void loadCourseData() {
        showLoading();

        // Check if user is logged in
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            isEnrolled = false;
            loadCourseDetails();
        } else {
            // First check if user is enrolled in this course
            courseService.getUserEnrollment(currentUser.getUid(), courseId, 
                    enrollment -> {
                        this.enrollment = enrollment;
                        isEnrolled = (enrollment != null);
                        loadCourseDetails();
                    }, 
                    e -> {
                        // Continue loading course details even if enrollment check fails
                        isEnrolled = false;
                        loadCourseDetails();
                    });
        }
    }

    private void loadCourseDetails() {
        courseService.getCourse(courseId, 
                course -> {
                    this.course = course;
                    displayCourseDetails();
                    loadModules();
                }, 
                e -> {
                    hideLoading();
                    showError(getString(R.string.error_loading_course, e.getMessage()));
                });
    }

    private void loadModules() {
        courseService.getCourseModules(courseId, 
                modules -> {
                    hideLoading();
                    updateModules(modules);
                }, 
                e -> {
                    hideLoading();
                    showError(getString(R.string.error_loading_course_content));
                });
    }

    private void displayCourseDetails() {
        // Set course title and author
        courseTitleTextView.setText(course.getTitle());
        authorTextView.setText(getString(R.string.course_author_format, course.getAuthor()));
        
        // Set course image
        if (course.getImageUrl() != null && !course.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(course.getImageUrl())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .error(R.drawable.placeholder_course)
                    .into(courseImageView);
        } else {
            courseImageView.setImageResource(R.drawable.placeholder_course);
        }
        
        // Set course stats
        ratingTextView.setText(String.format(Locale.getDefault(), "%.1f", course.getRating()));
        durationTextView.setText(course.getDuration());
        
        // Format students count using plural string resource
        int enrolledCount = course.getEnrolledCount();
        studentsTextView.setText(getResources().getQuantityString(
                R.plurals.course_enrolled_count, enrolledCount, enrolledCount));
        
        // Set course description
        descriptionTextView.setText(course.getDescription());
        
        // Set course details
        difficultyTextView.setText(course.getDifficulty());
        languageTextView.setText(course.getLanguage());
        
        // Format price (free or paid)
        if (course.isFree()) {
            priceTextView.setText(getString(R.string.enroll_free));
            enrollButton.setText(R.string.enroll_free);
        } else {
            String formattedPrice = NumberFormat.getCurrencyInstance().format(course.getPrice());
            priceTextView.setText(formattedPrice);
            enrollButton.setText(getString(R.string.enroll_paid, formattedPrice));
        }
        
        // Set requirements
        requirementsTextView.setText(course.getRequirements());
        
        // Handle UI for enrolled status
        updateEnrollmentUI();
    }
    
    private void updateEnrollmentUI() {
        if (isEnrolled) {
            // Show progress
            progressBar.setVisibility(View.VISIBLE);
            progressTextView.setVisibility(View.VISIBLE);
            
            // Set progress
            int progress = enrollment.getProgress();
            progressBar.setProgress(progress);
            progressTextView.setText(getString(R.string.course_progress_format, progress));
            
            // Update enroll button to continue learning or view certificate
            if (enrollment.isCompleted()) {
                enrollButton.setText(R.string.certificate_download);
                enrollButton.setOnClickListener(v -> viewCertificate());
            } else {
                enrollButton.setText(R.string.continue_lesson);
                enrollButton.setOnClickListener(v -> continueLearning());
            }
        } else {
            // Hide progress for non-enrolled courses
            progressBar.setVisibility(View.GONE);
            progressTextView.setVisibility(View.GONE);
            
            // Set enroll button
            if (course.isFree()) {
                enrollButton.setText(R.string.enroll_free);
            } else {
                String formattedPrice = NumberFormat.getCurrencyInstance().format(course.getPrice());
                enrollButton.setText(getString(R.string.enroll_paid, formattedPrice));
            }
            enrollButton.setOnClickListener(v -> enrollInCourse());
        }
        
        // Update module adapter with enrollment status
        if (moduleAdapter != null) {
            moduleAdapter.setEnrolled(isEnrolled);
        }
    }
    
    private void updateModules(List<Module> modules) {
        moduleAdapter.updateModules(modules);
    }
    
    private void enrollInCourse() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            // User needs to log in first
            Toast.makeText(this, "Please log in to enroll in courses", Toast.LENGTH_SHORT).show();
            // Start login activity
            // TODO: Navigate to login screen
            return;
        }
        
        // Show enrolling progress
        enrollButton.setEnabled(false);
        enrollButton.setText(R.string.enrolling);
        
        courseService.enrollInCourse(currentUser.getUid(), courseId, 
                enrollment -> {
                    this.enrollment = enrollment;
                    isEnrolled = true;
                    updateEnrollmentUI();
                    Toast.makeText(this, R.string.enrollment_success, Toast.LENGTH_SHORT).show();
                    enrollButton.setEnabled(true);
                }, 
                e -> {
                    Toast.makeText(this, getString(R.string.enrollment_error, e.getMessage()), 
                            Toast.LENGTH_LONG).show();
                    enrollButton.setEnabled(true);
                    if (course.isFree()) {
                        enrollButton.setText(R.string.enroll_free);
                    } else {
                        String formattedPrice = NumberFormat.getCurrencyInstance().format(course.getPrice());
                        enrollButton.setText(getString(R.string.enroll_paid, formattedPrice));
                    }
                });
    }
    
    private void continueLearning() {
        // TODO: Navigate to the next incomplete lesson
    }
    
    private void viewCertificate() {
        // TODO: Navigate to certificate viewer
    }
    
    private void showLoading() {
        loadingProgressBar.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
    }
    
    private void hideLoading() {
        loadingProgressBar.setVisibility(View.GONE);
    }
    
    private void showError(String errorMessage) {
        errorView.setVisibility(View.VISIBLE);
        errorTextView.setText(errorMessage);
    }
}