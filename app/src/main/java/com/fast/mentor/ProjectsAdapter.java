package com.fast.mentor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class ProjectsAdapter extends FirestoreRecyclerAdapter<Project, ProjectsAdapter.ProjectViewHolder> {

    public ProjectsAdapter(@NonNull FirestoreRecyclerOptions<Project> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull ProjectViewHolder holder, int position, @NonNull Project project) {
        holder.title.setText(project.getTitle());
        holder.provider.setText(project.getProvider());
        holder.guided.setText(project.getGuidedProjectTitle());
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_project, parent, false);
        return new ProjectViewHolder(view);
    }

    static class ProjectViewHolder extends RecyclerView.ViewHolder {
        TextView title, provider, guided;

        public ProjectViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.projectTitle);
            provider = itemView.findViewById(R.id.projectProvider);
            guided = itemView.findViewById(R.id.projectGuidedTitle);
        }
    }
}