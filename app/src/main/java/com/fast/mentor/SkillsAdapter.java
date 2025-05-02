package com.fast.mentor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SkillsAdapter extends RecyclerView.Adapter<SkillsAdapter.ViewHolder> {

    private List<String> skills;

    public SkillsAdapter(List<String> skills) {
        this.skills = skills;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_skill, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvSkill.setText(skills.get(position));
    }

    @Override
    public int getItemCount() { return skills.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSkill;

        ViewHolder(View view) {
            super(view);
            tvSkill = view.findViewById(R.id.tvSkill);
        }
    }
}
