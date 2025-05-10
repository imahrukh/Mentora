package com.fast.mentor;

/**
 * Represents a search result with the type of content, item ID, and display information
 */
public class SearchResult {
    public enum ResultType {
        COURSE,
        MODULE,
        LESSON,
        RESOURCE
    }

    private String id;
    private String title;
    private String description;
    private ResultType type;
    private String imageUrl;
    private String authorName;
    private String category;
    private float rating;
    private int lessonsCount;
    private int durationMinutes;

    public SearchResult() {
        // Empty constructor needed for Firestore
    }

    public SearchResult(String id, String title, String description, ResultType type, 
                       String imageUrl, String authorName, String category, 
                       float rating, int lessonsCount, int durationMinutes) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
        this.imageUrl = imageUrl;
        this.authorName = authorName;
        this.category = category;
        this.rating = rating;
        this.lessonsCount = lessonsCount;
        this.durationMinutes = durationMinutes;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ResultType getType() {
        return type;
    }

    public void setType(ResultType type) {
        this.type = type;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public int getLessonsCount() {
        return lessonsCount;
    }

    public void setLessonsCount(int lessonsCount) {
        this.lessonsCount = lessonsCount;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
}