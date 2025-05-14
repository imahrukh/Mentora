package com.fast.mentor;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fast.mentor.R;
import com.fast.mentor.SearchResult;
import com.fast.mentor.SearchService;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

public class SearchFragment extends Fragment {

    private EditText searchEditText;
    private ImageView clearSearchButton;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyStateTextView;
    private ChipGroup filterChipGroup;
    private Chip filterAllChip;
    private Chip filterCoursesChip;
    private Chip filterModulesChip;
    private Chip filterLessonsChip;

    private SearchAdapter adapter;
    private SearchService searchService;
    
    // Tracks the current filter selected
    private SearchResult.ResultType currentFilter = null; // null means "All"
    
    // Debounce search to avoid too many queries
    private Runnable searchRunnable;
    private final long SEARCH_DEBOUNCE_TIME_MS = 500;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        
        // Initialize search service
        searchService = new SearchService();
        
        // Initialize views
        searchEditText = view.findViewById(R.id.edittext_search);
        clearSearchButton = view.findViewById(R.id.button_clear_search);
        recyclerView = view.findViewById(R.id.recyclerview_search_results);
        progressBar = view.findViewById(R.id.progressbar_search);
        emptyStateTextView = view.findViewById(R.id.textview_empty_search);
        filterChipGroup = view.findViewById(R.id.chipgroup_search_filters);
        filterAllChip = view.findViewById(R.id.chip_filter_all);
        filterCoursesChip = view.findViewById(R.id.chip_filter_courses);
        filterModulesChip = view.findViewById(R.id.chip_filter_modules);
        filterLessonsChip = view.findViewById(R.id.chip_filter_lessons);
        
        // Setup RecyclerView
        adapter = new SearchAdapter(requireContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        
        // Setup search functionality
        setupSearchListener();
        
        // Setup clear button
        clearSearchButton.setOnClickListener(v -> {
            searchEditText.setText("");
            adapter.clearResults();
            showEmptyState(true);
        });
        
        // Setup filter chips
        setupFilterChips();
        
        // Initial UI state
        showEmptyState(true);
        
        return view;
    }
    
    private void setupSearchListener() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not used
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Remove any pending search
                if (searchRunnable != null) {
                    searchEditText.removeCallbacks(searchRunnable);
                }
                
                String query = s.toString().trim();
                
                // Clear button visibility
                clearSearchButton.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
                
                if (query.isEmpty()) {
                    adapter.clearResults();
                    showEmptyState(true);
                    return;
                }
                
                // Create new search with debounce
                searchRunnable = () -> performSearch(query);
                searchEditText.postDelayed(searchRunnable, SEARCH_DEBOUNCE_TIME_MS);
            }
        });
    }
    
    private void setupFilterChips() {
        filterChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chip_filter_all) {
                currentFilter = null;
            } else if (checkedId == R.id.chip_filter_courses) {
                currentFilter = SearchResult.ResultType.COURSE;
            } else if (checkedId == R.id.chip_filter_modules) {
                currentFilter = SearchResult.ResultType.MODULE;
            } else if (checkedId == R.id.chip_filter_lessons) {
                currentFilter = SearchResult.ResultType.LESSON;
            }
            
            // Re-run search with new filter if we have a query
            String query = searchEditText.getText().toString().trim();
            if (!query.isEmpty()) {
                performSearch(query);
            }
        });
    }
    
    private void performSearch(String query) {
        if (query.length() < 2) {
            return; // Don't search for very short queries
        }
        
        showLoading(true);
        
        // Use the SearchService to perform the search
        searchService.search(query, currentFilter, new SearchService.SearchCallback() {
            @Override
            public void onSearchResults(List<SearchResult> results) {
                updateSearchResults(results);
            }

            @Override
            public void onSearchError(Exception e) {
                showError(e.getMessage());
            }
        });
    }
    
    private void updateSearchResults(List<SearchResult> results) {
        if (getActivity() == null) return;
        
        getActivity().runOnUiThread(() -> {
            adapter.setSearchResults(results);
            showLoading(false);
            showEmptyState(results.isEmpty());
        });
    }
    
    private void showLoading(boolean isLoading) {
        if (getActivity() == null) return;
        
        getActivity().runOnUiThread(() -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            if (isLoading) {
                recyclerView.setVisibility(View.GONE);
                emptyStateTextView.setVisibility(View.GONE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
    }
    
    private void showEmptyState(boolean isEmpty) {
        if (getActivity() == null) return;
        
        getActivity().runOnUiThread(() -> {
            if (isEmpty) {
                String searchText = searchEditText.getText().toString().trim();
                if (searchText.isEmpty()) {
                    emptyStateTextView.setText(R.string.search_empty_initial);
                } else {
                    emptyStateTextView.setText(getString(R.string.search_empty_results, searchText));
                }
                emptyStateTextView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyStateTextView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
    }
    
    private void showError(String message) {
        if (getActivity() == null) return;
        
        getActivity().runOnUiThread(() -> {
            showLoading(false);
            Toast.makeText(requireContext(), 
                    getString(R.string.search_error), 
                    Toast.LENGTH_SHORT).show();
        });
    }
}