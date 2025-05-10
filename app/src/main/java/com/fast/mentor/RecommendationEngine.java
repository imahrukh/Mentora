package com.fast.mentor;

import android.content.Context;
import android.util.Log;

import com.fast.mentor.database.DatabaseHelper;
import com.fast.mentor.model.Course;
import com.fast.mentor.model.UserMetrics;
import com.fast.mentor.model.Recommendation;

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
        this.dbHelper = DatabaseHelper.getInstance(context);
    }
    
    public static synchronized RecommendationEngine getInstance(Context context) {
        if (instance == null) {
            instance = new RecommendationEngine(context.getApplicationContext());
        }
        return instance;
    }
    
    /**
     * Generate recommendations for a user based on their metrics
     * @param userId the user ID
     * @return list of recommended courses
     */
    public List<Recommendation> generateRecommendations(int userId) {
        Log.d(TAG, "Generating recommendations for user: " + userId);
        
        UserMetrics metrics = dbHelper.getUserMetrics(userId);
        List<Course> allCourses = dbHelper.getAllCourses();
        List<Integer> enrolledCourseIds = dbHelper.getEnrolledCourseIds(userId);
        List<Recommendation> recommendations = new ArrayList<>();
        
        // Filter out courses the user is already enrolled in
        List<Course> availableCourses = new ArrayList<>();
        for (Course course : allCourses) {
            if (!enrolledCourseIds.contains(course.getId())) {
                availableCourses.add(course);
            }
        }
        
        if (metrics == null) {
            // No metrics available, recommend popular courses
            Log.d(TAG, "No user metrics available, recommending popular courses");
            for (Course course : availableCourses) {
                if (course.isPopular()) {
                    Recommendation rec = new Recommendation();
                    rec.setUserId(userId);
                    rec.setCourseId(course.getId());
                    rec.setReason("popular");
                    rec.setStrength(3); // medium strength
                    rec.setTimestamp(System.currentTimeMillis());
                    recommendations.add(rec);
                }
            }
        } else {
            // Generate personalized recommendations based on metrics
            Log.d(TAG, "Generating personalized recommendations based on user metrics");
            
            // Recommend based on preferred difficulty
            int preferredDifficulty = metrics.getPreferredDifficulty();
            Log.d(TAG, "User preferred difficulty: " + preferredDifficulty);
            
            // Recommend based on learning pace
            String learningPace = metrics.getLearningPace();
            Log.d(TAG, "User learning pace: " + learningPace);
            
            for (Course course : availableCourses) {
                int matchScore = 0;
                String reason = "";
                
                // Check difficulty match
                if (course.getDifficulty() == preferredDifficulty) {
                    matchScore += 2;
                    reason = "difficulty_match";
                } else if (Math.abs(course.getDifficulty() - preferredDifficulty) == 1) {
                    // Close difficulty match
                    matchScore += 1;
                }
                
                // Check if course matches learning pace
                if ("fast".equals(learningPace) && course.getDifficulty() > preferredDifficulty) {
                    // Fast learners get recommended slightly more challenging courses
                    matchScore += 1;
                    reason = "pace_match_advanced";
                } else if ("slow".equals(learningPace) && course.getDifficulty() < preferredDifficulty) {
                    // Slower learners get recommended slightly easier courses
                    matchScore += 1;
                    reason = "pace_match_easier";
                }
                
                // Add recommendation if there's a reasonable match
                if (matchScore > 0) {
                    Recommendation rec = new Recommendation();
                    rec.setUserId(userId);
                    rec.setCourseId(course.getId());
                    rec.setReason(reason);
                    rec.setStrength(matchScore);
                    rec.setTimestamp(System.currentTimeMillis());
                    recommendations.add(rec);
                    
                    Log.d(TAG, "Added recommendation: " + course.getTitle() + 
                            " with match score: " + matchScore);
                }
            }
        }
        
        // Sort recommendations by strength (highest first)
        Collections.sort(recommendations, (r1, r2) -> 
                Integer.compare(r2.getStrength(), r1.getStrength()));
        
        // Save recommendations to database
        for (Recommendation rec : recommendations) {
            dbHelper.saveRecommendation(rec);
        }
        
        return recommendations;
    }
    
    /**
     * Get stored recommendations for a user
     * @param userId the user ID
     * @return list of course recommendations
     */
    public List<Recommendation> getUserRecommendations(int userId) {
        return dbHelper.getUserRecommendations(userId);
    }
    
    /**
     * Track when a user views or clicks on a recommendation
     * @param recommendationId the recommendation ID
     * @param action "view" or "click"
     */
    public void trackRecommendationAction(int recommendationId, String action) {
        Recommendation rec = dbHelper.getRecommendation(recommendationId);
        if (rec != null) {
            if ("view".equals(action)) {
                rec.setViewed(true);
            } else if ("click".equals(action)) {
                rec.setClicked(true);
            }
            dbHelper.updateRecommendation(rec);
        }
    }
}