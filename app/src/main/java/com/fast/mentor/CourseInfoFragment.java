package com.fast.mentor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.fast.mentor.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Fragment for displaying course information
 */
public class CourseInfoFragment extends Fragment {

    private String courseId;
    
    private ImageView ivInstructor;
    private TextView tvInstructorName;
    private TextView tvInstructorTitle;
    private TextView tvInstructorBio;
    private TextView tvCourseDescription;
    private TextView tvSkillsLearned;
    private TextView tvPrerequisites;
    private ProgressBar progressBar;
    
    private FirebaseFirestore firestore;
    
    public CourseInfoFragment() {
        // Required empty public constructor
    }
    
    public static CourseInfoFragment newInstance(String courseId) {
        CourseInfoFragment fragment = new CourseInfoFragment();
        Bundle args = new Bundle();
        args.putString("courseId", courseId);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            courseId = getArguments().getString("courseId");
        }
        
        // Initialize FirebaseFirestore
        firestore = FirebaseFirestore.getInstance();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_course_info, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        ivInstructor = view.findViewById(R.id.ivInstructor);
        tvInstructorName = view.findViewById(R.id.tvInstructorName);
        tvInstructorTitle = view.findViewById(R.id.tvInstructorTitle);
        tvInstructorBio = view.findViewById(R.id.tvInstructorBio);
        tvCourseDescription = view.findViewById(R.id.tvCourseDescription);
        tvSkillsLearned = view.findViewById(R.id.tvSkillsLearned);
        tvPrerequisites = view.findViewById(R.id.tvPrerequisites);
        progressBar = view.findViewById(R.id.progressBar);
        
        // Load course information
        loadCourseInfo();
    }
    
    private void loadCourseInfo() {
        if (courseId == null) {
            return;
        }
        
        showLoading(true);
        
        // Load course info
        firestore.collection("courses")
                .document(courseId)
                .get()
                .addOnSuccessListener(courseDoc -> {
                    if (courseDoc.exists()) {
                        // Set course description
                        String description = courseDoc.getString("description");
                        if (description != null && !description.isEmpty()) {
                            tvCourseDescription.setText(description);
                        } else {
                            tvCourseDescription.setText(R.string.no_description_available);
                        }
                        
                        // Set skills learned
                        String skills = courseDoc.getString("skillsLearned");
                        if (skills != null && !skills.isEmpty()) {
                            tvSkillsLearned.setText(skills);
                        } else {
                            tvSkillsLearned.setText(R.string.no_skills_info_available);
                        }
                        
                        // Set prerequisites
                        String prerequisites = courseDoc.getString("prerequisites");
                        if (prerequisites != null && !prerequisites.isEmpty()) {
                            tvPrerequisites.setText(prerequisites);
                        } else {
                            tvPrerequisites.setText(R.string.no_prerequisites);
                        }
                        
                        // Load instructor info
                        String instructorId = courseDoc.getString("instructorId");
                        if (instructorId != null && !instructorId.isEmpty()) {
                            loadInstructorInfo(instructorId);
                        } else {
                            hideInstructorSection();
                            showLoading(false);
                        }
                    } else {
                        showLoading(false);
                        Toast.makeText(getContext(), R.string.course_not_found, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(getContext(), "Error loading course info: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
    
    private void loadInstructorInfo(String instructorId) {
        firestore.collection("instructors")
                .document(instructorId)
                .get()
                .addOnSuccessListener(instructorDoc -> {
                    showLoading(false);
                    
                    if (instructorDoc.exists()) {
                        // Set instructor name
                        String name = instructorDoc.getString("name");
                        if (name != null && !name.isEmpty()) {
                            tvInstructorName.setText(name);
                        }
                        
                        // Set instructor title
                        String title = instructorDoc.getString("title");
                        if (title != null && !title.isEmpty()) {
                            tvInstructorTitle.setText(title);
                        }
                        
                        // Set instructor bio
                        String bio = instructorDoc.getString("bio");
                        if (bio != null && !bio.isEmpty()) {
                            tvInstructorBio.setText(bio);
                        }
                        
                        // Load instructor image
                        String imageUrl = instructorDoc.getString("imageUrl");
                        if (imageUrl != null && !imageUrl.isEmpty() && getContext() != null) {
                            Glide.with(getContext())
                                    .load(imageUrl)
                                    .placeholder(R.drawable.ic_profile_placeholder)
                                    .error(R.drawable.ic_profile_placeholder)
                                    .circleCrop()
                                    .into(ivInstructor);
                        }
                    } else {
                        hideInstructorSection();
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    hideInstructorSection();
                });
    }
    
    private void hideInstructorSection() {
        if (getView() != null) {
            getView().findViewById(R.id.instructorSection).setVisibility(View.GONE);
        }
    }
    
    private void showLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }
}