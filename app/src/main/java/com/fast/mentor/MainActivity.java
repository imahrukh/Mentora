package com.fast.mentor;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.fast.mentor.SearchFragment;
import com.fast.mentor.LearnFragment;
import com.fast.mentor.ProfileFragment;
import com.fast.mentor.ExploreFragment;
import com.fast.mentor.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize bottom navigation
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        // Set default selection
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_explore);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;

        int itemId = item.getItemId();
        if (itemId == R.id.nav_explore) {
            selectedFragment = new ExploreFragment();
        } else if (itemId == R.id.nav_learn) {
            selectedFragment = new LearnFragment();
        } else if (itemId == R.id.nav_search) {
            selectedFragment = new SearchFragment();
        } else if (itemId == R.id.nav_profile) {
            selectedFragment = new ProfileFragment();
        }

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();
            return true;
        }
        return false;
    }
}