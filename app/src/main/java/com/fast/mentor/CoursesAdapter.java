package com.fast.mentor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CoursesAdapter extends RecyclerView.Adapter<CoursesAdapter.CourseViewHolder> {
    private List<Courses> courseList;
    private Context context;

    public CoursesAdapter(Context context, List<Courses> courseList) {
        this.context = context;
        this.courseList = courseList;
    }

    public CoursesAdapter(List<Courses> courseList, ExploreActivity exploreActivity) {
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Courses course = courseList.get(position);
        holder.title.setText(course.getTitle());
        holder.provider.setText(course.getProvider());
        holder.image.setImageResource(course.getImageResId());

        holder.itemView.setOnClickListener(v ->
                Toast.makeText(context, "Feature not available yet", Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    static class CourseViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, provider;

        CourseViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.courseImage);
            title = itemView.findViewById(R.id.courseTitle);
            provider = itemView.findViewById(R.id.courseProvider);
        }
    }
}

