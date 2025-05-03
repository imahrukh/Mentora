package com.fast.mentor;

public class Recommendation {
    private String title;
    private String reason;
    private String type; // Adaptive/NextStep
    private long recommendedDuration; // minutes
    public Recommendation(String title, String reason, String type, long recommendedDuration) {
        this.title = title;
        this.reason = reason;
        this.type = type;
        this.recommendedDuration = recommendedDuration;
    }
    public String getTitle() {
        return title;
    }
    public String getReason() {
        return reason;
    }
    public String getType() {
        return type;
    }
    public long getRecommendedDuration() {
        return recommendedDuration;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setReason(String reason) {
        this.reason = reason;
    }
    public void setType(String type) {
        this.type = type;
    }
    public void setRecommendedDuration(long recommendedDuration) {
        this.recommendedDuration = recommendedDuration;

    }
}