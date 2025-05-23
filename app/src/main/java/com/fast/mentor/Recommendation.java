package com.fast.mentor;

/**
 * Course recommendation generated by the recommendation engine
 */
public class Recommendation {
    private int id;
    private int userId;
    private int courseId;
    private String reason; // why the course was recommended
    private int strength; // recommendation strength (1-5)
    private long timestamp; // when recommendation was generated
    private boolean viewed; // has user seen the recommendation
    private boolean clicked; // has user clicked on the recommendation
    
    // Associated course for UI display
    private Course course;

    public Recommendation() {
        // Default constructor
        this.timestamp = System.currentTimeMillis();
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

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isViewed() {
        return viewed;
    }

    public void setViewed(boolean viewed) {
        this.viewed = viewed;
    }

    public boolean isClicked() {
        return clicked;
    }

    public void setClicked(boolean clicked) {
        this.clicked = clicked;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }
    
    /**
     * Get user-friendly description of why this course was recommended
     * @return user-friendly reason text
     */
    public String getReasonText() {
        switch (reason) {
            case "popular":
                return "Popular with students like you";
            case "difficulty_match":
                return "This matches your preferred difficulty level";
            case "pace_match_advanced":
                return "Based on your fast learning pace, you might enjoy this more advanced course";
            case "pace_match_easier":
                return "This course aligns well with your learning style";
            case "topic_match":
                return "This topic seems to interest you based on your activity";
            case "next_step":
                return "This is a natural next step after your completed courses";
            default:
                return "Recommended for you";
        }
    }
}