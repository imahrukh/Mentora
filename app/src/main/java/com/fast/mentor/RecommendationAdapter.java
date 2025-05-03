package com.fast.mentor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.jspecify.annotations.NonNull;

import java.util.List;

public class RecommendationAdapter extends RecyclerView.Adapter<RecommendationAdapter.ViewHolder> {
    private List<Recommendation> recommendations;
    private Context context;

    public RecommendationAdapter(List<Recommendation> recommendations) {
        this.recommendations = recommendations;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_recommendation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recommendation recommendation = recommendations.get(position);

        // Set basic information
        holder.tvTitle.setText(recommendation.getTitle());
        holder.tvReason.setText(recommendation.getReason());

        // Handle duration display
        if(recommendation.getRecommendedDuration() > 0) {
            holder.tvDuration.setText(formatDuration(recommendation.getRecommendedDuration()));
            holder.tvDuration.setTextColor(ContextCompat.getColor(context, R.color.teal));
        } else {
            holder.tvDuration.setText("Next Step");
            holder.tvDuration.setTextColor(ContextCompat.getColor(context, R.color.red));
        }

        // Style based on recommendation type
        if(recommendation.getType().equals("NextStep")) {
            holder.tvTitle.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_next_step, 0, 0, 0
            );
        } else {
            holder.tvTitle.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_adaptive, 0, 0, 0
            );
        }
    }

    private String formatDuration(long minutes) {
        if(minutes < 60) {
            return minutes + " min";
        } else {
            return (minutes/60) + " hr " + (minutes%60) + " min";
        }
    }

    @Override
    public int getItemCount() { return recommendations.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvReason, tvDuration;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvReason = itemView.findViewById(R.id.tvReason);
            tvDuration = itemView.findViewById(R.id.tvDuration);
        }
    }
}