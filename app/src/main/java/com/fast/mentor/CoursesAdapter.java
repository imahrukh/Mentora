package com.fast.mentor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

public class CoursesAdapter extends FirestoreRecyclerAdapter<Course, CoursesAdapter.CourseViewHolder> {

    public CoursesAdapter(@NonNull FirestoreRecyclerOptions<Course> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull CourseViewHolder holder, int position, @NonNull Course course) {
        holder.title.setText(course.getTitle());
        holder.provider.setText(course.getProvider());

        // Add click listener if needed
        holder.itemView.setOnClickListener(v -> {
            DocumentSnapshot snapshot = getSnapshots().getSnapshot(position);
            String courseId = snapshot.getId();
            // Handle click
        });
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_course, parent, false);
        return new CourseViewHolder(view);
    }

    static class CourseViewHolder extends RecyclerView.ViewHolder {
        TextView title, provider;

        public CourseViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.courseTitle);
            provider = itemView.findViewById(R.id.courseProvider);
        }
    }
}