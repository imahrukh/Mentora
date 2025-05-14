package com.fast.mentor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Adapter for displaying modules and their content items
 */
public class ModuleAdapter extends RecyclerView.Adapter<ModuleAdapter.ModuleViewHolder> {
    private boolean isEnrolled;
    private final List<Module> modules;
    private final Context context;
    private final WeekAdapter.OnContentItemClickListener listener;

    public ModuleAdapter(Context context, List<Module> modules, boolean isEnrolled, WeekAdapter.OnContentItemClickListener listener) {
        this.context = context;
        this.modules = modules;
        this.isEnrolled = isEnrolled;
        this.listener = listener;
    }

    public void setEnrolled(boolean enrolled) {
        this.isEnrolled = enrolled;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ModuleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_module, parent, false);
        return new ModuleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ModuleViewHolder holder, int position) {
        Module module = modules.get(position);
        holder.bind(module, listener);
    }

    @Override
    public int getItemCount() {
        return modules.size();
    }

    public void updateModules(List<Module> updatedModules) {
        modules.clear();
        modules.addAll(updatedModules);
        notifyDataSetChanged();
    }

    static class ModuleViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvModuleTitle;
        private final TextView tvModuleProgress;
        private final ProgressBar progressBar;
        private final ImageView ivExpand;
        private final RecyclerView rvContentItems;
        private final View contentItemsContainer;

        public ModuleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvModuleTitle = itemView.findViewById(R.id.moduleTitleTextView);
            tvModuleProgress = itemView.findViewById(R.id.moduleProgressTextView);
            progressBar = itemView.findViewById(R.id.progressBar);
            ivExpand = itemView.findViewById(R.id.expandCollapseIcon);
            rvContentItems = itemView.findViewById(R.id.lessonsRecyclerView);
            contentItemsContainer = itemView.findViewById(R.id.moduleContentLayout);
        }

        public void bind(final Module module, final WeekAdapter.OnContentItemClickListener listener) {
            tvModuleTitle.setText(module.getTitle());
            tvModuleProgress.setText(module.getFormattedProgress());
            tvModuleProgress.setVisibility(View.VISIBLE);
            progressBar.setProgress(Math.round(module.getProgressPercentage()));

            updateExpandState(module);

            itemView.setOnClickListener(v -> {
                module.toggleExpanded();
                updateExpandState(module);
            });

            if (module.getItems() != null && !module.getItems().isEmpty()) {
                ContentItemAdapter contentItemAdapter = new ContentItemAdapter(module.getItems(), listener);
                rvContentItems.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
                rvContentItems.setAdapter(contentItemAdapter);
            }
        }

        private void updateExpandState(Module module) {
            if (module.isExpanded()) {
                ivExpand.setImageResource(R.drawable.ic_collapse);
                contentItemsContainer.setVisibility(View.VISIBLE);
            } else {
                ivExpand.setImageResource(R.drawable.ic_expand);
                contentItemsContainer.setVisibility(View.GONE);
            }
        }
    }
}
