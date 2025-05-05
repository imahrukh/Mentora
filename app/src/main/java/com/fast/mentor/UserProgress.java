package com.fast.mentor;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UserProgress {
    private Map<String, ModulePerformance> modulePerformance;
    private String learningStyle; // Visual/Auditory/Kinesthetic
    private double performanceScore;
    private String focusArea;
    private Date lastUpdated;
    public UserProgress() {
        this.modulePerformance = new HashMap<>();
        this.learningStyle = "";
        this.performanceScore = 0.0;
        this.focusArea = "";
        this.lastUpdated = new Date();
    }
    public UserProgress(Map<String, ModulePerformance> modulePerformance, String learningStyle, double performanceScore, String focusArea, Date lastUpdated) {
        this.modulePerformance = modulePerformance;
        this.learningStyle = learningStyle;
        this.performanceScore = performanceScore;
        this.focusArea = focusArea;
        this.lastUpdated = lastUpdated;
    }
    public Map<String, ModulePerformance> getModulePerformance() {
        return modulePerformance;
    }
    public void setModulePerformance(Map<String, ModulePerformance> modulePerformance) {
        this.modulePerformance = modulePerformance;
    }
    public String getLearningStyle() {
        return learningStyle;
    }
    public void setLearningStyle(String learningStyle){
        this.learningStyle=learningStyle;
    }
    public void setPerformanceScore(double performanceScore) {
        this.performanceScore = performanceScore;
    }
    public double getPerformanceScore() {
        return performanceScore;
    }
    public String getFocusArea() {
        return focusArea;
    }
    public void setFocusArea(String focusArea) {
        this.focusArea=focusArea;
    }
    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    public Date getLastUpdated() {
        return lastUpdated;
    }

    public static void updateProgress(String userId, String courseId, String itemId, boolean completed) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("progress." + itemId + ".completed", completed);
        updates.put("progress." + itemId + ".timestamp", FieldValue.serverTimestamp());

        FirebaseFirestore.getInstance()
                .collection("users").document(userId)
                .collection("enrolledCourses").document(courseId)
                .update(updates);
    }
}
