package com.fast.mentor;

import androidx.annotation.Keep;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

/**
 * Model class for quizzes.
 */
@Keep
@IgnoreExtraProperties
public class Quiz {
    
    private String id;
    private String lessonId;
    private String title;
    private String description;
    private int questionCount;
    private int timeLimit; // in minutes
    private int passingScore; // percentage
    private boolean isRequired;
    private int maxAttempts;
    private @ServerTimestamp Date createdAt;

    /**
     * Default constructor required for Firestore
     */
    public Quiz() {
        // Required empty constructor
    }

    /**
     * Constructor with basic fields
     */
    public Quiz(String id, String lessonId, String title, String description) {
        this.id = id;
        this.lessonId = lessonId;
        this.title = title;
        this.description = description;
    }

    // Getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLessonId() {
        return lessonId;
    }

    public void setLessonId(String lessonId) {
        this.lessonId = lessonId;
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

    public int getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(int questionCount) {
        this.questionCount = questionCount;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public int getPassingScore() {
        return passingScore;
    }

    public void setPassingScore(int passingScore) {
        this.passingScore = passingScore;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public void setRequired(boolean required) {
        isRequired = required;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}