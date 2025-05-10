package com.fast.mentor;

/**
 * Stores user learning metrics for the recommendation engine
 */
public class UserMetrics {
    private int id;
    private int userId;
    private int averageCompletionRate; // percentage of expected time
    private String preferredContentType; // based on usage patterns
    private int preferredDifficulty; // 1-5 scale
    private String learningPace; // "fast", "medium", "slow"
    private long lastUpdated; // timestamp of last update

    public UserMetrics() {
        // Default constructor with reasonable defaults
        this.averageCompletionRate = 100; // default to expected time
        this.preferredDifficulty = 2; // medium
        this.learningPace = "medium";
        this.preferredContentType = "video"; // most common type
        this.lastUpdated = System.currentTimeMillis();
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getAverageCompletionRate() {
        return averageCompletionRate;
    }

    public void setAverageCompletionRate(int averageCompletionRate) {
        this.averageCompletionRate = averageCompletionRate;
    }

    public String getPreferredContentType() {
        return preferredContentType;
    }

    public void setPreferredContentType(String preferredContentType) {
        this.preferredContentType = preferredContentType;
    }

    public int getPreferredDifficulty() {
        return preferredDifficulty;
    }

    public void setPreferredDifficulty(int preferredDifficulty) {
        this.preferredDifficulty = preferredDifficulty;
    }

    public String getLearningPace() {
        return learningPace;
    }

    public void setLearningPace(String learningPace) {
        this.learningPace = learningPace;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    /**
     * Update metrics based on a new content completion
     * @param completionRate the completion rate (percentage of expected time)
     * @param contentType the type of content completed
     * @param contentDifficulty the difficulty level of the completed content
     */
    public void updateMetrics(int completionRate, String contentType, int contentDifficulty) {
        // Update completion rate using moving average
        this.averageCompletionRate = (this.averageCompletionRate + completionRate) / 2;
        
        // Update preferred content type if different from current
        if (!this.preferredContentType.equals(contentType)) {
            // Simple logic: only change preferred type if encountered multiple times
            // More sophisticated approach would track frequency of each type
            this.preferredContentType = contentType;
        }
        
        // Update difficulty preference based on completion rate
        if (completionRate < 70) {  // Completed faster than expected
            // User might prefer more difficult content
            this.preferredDifficulty = Math.min(5, this.preferredDifficulty + 1);
            this.learningPace = "fast";
        } else if (completionRate > 130) {  // Took longer than expected
            // User might prefer easier content
            this.preferredDifficulty = Math.max(1, this.preferredDifficulty - 1);
            this.learningPace = "slow";
        } else {
            this.learningPace = "medium";
        }
        
        this.lastUpdated = System.currentTimeMillis();
    }
}