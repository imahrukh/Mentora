package com.fast.mentor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fast.mentor.R;

import java.util.List;

/**
 * Adapter for displaying enrolled courses with progress
 */
public class EnrolledCourseAdapter extends RecyclerView.Adapter<EnrolledCourseAdapter.EnrolledCourseViewHolder> {

    private final List<EnrolledCourse> enrolledCourses;
    private final OnCourseClickListener listener;

    // Interface for course click events
    public interface OnCourseClickListener {
        void onCourseClick(EnrolledCourse course);
    }

    public EnrolledCourseAdapter(List<EnrolledCourse> enrolledCourses, OnCourseClickListener listener) {
        this.enrolledCourses = enrolledCourses;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EnrolledCourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_enrolled_course, parent, false);
        return new EnrolledCourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EnrolledCourseViewHolder holder, int position) {
        EnrolledCourse course = enrolledCourses.get(position);
        holder.bind(course, listener);
    }

    @Override
    public int getItemCount() {
        return enrolledCourses.size();
    }

    static class EnrolledCourseViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivCourseImage;
        private final TextView tvCourseTitle;
        private final TextView tvInstructorName;
        private final TextView tvProgressText;
        private final TextView tvTimeRemaining;
        private final ProgressBar progressBar;
        private final Button btnContinue;

        public EnrolledCourseViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCourseImage = itemView.findViewById(R.id.ivCourseImage);
            tvCourseTitle = itemView.findViewById(R.id.tvCourseTitle);
            tvInstructorName = itemView.findViewById(R.id.tvInstructorName);
            tvProgressText = itemView.findViewById(R.id.tvProgressText);
            tvTimeRemaining = itemView.findViewById(R.id.tvTimeRemaining);
            progressBar = itemView.findViewById(R.id.progressBar);
            btnContinue = itemView.findViewById(R.id.btnContinue);
        }

        public void bind(EnrolledCourse course, OnCourseClickListener listener) {
            // Set course details
            tvCourseTitle.setText(course.getTitle());
            tvInstructorName.setText(course.getInstructorName());
            
            // Set progress
            tvProgressText.setText(course.getFormattedProgress());
            tvTimeRemaining.setText(course.getFormattedTimeRemaining());
            progressBar.setProgress(Math.round(course.getProgress()));
            
            // Load thumbnail
            if (course.getThumbnailUrl() != null && !course.getThumbnailUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(course.getThumbnailUrl())
                        .placeholder(R.drawable.placeholder_course)
                        .error(R.drawable.error_course)
                        .centerCrop()
                        .into(ivCourseImage);
            } else {
                ivCourseImage.setImageResource(R.drawable.placeholder_course);
            }
            
            // Set click listeners
            btnContinue.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCourseClick(course);
                }
            });
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCourseClick(course);
                }
            });
        }
    }
}