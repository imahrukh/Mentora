package com.fast.mentor;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Represents a learning course.
 */
public class Course {
    private int durationHours;
    private String instructorAffiliation;
    private  String instructorName;
    private String imageUrl;
    private String author;
    private String difficulty;
    private String language;
    private String requirements;
    private int enrolledCount;
    private String courseId;
    private String title;
    private String description;
    private String category;
    private String thumbnailUrl;
    private int durationInHours;        // total hours
    private String level;               // "Beginner", "Intermediate", "Advanced"
    private boolean isPopular;
    private boolean isNew;
    private double rating;
    private int reviewsCount;
    private double price;               // newly added
    private List<String> whatYouWillLearn;
    private List<Map<String, Object>> instructors;
    private List<Week> weeks;           // Week → Module → Lesson tree
    private String createdAt;
    private String updatedAt;
    private int lessonsCount;


    // Empty constructor required for Firestore
    public Course() {}
    public Course(String courseId, String title, String instructorName, String instructorAffiliation,
                  float rating, int reviewsCount, String level, int durationHours, int lessonsCount,
                  float price, boolean isPopular, String thumbnailUrl) {
        this.courseId = courseId;
        this.title = title;
        this.instructorName = instructorName;
        this.instructorAffiliation = instructorAffiliation;
        this.rating = rating;
        this.reviewsCount = reviewsCount;
        this.level = level;
        this.durationHours = durationHours;
        this.lessonsCount = lessonsCount;
        this.price = price;
        this.isPopular = isPopular;
        this.thumbnailUrl = thumbnailUrl;
    }


    /**
     * Returns count of all lessons across weeks→modules
     */
    public int getLessonsCount() {
        if (weeks == null) return 0;
        int total = 0;
        for (Week week : weeks) {
            if (week.getModules() != null) {
                for (Module module : week.getModules()) {
                    if (module.getItems() != null) {
                        total += module.getItems().size();
                    }
                }
            }
        }
        return total;
    }

    // --- New helper methods for adapter ---

    /** e.g. "5h" or "1h" */
    public String getFormattedDuration() {
        return durationInHours + "h";
    }

    /** e.g. "12 lessons" or "1 lesson" */
    public String getFormattedLessons() {
        int count = getLessonsCount();
        return count + (count == 1 ? " lesson" : " lessons");
    }

    /** e.g. "$19.99" or "Free" */
    public String getFormattedPrice() {
        if (price <= 0) {
            return "Free";
        }
        NumberFormat fmt = NumberFormat.getCurrencyInstance(Locale.getDefault());
        return fmt.format(price);
    }

    /** e.g. "4.5 (200)" */
    public String getFormattedRating() {
        return String.format(Locale.getDefault(), "%.1f (%d)", rating, reviewsCount);
    }

    /**
     * Returns a display string for instructor(s), e.g. "Jane Doe, John Smith"
     */
    public String getFullInstructorInfo() {
        if (instructors == null || instructors.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> inst : instructors) {
            Object name = inst.get("name");
            if (name != null) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(name.toString());
            }
        }
        return sb.toString();
    }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public int getDurationInHours() { return durationInHours; }
    public void setDurationInHours(int durationInHours) { this.durationInHours = durationInHours; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public boolean isPopular() { return isPopular; }
    public void setPopular(boolean popular) { isPopular = popular; }

    public boolean isNew() { return isNew; }
    public void setNew(boolean aNew) { isNew = aNew; }

    public float getRating() { return (float) rating; }
    public void setRating(double rating) { this.rating = rating; }

    public int getReviewsCount() { return reviewsCount; }
    public void setReviewsCount(int reviewsCount) { this.reviewsCount = reviewsCount; }

    // Price field
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public List<String> getWhatYouWillLearn() { return whatYouWillLearn; }
    public void setWhatYouWillLearn(List<String> whatYouWillLearn) { this.whatYouWillLearn = whatYouWillLearn; }

    public List<Map<String,Object>> getInstructors() { return instructors; }
    public void setInstructors(List<Map<String,Object>> instructors) { this.instructors = instructors; }

    public List<Week> getWeeks() { return weeks; }
    public void setWeeks(List<Week> weeks) { this.weeks = weeks; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    /** @return total duration in minutes */
    public int getDuration() { return durationInHours * 60; }

    public boolean isFree() {
        return price == 0;
    }
    public String getDifficulty() {
        return difficulty;
    }

    public String getAuthor() {
        return author;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getEnrolledCount() {
        return enrolledCount;
    }

    public String getLanguage() {
        return language;
    }

    public String getRequirements() {
        return requirements;
    }

    public Module[] getModules() {
        if (weeks == null) return new Module[0];
        else {
            return weeks.stream()
                    .flatMap(week -> week.getModules().stream())
                    .toArray(Module[]::new);
        }

    }

    public void setInstructorName(String s) {
        if (instructors == null) instructors = new ArrayList<>();
        instructors.add(Map.of("name", s));
    }

    public void setInstructorAffiliation(String googleDeveloperExpert) {
        if (instructors == null) instructors = new ArrayList<>();
        instructors.add(Map.of("affiliation", googleDeveloperExpert));
    }

    public void setDurationHours(int i) {
        this.durationInHours = i;
    }

    public void setLessonsCount(int i) {
        this.lessonsCount = i;

    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty == 1 ? "Beginner" : difficulty == 2 ? "Intermediate" : "Advanced";
    }
    public int getDifficultyLevel() {
        return difficulty.equals("Beginner") ? 1 : difficulty.equals("Intermediate") ? 2 : 3;
    }
}
