package com.fast.mentor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fast.mentor.R;

import java.util.List;

/**
 * Adapter for displaying courses in a RecyclerView
 */
public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    private final List<Course> courseList;
    private final OnCourseClickListener listener;

    // Interface for click events
    public interface OnCourseClickListener {
        void onCourseClick(Course course);
    }

    public CourseAdapter(List<Course> courseList, OnCourseClickListener listener) {
        this.courseList = courseList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course course = courseList.get(position);
        holder.bind(course, listener);
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    static class CourseViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivCourseThumbnail;
        private final TextView tvBadge;
        private final TextView tvCourseTitle;
        private final TextView tvInstructorName;
        private final RatingBar ratingBar;
        private final TextView tvRating;
        private final TextView tvCourseLevel;
        private final TextView tvDuration;
        private final TextView tvLessonsCount;
        private final TextView tvPrice;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCourseThumbnail = itemView.findViewById(R.id.ivCourseThumbnail);
            tvBadge = itemView.findViewById(R.id.tvBadge);
            tvCourseTitle = itemView.findViewById(R.id.tvCourseTitle);
            tvInstructorName = itemView.findViewById(R.id.tvInstructorName);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvCourseLevel = itemView.findViewById(R.id.tvCourseLevel);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvLessonsCount = itemView.findViewById(R.id.tvLessonsCount);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }

        public void bind(Course course, OnCourseClickListener listener) {
            // Set course title
            tvCourseTitle.setText(course.getTitle());
            
            // Set instructor name
            tvInstructorName.setText(course.getFullInstructorInfo());
            
            // Set course rating
            ratingBar.setRating(course.getRating());
            tvRating.setText(course.getFormattedRating());
            
            // Set course level
            tvCourseLevel.setText(course.getLevel());
            
            // Set duration and lessons count
            tvDuration.setText(course.getFormattedDuration());
            tvLessonsCount.setText(course.getFormattedLessons());
            
            // Set price
            tvPrice.setText(course.getFormattedPrice());
            
            // Set badge (Popular or New)
            if (course.isNew()) {
                tvBadge.setText(R.string.new_badge);
                tvBadge.setBackgroundResource(R.drawable.badge_new_background);
                tvBadge.setVisibility(View.VISIBLE);
            } else if (course.isPopular()) {
                tvBadge.setText(R.string.popular_badge);
                tvBadge.setBackgroundResource(R.drawable.badge_popular_background);
                tvBadge.setVisibility(View.VISIBLE);
            } else {
                tvBadge.setVisibility(View.GONE);
            }
            
            // Load thumbnail image with Glide
            if (course.getThumbnailUrl() != null && !course.getThumbnailUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(course.getThumbnailUrl())
                        .placeholder(R.drawable.placeholder_course)
                        .error(R.drawable.error_course)
                        .centerCrop()
                        .into(ivCourseThumbnail);
            } else {
                // Set a default image if no thumbnail is available
                ivCourseThumbnail.setImageResource(R.drawable.placeholder_course);
            }
            
            // Set click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCourseClick(course);
                }
            });
        }
    }
}