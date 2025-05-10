package com.fast.mentor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fast.mentor.R;

import java.util.List;

/**
 * Adapter for displaying modules and their content items
 */
public class ModuleAdapter extends RecyclerView.Adapter<ModuleAdapter.ModuleViewHolder> {

    private final List<Module> modules;
    private final WeekAdapter.OnContentItemClickListener listener;

    public ModuleAdapter(List<Module> modules, WeekAdapter.OnContentItemClickListener listener) {
        this.modules = modules;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ModuleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_module, parent, false);
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

    static class ModuleViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvModuleTitle;
        private final TextView tvModuleProgress;
        private final ProgressBar progressBar;
        private final ImageView ivExpand;
        private final RecyclerView rvContentItems;
        private final View contentItemsContainer;

        public ModuleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvModuleTitle = itemView.findViewById(R.id.tvModuleTitle);
            tvModuleProgress = itemView.findViewById(R.id.tvModuleProgress);
            progressBar = itemView.findViewById(R.id.progressBar);
            ivExpand = itemView.findViewById(R.id.ivExpand);
            rvContentItems = itemView.findViewById(R.id.rvContentItems);
            contentItemsContainer = itemView.findViewById(R.id.contentItemsContainer);
        }

        public void bind(final Module module, final WeekAdapter.OnContentItemClickListener listener) {
            // Set module details
            tvModuleTitle.setText(module.getTitle());
            tvModuleProgress.setText(module.getFormattedProgress());
            progressBar.setProgress(Math.round(module.getProgressPercentage()));

            // Setup expand/collapse functionality
            updateExpandState(module);
            
            itemView.setOnClickListener(v -> {
                module.toggleExpanded();
                updateExpandState(module);
            });

            // Setup content items recyclerview
            if (module.getItems() != null && !module.getItems().isEmpty()) {
                ContentItemAdapter contentItemAdapter = new ContentItemAdapter(module.getItems(), listener);
                rvContentItems.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
                rvContentItems.setAdapter(contentItemAdapter);
            }
        }

        private void updateExpandState(Module module) {
            // Update UI based on expanded state
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