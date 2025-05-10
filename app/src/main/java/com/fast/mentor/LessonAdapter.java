package com.fast.mentor;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fast.mentor.R;
import com.fast.mentor.model.Lesson;

import java.util.List;

public class LessonAdapter extends RecyclerView.Adapter<LessonAdapter.LessonViewHolder> {

    private Context context;
    private List<Lesson> lessons;
    private boolean isEnrolled;

    public LessonAdapter(Context context, List<Lesson> lessons, boolean isEnrolled) {
        this.context = context;
        this.lessons = lessons;
        this.isEnrolled = isEnrolled;
    }

    @NonNull
    @Override
    public LessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_lesson, parent, false);
        return new LessonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LessonViewHolder holder, int position) {
        Lesson lesson = lessons.get(position);
        holder.bind(lesson);
    }

    @Override
    public int getItemCount() {
        return lessons.size();
    }

    public void updateLessons(List<Lesson> lessons) {
        this.lessons = lessons;
        notifyDataSetChanged();
    }
    
    public void setEnrolled(boolean enrolled) {
        this.isEnrolled = enrolled;
        notifyDataSetChanged();
    }

    public class LessonViewHolder extends RecyclerView.ViewHolder {
        private ImageView lessonTypeIcon;
        private TextView lessonTitleTextView;
        private TextView lessonDurationTextView;
        private ImageView lessonStatusIcon;

        public LessonViewHolder(@NonNull View itemView) {
            super(itemView);
            lessonTypeIcon = itemView.findViewById(R.id.lessonTypeIcon);
            lessonTitleTextView = itemView.findViewById(R.id.lessonTitleTextView);
            lessonDurationTextView = itemView.findViewById(R.id.lessonDurationTextView);
            lessonStatusIcon = itemView.findViewById(R.id.lessonStatusIcon);
            
            // Set click listener for lesson item
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position == RecyclerView.NO_POSITION) return;
                
                Lesson lesson = lessons.get(position);
                
                if (isEnrolled) {
                    // Navigate to lesson content
                    openLessonContent(lesson);
                } else {
                    // Show message that user needs to enroll first
                    showEnrollmentRequiredMessage();
                }
            });
        }

        public void bind(Lesson lesson) {
            // Set lesson title
            lessonTitleTextView.setText(lesson.getTitle());
            
            // Set lesson duration
            lessonDurationTextView.setText(lesson.getDuration());
            
            // Set lesson type icon based on content type
            switch (lesson.getType()) {
                case VIDEO:
                    lessonTypeIcon.setImageResource(R.drawable.ic_video);
                    break;
                case DOCUMENT:
                    lessonTypeIcon.setImageResource(R.drawable.ic_document);
                    break;
                case QUIZ:
                    lessonTypeIcon.setImageResource(R.drawable.ic_quiz);
                    break;
                case ASSIGNMENT:
                    lessonTypeIcon.setImageResource(R.drawable.ic_assignment);
                    break;
                default:
                    lessonTypeIcon.setImageResource(R.drawable.ic_document);
                    break;
            }
            
            // Set completion status icon for enrolled users
            if (isEnrolled) {
                lessonStatusIcon.setVisibility(View.VISIBLE);
                
                if (lesson.isCompleted()) {
                    lessonStatusIcon.setImageResource(R.drawable.ic_check_circle);
                } else if (lesson.getProgress() > 0) {
                    lessonStatusIcon.setImageResource(R.drawable.ic_in_progress);
                } else {
                    lessonStatusIcon.setVisibility(View.GONE);
                }
            } else {
                lessonStatusIcon.setVisibility(View.GONE);
            }
        }
        
        private void openLessonContent(Lesson lesson) {
            // TODO: Navigate to appropriate lesson content activity based on type
            // For now, just show a toast or navigate to a placeholder activity
            
            // Example navigation to LessonContentActivity
            // Intent intent = new Intent(context, LessonContentActivity.class);
            // intent.putExtra(LessonContentActivity.EXTRA_LESSON_ID, lesson.getId());
            // context.startActivity(intent);
        }
        
        private void showEnrollmentRequiredMessage() {
            // Show a message that user needs to enroll in the course first
            // For now, just show a toast message
            // Toast.makeText(context, "Please enroll in this course to access lessons", Toast.LENGTH_SHORT).show();
        }
    }
}