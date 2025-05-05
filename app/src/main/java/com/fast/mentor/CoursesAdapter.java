package com.fast.mentor;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
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
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, CourseDetailActivity.class);
            intent.putExtra(IntentConstants.EXTRA_COURSE_ID, courseId);

            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
                    (Activity) context,
                    holder.imageView, // Shared element
                    "course_image" // Transition name
            );

            context.startActivity(intent, options.toBundle());
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