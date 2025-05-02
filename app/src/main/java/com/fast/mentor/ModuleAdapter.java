package com.fast.mentor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class ModuleAdapter extends RecyclerView.Adapter<ModuleAdapter.ViewHolder> {
    private List<Module> modules;
    private Map<String, ModuleProgress> progressMap;

    public ModuleAdapter(List<Module> modules, Map<String, ModuleProgress> progressMap) {
        this.modules = modules;
        this.progressMap = progressMap;
    }

    public void updateModules(List<Module> selectedWeekModules) {
        modules = selectedWeekModules;
        notifyDataSetChanged();

    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvModuleTitle;
        RecyclerView rvModuleItems;

        ViewHolder(View view) {
            super(view);
            tvModuleTitle = view.findViewById(R.id.tvModuleTitle);
            rvModuleItems = view.findViewById(R.id.rvModuleItems);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_module, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Module module = modules.get(position);
        holder.tvModuleTitle.setText(module.getTitle());

        // Initialize nested RecyclerView
        ModuleItemAdapter itemAdapter = new ModuleItemAdapter(module.getItems(), progressMap);
        holder.rvModuleItems.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.rvModuleItems.setAdapter(itemAdapter);
    }

    @Override
    public int getItemCount() {
        return 0;
    }
}