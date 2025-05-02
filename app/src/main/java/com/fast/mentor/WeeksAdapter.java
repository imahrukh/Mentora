package com.fast.mentor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class WeeksAdapter extends RecyclerView.Adapter<WeeksAdapter.ViewHolder> {
    private List<Week> weeks;
    private int selectedPosition = 0;
    private OnWeekClickListener listener;

    public interface OnWeekClickListener {
        void onWeekClick(int position);
    }

    public WeeksAdapter(List<Week> weeks, OnWeekClickListener listener) {
        this.weeks = weeks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_week, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Week week = weeks.get(position);

        // Set week number text
        holder.btnWeek.setText("Week " + week.getWeekNumber());

        // Update appearance based on selection
        if (position == selectedPosition) {
            holder.btnWeek.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.teal));
            holder.btnWeek.setStrokeWidth(0);
        } else {
            holder.btnWeek.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.transparent));
            holder.btnWeek.setStrokeWidth(2);
        }

        holder.btnWeek.setOnClickListener(v -> {
            // Update selected position
            int previousPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();

            // Notify changes
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);

            // Trigger click listener
            listener.onWeekClick(selectedPosition);
        });
    }

    @Override
    public int getItemCount() { return weeks.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialButton btnWeek;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            btnWeek = itemView.findViewById(R.id.btnWeek);
        }
    }
}
