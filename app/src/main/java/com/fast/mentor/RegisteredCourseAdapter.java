package com.fast.mentor;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RegisteredCourseAdapter extends RecyclerView.Adapter<RegisteredCourseAdapter.ViewHolder> {

    private Context context;
    private List<Course> courses;

    public RegisteredCourseAdapter(Context context, List<Course> courses) {
        this.context = context;
        this.courses = courses;
    }

    @NonNull
    @Override
    public RegisteredCourseAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_registered_course, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RegisteredCourseAdapter.ViewHolder holder, int position) {
        Course course = courses.get(position);
        holder.title.setText(course.getTitle());
        holder.provider.setText(course.getProvider());
        holder.moduleTitle.setText(course.getModuleTitle());
        holder.moduleProgress.setText(course.getModuleProgress());

        holder.btnContinue.setOnClickListener(v ->
                context.startActivity(new Intent(context, CourseDetailActivity.class))
        );
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, provider, moduleTitle, moduleProgress;
        Button btnContinue;

        public ViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.courseTitle);
            provider = view.findViewById(R.id.courseProvider);
            moduleTitle = view.findViewById(R.id.moduleTitle);
            moduleProgress = view.findViewById(R.id.moduleProgress);
            btnContinue = view.findViewById(R.id.btnContinue);
        }
    }
    public void setCourses(List<Course> newCourses) {
        this.courses = newCourses;
        notifyDataSetChanged();
    }
}

