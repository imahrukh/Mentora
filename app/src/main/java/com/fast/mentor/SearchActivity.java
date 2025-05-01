package com.fast.mentor;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private SearchView searchView;
    private FlexboxLayout recentSearchesContainer, popularSearchesContainer;
    private Button btnClear;
    private DatabaseHelper dbHelper; // Your SQLiteOpenHelper

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        dbHelper = new DatabaseHelper(this);

        searchView = findViewById(R.id.searchView);
        recentSearchesContainer = findViewById(R.id.recentSearchesContainer);
        popularSearchesContainer = findViewById(R.id.popularSearchesContainer);
        btnClear = findViewById(R.id.btnClear);

        loadRecentSearches();
        loadPopularSearches();

        btnClear.setOnClickListener(v -> {
            dbHelper.clearRecentSearches();
            loadRecentSearches();
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.trim().isEmpty()) {
                    dbHelper.insertSearch(query);
                    loadRecentSearches();
                    Toast.makeText(SearchActivity.this, "Searched: " + query, Toast.LENGTH_SHORT).show();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        setupBottomNavigation();
    }

    private void loadRecentSearches() {
        recentSearchesContainer.removeAllViews();
        List<String> recentSearches = dbHelper.getRecentSearches();
        for (String term : recentSearches) {
            TextView chip = makeSearchChip(term);
            recentSearchesContainer.addView(chip);
        }
    }

    private void loadPopularSearches() {
        String[] popular = {"Machine Learning", "Kotlin", "Deep Learning", "AI Ethics"};
        for (String term : popular) {
            TextView chip = makeSearchChip(term);
            popularSearchesContainer.addView(chip);
        }
    }

    private TextView makeSearchChip(String text) {
        TextView chip = new TextView(this);
        chip.setText(text);
        chip.setPadding(30, 20, 30, 20);
        chip.setBackgroundResource(R.drawable.searchview_border);
        chip.setTextColor(getResources().getColor(R.color.white));

        FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(10, 10, 10, 10);
        chip.setLayoutParams(params);

        chip.setOnClickListener(v -> searchView.setQuery(text, true));

        return chip;
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_search);

        bottomNav.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_explore:
                    startActivity(new Intent(this, ExploreActivity.class));
                    return true;
                case R.id.nav_courses:
                    startActivity(new Intent(this, LearnActivity.class));
                    return true;
                case R.id.nav_profile:
                    startActivity(new Intent(this, ProfileActivity.class));
                    return true;
                case R.id.nav_search:
                    return true;
            }
            return false;
        });
    }
}
