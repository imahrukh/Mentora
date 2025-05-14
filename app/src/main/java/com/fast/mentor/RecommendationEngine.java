package com.fast.mentor;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides course recommendations based on user learning patterns
 */
public class RecommendationEngine {
    private static final String TAG = "RecommendationEngine";

    private static RecommendationEngine instance;
    private final DatabaseHelper dbHelper;

    private RecommendationEngine(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context.getApplicationContext());
    }

    public static synchronized RecommendationEngine getInstance(Context context) {
        if (instance == null) {
            instance = new RecommendationEngine(context);
        }
        return instance;
    }

    /**
     * Generate recommendations for a user based on their metrics
     * @param userId the user ID (String)
     * @return list of recommended courses
     */
    public List<Recommendation> generateRecommendations(String userId) {
        Log.d(TAG, "Generating recommendations for user: " + userId);

        // 1) fetch metrics
        UserMetrics metrics = dbHelper.getUserMetrics(Integer.parseInt(userId));

        // 2) all courses & those not yet enrolled
        List<Course> allCourses = dbHelper.getAllCourses();
        List<String> enrolledCourseIds = dbHelper.getEnrolledCourseIds(userId);

        List<Recommendation> recs = new ArrayList<>();
        List<Course> available = new ArrayList<>();
        for (Course c : allCourses) {
            if (!enrolledCourseIds.contains(c.getCourseId())) {
                available.add(c);
            }
        }

        if (metrics == null) {
            // no metrics → recommend popular
            Log.d(TAG, "No user metrics, recommending popular courses");
            for (Course c : available) {
                if (c.isPopular()) {
                    Recommendation r = new Recommendation();
                    r.setUserId(Integer.parseInt(userId));
                    r.setCourseId(Integer.parseInt(c.getCourseId()));
                    r.setReason("popular");
                    r.setStrength(3);
                    r.setTimestamp(System.currentTimeMillis());
                    recs.add(r);
                }
            }
        } else {
            // personalized
            Log.d(TAG, "Personalized recommendations based on metrics");
            int prefDiff = metrics.getPreferredDifficulty();
            String pace = metrics.getLearningPace();

            for (Course c : available) {
                int match = 0;
                String reason = "";

                // assume Course.getDifficulty() returns an int difficulty
                int courseDiff = Integer.parseInt(c.getDifficulty());

                if (courseDiff == prefDiff) {
                    match += 2;
                    reason = "difficulty_match";
                } else if (Math.abs(courseDiff - prefDiff) == 1) {
                    match += 1;
                }

                if ("fast".equals(pace) && courseDiff > prefDiff) {
                    match += 1;
                    reason = "pace_advanced";
                } else if ("slow".equals(pace) && courseDiff < prefDiff) {
                    match += 1;
                    reason = "pace_easier";
                }

                if (match > 0) {
                    Recommendation r = new Recommendation();
                    r.setUserId(Integer.parseInt(userId));
                    r.setCourseId(Integer.parseInt(c.getCourseId()));
                    r.setReason(reason);
                    r.setStrength(match);
                    r.setTimestamp(System.currentTimeMillis());
                    recs.add(r);
                    Log.d(TAG, "Rec: " + c.getTitle() + " score=" + match);
                }
            }
        }

        // sort & persist
        Collections.sort(recs, (a,b) -> Integer.compare(b.getStrength(), a.getStrength()));
        for (Recommendation r : recs) {
            dbHelper.saveRecommendation(r);
        }
        return recs;
    }

    /**
     * Retrieve stored recommendations for a user
     */
    public List<Recommendation> getUserRecommendations(String userId) {
        return dbHelper.getUserRecommendations(userId);
    }

    /**
     * Track view/click actions
     */
    public void trackRecommendationAction(int recommendationId, String action) {
        Recommendation r = dbHelper.getRecommendation(recommendationId);
        if (r == null) return;
        if ("view".equals(action)) {
            r.setViewed(true);
        } else if ("click".equals(action)) {
            r.setClicked(true);
        }
        dbHelper.updateRecommendation(r);
    }
}
