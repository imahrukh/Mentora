package com.fast.mentor;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fast.mentor.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fragment for displaying course content (modules, lessons)
 */
public class CourseContentFragment extends Fragment implements CourseModuleAdapter.OnModuleClickListener {

    private String courseId;
    
    private RecyclerView rvModules;
    private ProgressBar progressBar;
    private TextView tvNoContent;
    
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;
    
    private CourseModuleAdapter moduleAdapter;
    private List<CourseModule> modules;
    
    private Map<String, List<ContentItem>> moduleContentMap;
    private Map<String, Boolean> moduleExpandedMap;
    
    public CourseContentFragment() {
        // Required empty public constructor
    }
    
    public static CourseContentFragment newInstance(String courseId) {
        CourseContentFragment fragment = new CourseContentFragment();
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
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        
        // Initialize data structures
        modules = new ArrayList<>();
        moduleContentMap = new HashMap<>();
        moduleExpandedMap = new HashMap<>();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_course_content, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        rvModules = view.findViewById(R.id.rvModules);
        progressBar = view.findViewById(R.id.progressBar);
        tvNoContent = view.findViewById(R.id.tvNoContent);
        
        // Setup recycler view
        rvModules.setLayoutManager(new LinearLayoutManager(getContext()));
        moduleAdapter = new CourseModuleAdapter(getContext(), modules, moduleContentMap, moduleExpandedMap, this);
        rvModules.setAdapter(moduleAdapter);
        
        // Load course modules and content
        loadCourseContent();
    }
    
    private void loadCourseContent() {
        if (courseId == null) {
            showEmptyState();
            return;
        }
        
        showLoading(true);
        
        // Load modules
        firestore.collection("modules")
                .whereEqualTo("courseId", courseId)
                .orderBy("orderIndex", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        showEmptyState();
                        return;
                    }
                    
                    modules.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        CourseModule module = document.toObject(CourseModule.class);
                        module.setId(document.getId());
                        modules.add(module);
                        
                        // Initialize moduleExpandedMap with all modules collapsed
                        moduleExpandedMap.put(module.getId(), false);
                        
                        // Load content for each module
                        loadModuleContent(module.getId());
                    }
                    
                    moduleAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    showEmptyState();
                    Toast.makeText(getContext(), "Failed to load modules: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
    
    private void loadModuleContent(String moduleId) {
        firestore.collection("lessons")
                .whereEqualTo("moduleId", moduleId)
                .orderBy("orderIndex", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ContentItem> contentItems = new ArrayList<>();
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        ContentItem contentItem = new ContentItem();
                        contentItem.setId(document.getId());
                        contentItem.setModuleId(document.getString("moduleId"));
                        contentItem.setTitle(document.getString("title"));
                        contentItem.setDescription(document.getString("description"));
                        contentItem.setType(document.getString("type"));
                        contentItem.setContentUrl(document.getString("contentUrl"));
                        
                        // Get duration (if available)
                        if (document.contains("duration") && document.getLong("duration") != null) {
                            contentItem.setDuration(document.getLong("duration").intValue());
                        } else {
                            contentItem.setDuration(0);
                        }
                        
                        // Get order index
                        if (document.contains("orderIndex") && document.getLong("orderIndex") != null) {
                            contentItem.setOrderIndex(document.getLong("orderIndex").intValue());
                        } else {
                            contentItem.setOrderIndex(0);
                        }
                        
                        // Get optional flag
                        contentItem.setOptional(Boolean.TRUE.equals(document.getBoolean("isOptional")));
                        
                        // Default not completed
                        contentItem.setCompleted(false);
                        
                        contentItems.add(contentItem);
                    }
                    
                    // Sort by order index
                    Collections.sort(contentItems, Comparator.comparingInt(ContentItem::getOrderIndex));
                    
                    // Store in map
                    moduleContentMap.put(moduleId, contentItems);
                    
                    // Check completion status for each content item
                    checkCompletionStatus(moduleId, contentItems);
                    
                    // Update adapter
                    moduleAdapter.notifyDataSetChanged();
                    showLoading(false);
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(getContext(), "Failed to load lessons: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
    
    private void checkCompletionStatus(String moduleId, List<ContentItem> contentItems) {
        if (currentUser == null || contentItems.isEmpty()) {
            return;
        }
        
        String userId = currentUser.getUid();
        
        // For each content item, check if it's completed
        for (ContentItem item : contentItems) {
            firestore.collection("progress")
                    .document(userId + "_" + item.getId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists() && 
                                Boolean.TRUE.equals(documentSnapshot.getBoolean("isCompleted"))) {
                            item.setCompleted(true);
                            moduleAdapter.notifyDataSetChanged();
                            
                            // Update module progress
                            updateModuleProgress(moduleId);
                        }
                    });
        }
    }
    
    private void updateModuleProgress(String moduleId) {
        List<ContentItem> items = moduleContentMap.get(moduleId);
        if (items == null || items.isEmpty()) {
            return;
        }
        
        // Count completed items
        int totalItems = items.size();
        int completedItems = 0;
        
        for (ContentItem item : items) {
            if (item.isCompleted()) {
                completedItems++;
            }
        }
        
        // Calculate progress percentage
        int progressPercentage = (int) (((float) completedItems / totalItems) * 100);
        
        // Update module progress in UI
        for (CourseModule module : modules) {
            if (module.getId().equals(moduleId)) {
                module.setProgress(progressPercentage);
                moduleAdapter.notifyDataSetChanged();
                break;
            }
        }
    }
    
    @Override
    public void onModuleClick(int position, String moduleId) {
        // Toggle module expanded state
        boolean isExpanded = moduleExpandedMap.get(moduleId);
        moduleExpandedMap.put(moduleId, !isExpanded);
        
        // Update adapter
        moduleAdapter.notifyItemChanged(position);
    }
    
    @Override
    public void onContentItemClick(ContentItem contentItem) {
        if (contentItem == null) {
            return;
        }
        
        // Launch appropriate activity based on content type
        switch (contentItem.getType()) {
            case "video":
                launchVideoPlayer(contentItem);
                break;
            case "reading":
                launchPdfViewer(contentItem);
                break;
            case "quiz":
                launchQuizActivity(contentItem);
                break;
            case "assignment":
                launchAssignmentSubmission(contentItem);
                break;
            default:
                Toast.makeText(getContext(), "Unknown content type: " + contentItem.getType(),
                        Toast.LENGTH_SHORT).show();
                break;
        }
    }
    
    private void launchVideoPlayer(ContentItem contentItem) {
        Intent intent = new Intent(getActivity(), VideoLessonActivity.class);
        intent.putExtra("contentItem", contentItem);
        intent.putExtra("courseId", courseId);
        startActivity(intent);
    }
    
    private void launchPdfViewer(ContentItem contentItem) {
        Intent intent = new Intent(getActivity(), PDFViewerActivity.class);
        intent.putExtra("contentItem", contentItem);
        intent.putExtra("courseId", courseId);
        startActivity(intent);
    }
    
    private void launchQuizActivity(ContentItem contentItem) {
        Intent intent = new Intent(getActivity(), PDFViewerActivity.class);
        intent.putExtra("contentItem", contentItem);
        intent.putExtra("courseId", courseId);
        startActivity(intent);
    }
    
    private void launchAssignmentSubmission(ContentItem contentItem) {
        Intent intent = new Intent(getActivity(), AssignmentSubmissionActivity.class);
        intent.putExtra("contentItem", contentItem);
        intent.putExtra("courseId", courseId);
        startActivity(intent);
    }
    
    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        
        if (!isLoading && !modules.isEmpty()) {
            tvNoContent.setVisibility(View.GONE);
            rvModules.setVisibility(View.VISIBLE);
        }
    }
    
    private void showEmptyState() {
        showLoading(false);
        tvNoContent.setVisibility(View.VISIBLE);
        rvModules.setVisibility(View.GONE);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        // Refresh content when returning to the fragment
        // (e.g., after completing a video or quiz)
        if (courseId != null && !modules.isEmpty()) {
            for (CourseModule module : modules) {
                List<ContentItem> items = moduleContentMap.get(module.getId());
                if (items != null && !items.isEmpty()) {
                    checkCompletionStatus(module.getId(), items);
                }
            }
        }
    }
}