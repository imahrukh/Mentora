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

public class ProjectsAdapter extends RecyclerView.Adapter<ProjectsAdapter.ProjectViewHolder> {
    private List<Project> projectList;
    private Context context;

    public ProjectsAdapter(Context context, List<Project> projectList) {
        this.context = context;
        this.projectList = projectList;
    }


    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_project, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        Project project = projectList.get(position);
        holder.title.setText(project.getTitle());
        holder.provider.setText(project.getProvider());
        holder.guided.setText(project.getGuidedProjectTitle());
        holder.image.setImageResource(project.getImageResId());

        holder.itemView.setOnClickListener(v ->
                Toast.makeText(context, "Feature not available yet", Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public int getItemCount() {
        return projectList.size();
    }

    static class ProjectViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, provider, guided;

        ProjectViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.projectImage);
            title = itemView.findViewById(R.id.projectTitle);
            provider = itemView.findViewById(R.id.projectProvider);
            guided = itemView.findViewById(R.id.projectGuidedTitle);
        }
    }
}
