package com.fast.mentor;

import androidx.annotation.Keep;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

/**
 * Model class for assignments.
 */
@Keep
@IgnoreExtraProperties
public class Assignment {
    
    private String id;
    private String lessonId;
    private String title;
    private String description;
    private String instructions;
    private int points;
    private int passingGrade;
    private Date deadline;
    private boolean isRequired;
    private @ServerTimestamp Date createdAt;

    /**
     * Default constructor required for Firestore
     */
    public Assignment() {
        // Required empty constructor
    }

    /**
     * Constructor with basic fields
     */
    public Assignment(String id, String lessonId, String title, String description) {
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

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getPassingGrade() {
        return passingGrade;
    }

    public void setPassingGrade(int passingGrade) {
        this.passingGrade = passingGrade;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public void setRequired(boolean required) {
        isRequired = required;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}