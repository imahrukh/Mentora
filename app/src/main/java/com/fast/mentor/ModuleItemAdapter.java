package com.fast.mentor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;
import java.util.Map;

public class ModuleItemAdapter extends RecyclerView.Adapter<ModuleItemAdapter.ViewHolder> {
    private List<ModuleItem> items;
    private Map<String, ModuleProgress> progressMap;
    private Context context;

    public ModuleItemAdapter(List<ModuleItem> items, Map<String, ModuleProgress> progressMap) {
        this.items=items;
        this.progressMap=progressMap;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        ImageView ivStatus;
        TextView tvItemTitle, tvItemType, tvOptional;

        ViewHolder(View view) {
            super(view);
            cardView = view.findViewById(R.id.cardView);
            ivStatus = view.findViewById(R.id.ivStatus);
            tvItemTitle = view.findViewById(R.id.tvItemTitle);
            tvItemType = view.findViewById(R.id.tvItemType);
            tvOptional = view.findViewById(R.id.tvOptional);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_module_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ModuleItem item = items.get(position);
        ModuleProgress progress = progressMap.get(item.getItemId());

        // Set border color based on item type
        int strokeColor = getStrokeColor(item.getType());
        holder.cardView.setStrokeColor(strokeColor);

        // Set status icon
        int iconColor = (progress != null && progress.isCompleted()) ?
                ContextCompat.getColor(context, R.color.green) :
                ContextCompat.getColor(context, R.color.grey2);
        holder.ivStatus.setColorFilter(iconColor);

        // Set content
        holder.tvItemTitle.setText(item.getTitle());
        holder.tvItemType.setText(item.getType().toString());
        holder.tvOptional.setVisibility(item.isOptional() ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    private int getStrokeColor(ItemType type) {
        switch (type) {
            case QUIZ:
            case ASSIGNMENT:
            case LAB:
                return ContextCompat.getColor(context, R.color.teal);
            default: // LECTURE
                return ContextCompat.getColor(context, R.color.red);
        }
    }
}