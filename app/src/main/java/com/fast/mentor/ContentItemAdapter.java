package com.fast.mentor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

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
        private final ImageView ivContentIcon;
        private final TextView tvContentTitle;
        private final TextView tvContentInfo;
        private final ImageView ivCompletionStatus;

        public ContentItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ivContentIcon = itemView.findViewById(R.id.ivContentIcon);
            tvContentTitle = itemView.findViewById(R.id.tvContentTitle);
            tvContentInfo = itemView.findViewById(R.id.tvContentInfo);
            ivCompletionStatus = itemView.findViewById(R.id.ivCompletionStatus);
        }

        public void bind(final ContentItem contentItem, final WeekAdapter.OnContentItemClickListener listener) {
            // Set content item details
            tvContentTitle.setText(contentItem.getTitle());
            tvContentInfo.setText(contentItem.getType().substring(0, 1).toUpperCase() + contentItem.getType().substring(1) + " • " + contentItem.getFormattedDuration());

            // Set appropriate icon based on content type
            ivContentIcon.setImageResource(contentItem.getTypeIconResource());

            // Set completion status
            ivCompletionStatus.setVisibility(contentItem.isCompleted() ? View.VISIBLE : View.GONE);

            // Make the entire item clickable
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onContentItemClick(contentItem);
                }
            });
        }
    }
}
