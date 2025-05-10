package com.fast.mentor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

/**
 * Fragment for displaying and participating in course discussions
 */
public class CourseDiscussionsFragment extends Fragment {

    private String courseId;
    
    private RecyclerView rvDiscussions;
    private EditText etMessage;
    private Button btnSend;
    private ProgressBar progressBar;
    private TextView tvNoDiscussions;
    
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;
    
    public CourseDiscussionsFragment() {
        // Required empty public constructor
    }
    
    public static CourseDiscussionsFragment newInstance(String courseId) {
        CourseDiscussionsFragment fragment = new CourseDiscussionsFragment();
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
        
        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_course_discussions, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        rvDiscussions = view.findViewById(R.id.rvDiscussions);
        etMessage = view.findViewById(R.id.etMessage);
        btnSend = view.findViewById(R.id.btnSend);
        progressBar = view.findViewById(R.id.progressBar);
        tvNoDiscussions = view.findViewById(R.id.tvNoDiscussions);
        
        // Setup recycler view
        rvDiscussions.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Setup send button
        btnSend.setOnClickListener(v -> sendMessage());
        
        // Initially show placeholder message
        showEmptyState();
        tvNoDiscussions.setText(R.string.discussions_coming_soon);
    }
    
    private void sendMessage() {
        // In a full implementation, this would send a message to Firestore
        
        String message = etMessage.getText().toString().trim();
        if (message.isEmpty()) {
            Toast.makeText(getContext(), R.string.empty_message, Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (currentUser == null || courseId == null) {
            Toast.makeText(getContext(), R.string.not_logged_in, Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Clear the input field
        etMessage.setText("");
        
        // Show coming soon message
        Toast.makeText(getContext(), R.string.discussions_coming_soon, Toast.LENGTH_SHORT).show();
    }
    
    private void showEmptyState() {
        rvDiscussions.setVisibility(View.GONE);
        tvNoDiscussions.setVisibility(View.VISIBLE);
    }
    
    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }
}