package com.fast.mentor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.jspecify.annotations.NonNull;

import java.util.List;

public class InstructorAdapter extends RecyclerView.Adapter<InstructorAdapter.ViewHolder> {
    private List<CourseInfo.Instructor> instructors;

    public InstructorAdapter(List<CourseInfo.Instructor> instructors) {
        this.instructors = instructors;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_instructor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CourseInfo.Instructor instructor = instructors.get(position);
        holder.tvName.setText(instructor.getName());
        holder.tvRole.setText(instructor.getRole());
        holder.tvAffiliation.setText(instructor.getAffiliation());
    }

    @Override
    public int getItemCount() { return instructors.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvRole, tvAffiliation;

        ViewHolder(View view) {
            super(view);
            tvName = view.findViewById(R.id.tvInstructorName);
            tvRole = view.findViewById(R.id.tvInstructorRole);
            tvAffiliation = view.findViewById(R.id.tvInstructorAffiliation);
        }
    }
}
