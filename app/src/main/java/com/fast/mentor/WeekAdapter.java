package com.fast.mentor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fast.mentor.R;

import java.util.List;

/**
 * Adapter for displaying course weeks and their modules
 */
public class WeekAdapter extends RecyclerView.Adapter<WeekAdapter.WeekViewHolder> {

    private final List<Week> weeks;
    private final OnContentItemClickListener listener;

    public interface OnContentItemClickListener {
        void onContentItemClick(ContentItem contentItem);
    }

    public WeekAdapter(List<Week> weeks, OnContentItemClickListener listener) {
        this.weeks = weeks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public WeekViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_week, parent, false);
        return new WeekViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeekViewHolder holder, int position) {
        Week week = weeks.get(position);
        holder.bind(week, listener);
    }

    @Override
    public int getItemCount() {
        return weeks.size();
    }

    static class WeekViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvWeekTitle;
        private final TextView tvWeekDescription;
        private final ImageView ivExpand;
        private final RecyclerView rvModules;
        private final View modulesContainer;

        public WeekViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWeekTitle = itemView.findViewById(R.id.tvWeekTitle);
            tvWeekDescription = itemView.findViewById(R.id.tvWeekDescription);
            ivExpand = itemView.findViewById(R.id.ivExpand);
            rvModules = itemView.findViewById(R.id.rvModules);
            modulesContainer = itemView.findViewById(R.id.modulesContainer);
        }

        public void bind(final Week week, final OnContentItemClickListener listener) {
            // Set week details
            tvWeekTitle.setText(String.format("Week %d: %s", week.getWeekNumber(), week.getTitle()));

            if (week.getDescription() != null && !week.getDescription().isEmpty()) {
                tvWeekDescription.setText(week.getDescription());
                tvWeekDescription.setVisibility(View.VISIBLE);
            } else {
                tvWeekDescription.setVisibility(View.GONE);
            }

            // Setup expand/collapse functionality
            updateExpandState(week);

            itemView.setOnClickListener(v -> {
                week.setExpanded(!week.isExpanded());
                updateExpandState(week);
            });

            // Setup modules RecyclerView
            if (week.getModules() != null && !week.getModules().isEmpty()) {
                Context context = itemView.getContext();
                boolean isEnrolled = true; // or false depending on your app logic

                ModuleAdapter moduleAdapter = new ModuleAdapter(context, week.getModules(), isEnrolled, listener);
                rvModules.setLayoutManager(new LinearLayoutManager(context));
                rvModules.setAdapter(moduleAdapter);
            }
        }



        private void updateExpandState(Week week) {
            // Update UI based on expanded state
            if (week.isExpanded()) {
                ivExpand.setImageResource(R.drawable.ic_collapse);
                modulesContainer.setVisibility(View.VISIBLE);
            } else {
                ivExpand.setImageResource(R.drawable.ic_expand);
                modulesContainer.setVisibility(View.GONE);
            }
        }
    }
}