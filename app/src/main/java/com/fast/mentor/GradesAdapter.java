package com.fast.mentor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GradesAdapter extends RecyclerView.Adapter<GradesAdapter.ViewHolder> {
    private List<GradeItem> gradeItems;
    private Context context;

    public GradesAdapter(List<GradeItem> gradeItems) {
        this.gradeItems = gradeItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_grade, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GradeItem item = gradeItems.get(position);

        holder.cbCompleted.setChecked(item.isCompleted());
        holder.tvTitle.setText(item.getTitle());
        holder.tvType.setText(item.getType());
        holder.tvWeight.setText(item.getWeight() + "%");

        // Set text color based on completion
        int textColor = item.isCompleted() ?
                ContextCompat.getColor(context, R.color.red) :
                ContextCompat.getColor(context, R.color.green);

        holder.tvTitle.setTextColor(textColor);
    }

    @Override
    public int getItemCount() { return gradeItems.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCheckBox cbCompleted;
        TextView tvTitle, tvType, tvWeight;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cbCompleted = itemView.findViewById(R.id.cbCompleted);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvType = itemView.findViewById(R.id.tvType);
            tvWeight = itemView.findViewById(R.id.tvWeight);
        }
    }
}