package com.fast.mentor;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fast.mentor.R;
import com.fast.mentor.SearchResult;
import com.fast.mentor.CourseDetailActivity;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {
    
    private final Context context;
    private List<SearchResult> searchResults;
    
    public SearchAdapter(Context context) {
        this.context = context;
        this.searchResults = new ArrayList<>();
    }
    
    public void setSearchResults(List<SearchResult> searchResults) {
        this.searchResults = searchResults;
        notifyDataSetChanged();
    }
    
    public void clearResults() {
        searchResults.clear();
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_search_result, parent, false);
        return new SearchViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        SearchResult result = searchResults.get(position);
        
        holder.titleTextView.setText(result.getTitle());
        holder.descriptionTextView.setText(result.getDescription());
        holder.authorTextView.setText(result.getAuthorName());
        holder.categoryTextView.setText(result.getCategory());
        
        // Display type badge
        String typeBadge = "";
        int badgeColor = 0;
        
        switch (result.getType()) {
            case COURSE:
                typeBadge = "Course";
                badgeColor = R.color.colorPrimary;
                break;
            case MODULE:
                typeBadge = "Module";
                badgeColor = R.color.colorSecondary;
                break;
            case LESSON:
                typeBadge = "Lesson";
                badgeColor = R.color.primaryColor;
                break;
            case RESOURCE:
                typeBadge = "Resource";
                badgeColor = R.color.colorSecondaryVariant;
                break;
        }
        
        holder.typeBadgeTextView.setText(typeBadge);
        holder.typeBadgeTextView.setBackgroundResource(badgeColor);
        
        // Format and set metadata
        String metadata = String.format("%d lessons • %d min", 
                result.getLessonsCount(), result.getDurationMinutes());
        holder.metadataTextView.setText(metadata);
        
        // Load image
        if (result.getImageUrl() != null && !result.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(result.getImageUrl())
                    .placeholder(R.drawable.placeholder_course)
                    .error(R.drawable.placeholder_course)
                    .into(holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.placeholder_course);
        }
        
        // Set rating
        holder.ratingTextView.setText(String.format("%.1f", result.getRating()));
        
        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (result.getType() == SearchResult.ResultType.COURSE) {
                // Navigate to course detail
                Intent intent = new Intent(context, CourseDetailActivity.class);
                intent.putExtra("courseId", result.getId());
                context.startActivity(intent);
            } else if (result.getType() == SearchResult.ResultType.MODULE) {
                // Get the parent course ID and navigate to course detail with module highlighted
                // Implementation depends on how modules are structured in your app
                Intent intent = new Intent(context, CourseDetailActivity.class);
                intent.putExtra("courseId", result.getId().split("_")[0]); // Assuming format "courseId_moduleId"
                intent.putExtra("moduleId", result.getId());
                context.startActivity(intent);
            } else if (result.getType() == SearchResult.ResultType.LESSON) {
                // Navigate directly to lesson
                // Implementation depends on how lessons are accessed in your app
                // e.g., LessonPlayerActivity.start(context, result.getId());
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return searchResults.size();
    }
    
    static class SearchViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleTextView;
        TextView descriptionTextView;
        TextView authorTextView;
        TextView categoryTextView;
        TextView typeBadgeTextView;
        TextView metadataTextView;
        TextView ratingTextView;
        
        public SearchViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_search_result);
            titleTextView = itemView.findViewById(R.id.text_search_title);
            descriptionTextView = itemView.findViewById(R.id.text_search_description);
            authorTextView = itemView.findViewById(R.id.text_search_author);
            categoryTextView = itemView.findViewById(R.id.text_search_category);
            typeBadgeTextView = itemView.findViewById(R.id.text_search_type_badge);
            metadataTextView = itemView.findViewById(R.id.text_search_metadata);
            ratingTextView = itemView.findViewById(R.id.text_search_rating);
        }
    }
}