package com.fast.mentor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fast.mentor.R;

import java.util.List;
import java.util.Map;

/**
 * Adapter for displaying course modules and their content items
 */
public class CourseModuleAdapter extends RecyclerView.Adapter<CourseModuleAdapter.ModuleViewHolder> {
    
    private Context context;
    private List<CourseModule> modules;
    private Map<String, List<ContentItem>> moduleContentMap;
    private Map<String, Boolean> moduleExpandedMap;
    private OnModuleClickListener listener;
    
    /**
     * Interface for handling module click events
     */
    public interface OnModuleClickListener {
        void onModuleClick(int position, String moduleId);
        void onContentItemClick(ContentItem contentItem);
    }
    
    public CourseModuleAdapter(Context context, List<CourseModule> modules,
                              Map<String, List<ContentItem>> moduleContentMap,
                              Map<String, Boolean> moduleExpandedMap,
                              OnModuleClickListener listener) {
        this.context = context;
        this.modules = modules;
        this.moduleContentMap = moduleContentMap;
        this.moduleExpandedMap = moduleExpandedMap;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ModuleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_course_module, parent, false);
        return new ModuleViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ModuleViewHolder holder, int position) {
        CourseModule module = modules.get(position);
        
        // Set module title and info
        holder.tvModuleTitle.setText(module.getTitle());
        holder.tvModuleInfo.setText(context.getString(R.string.module_info_format,
                module.getLessonsCount(), module.getFormattedDuration()));
        
        // Set progress
        holder.progressBar.setProgress(module.getProgress());
        holder.tvProgress.setText(context.getString(R.string.progress_percentage, module.getProgress()));
        
        // Set expanded state
        boolean isExpanded = moduleExpandedMap.containsKey(module.getId()) && 
                moduleExpandedMap.get(module.getId());
        
        holder.ivExpand.setImageResource(isExpanded ? 
                R.drawable.ic_collapse : R.drawable.ic_expand);
        
        holder.contentContainer.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        
        // Clear previous content
        holder.contentContainer.removeAllViews();
        
        // Add content items if expanded
        if (isExpanded && moduleContentMap.containsKey(module.getId())) {
            List<ContentItem> contentItems = moduleContentMap.get(module.getId());
            
            for (ContentItem item : contentItems) {
                View contentView = LayoutInflater.from(context)
                        .inflate(R.layout.item_content, holder.contentContainer, false);
                
                // Find views
                ImageView ivContentIcon = contentView.findViewById(R.id.ivContentIcon);
                TextView tvContentTitle = contentView.findViewById(R.id.tvContentTitle);
                TextView tvContentInfo = contentView.findViewById(R.id.tvContentInfo);
                ImageView ivCompletionStatus = contentView.findViewById(R.id.ivCompletionStatus);
                
                // Set content data
                ivContentIcon.setImageResource(item.getTypeIconResource());
                tvContentTitle.setText(item.getTitle());
                
                String contentType = getContentTypeString(item.getType());
                String duration = item.getDuration() > 0 ? item.getFormattedDuration() : "";
                
                if (!duration.isEmpty()) {
                    tvContentInfo.setText(context.getString(R.string.content_info_format, 
                            contentType, duration));
                } else {
                    tvContentInfo.setText(contentType);
                }
                
                // Set completion status
                ivCompletionStatus.setImageResource(item.isCompleted() ? 
                        R.drawable.ic_completed : R.drawable.ic_pending);
                
                // Set click listener
                contentView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onContentItemClick(item);
                    }
                });
                
                // Add to container
                holder.contentContainer.addView(contentView);
            }
        }
        
        // Set click listener on module header
        holder.moduleHeader.setOnClickListener(v -> {
            if (listener != null) {
                listener.onModuleClick(holder.getAdapterPosition(), module.getId());
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return modules.size();
    }
    
    private String getContentTypeString(String type) {
        switch (type) {
            case "video":
                return context.getString(R.string.video);
            case "reading":
                return context.getString(R.string.reading);
            case "quiz":
                return context.getString(R.string.quiz);
            case "assignment":
                return context.getString(R.string.assignment);
            default:
                return type;
        }
    }
    
    /**
     * ViewHolder for module items
     */
    static class ModuleViewHolder extends RecyclerView.ViewHolder {
        LinearLayout moduleHeader;
        TextView tvModuleTitle;
        TextView tvModuleInfo;
        TextView tvProgress;
        ProgressBar progressBar;
        ImageView ivExpand;
        LinearLayout contentContainer;
        
        public ModuleViewHolder(@NonNull View itemView) {
            super(itemView);
            moduleHeader = itemView.findViewById(R.id.moduleHeader);
            tvModuleTitle = itemView.findViewById(R.id.tvModuleTitle);
            tvModuleInfo = itemView.findViewById(R.id.tvModuleInfo);
            tvProgress = itemView.findViewById(R.id.tvProgress);
            progressBar = itemView.findViewById(R.id.progressBar);
            ivExpand = itemView.findViewById(R.id.ivExpand);
            contentContainer = itemView.findViewById(R.id.contentContainer);
        }
    }
}