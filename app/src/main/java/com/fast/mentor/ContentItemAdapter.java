package com.fast.mentor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fast.mentor.R;

import java.util.List;

/**
 * Adapter for displaying content items (videos, readings, quizzes, etc.)
 */
public class ContentItemAdapter extends RecyclerView.Adapter<ContentItemAdapter.ContentItemViewHolder> {

    private final List<ContentItem> contentItems;
    private final WeekAdapter.OnContentItemClickListener listener;

    public ContentItemAdapter(List<ContentItem> contentItems, WeekAdapter.OnContentItemClickListener listener) {
        this.contentItems = contentItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ContentItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_content, parent, false);
        return new ContentItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContentItemViewHolder holder, int position) {
        ContentItem contentItem = contentItems.get(position);
        holder.bind(contentItem, listener);
    }

    @Override
    public int getItemCount() {
        return contentItems.size();
    }

    static class ContentItemViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivContentType;
        private final TextView tvContentTitle;
        private final TextView tvContentDuration;
        private final Button btnAction;
        private final ImageView ivCompleted;

        public ContentItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ivContentType = itemView.findViewById(R.id.ivContentType);
            tvContentTitle = itemView.findViewById(R.id.tvContentTitle);
            tvContentDuration = itemView.findViewById(R.id.tvContentDuration);
            btnAction = itemView.findViewById(R.id.btnAction);
            ivCompleted = itemView.findViewById(R.id.ivCompleted);
        }

        public void bind(final ContentItem contentItem, final WeekAdapter.OnContentItemClickListener listener) {
            // Set content item details
            tvContentTitle.setText(contentItem.getTitle());
            tvContentDuration.setText(contentItem.getFormattedDuration());
            
            // Set appropriate icon based on content type
            ivContentType.setImageResource(contentItem.getIconResource());
            
            // Set completion status
            ivCompleted.setVisibility(contentItem.isCompleted() ? View.VISIBLE : View.GONE);
            
            // Set button text based on content type and completion status
            btnAction.setText(contentItem.getButtonText());
            
            // Set click listener for the action button
            btnAction.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onContentItemClick(contentItem);
                }
            });
            
            // Make the entire item clickable as well
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onContentItemClick(contentItem);
                }
            });
        }
    }
}