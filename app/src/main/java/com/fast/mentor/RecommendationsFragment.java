package com.fast.mentor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;
import java.util.stream.Collectors;

public class RecommendationsFragment extends Fragment {
    private RecyclerView rvRecommendations, rvNextSteps;
    private RecommendationAdapter adapter;
    private TextView tvPerformanceLevel, tvLearningStyle, tvFocusArea;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recommendations, container, false);

        // Initialize views
        tvPerformanceLevel = view.findViewById(R.id.tvPerformanceLevel);
        tvLearningStyle = view.findViewById(R.id.tvLearningStyle);
        tvFocusArea = view.findViewById(R.id.tvFocusArea);
        rvRecommendations = view.findViewById(R.id.rvRecommendations);
        rvNextSteps = view.findViewById(R.id.rvNextSteps);

        loadRecommendations();
        return view;
    }

    private void loadRecommendations() {
        String courseId = getArguments().getString("courseId");
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        new RecommendationEngine().getRecommendations(userId, courseId, recommendations -> {
            // Split into adaptive recommendations and next steps
            List<Recommendation> adaptiveRecs = recommendations.stream()
                    .filter(r -> r.getType().equals("Adaptive"))
                    .collect(Collectors.toList());

            List<Recommendation> nextSteps = recommendations.stream()
                    .filter(r -> r.getType().equals("NextStep"))
                    .collect(Collectors.toList());

            // Update UI
            tvPerformanceLevel.setText(calculatePerformanceLevel(adaptiveRecs));
            tvLearningStyle.setText("Learning Style: " + getLearningStyle());
            tvFocusArea.setText("Focus Area: " + identifyFocusArea(adaptiveRecs));

            rvRecommendations.setAdapter(new RecommendationAdapter(adaptiveRecs));
            rvNextSteps.setAdapter(new RecommendationAdapter(nextSteps));
        });
    }

    private String getLearningStyle() {
        return "Beginner";
    }

    private String calculatePerformanceLevel(List<Recommendation> recommendations) {
        // Implementation based on recommendation patterns
        return "Intermediate";
    }

    private String identifyFocusArea(List<Recommendation> recommendations) {
        // Implementation to find most recommended area
        return "Algorithm Optimization";
    }
}