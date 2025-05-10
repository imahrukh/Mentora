package com.fast.mentor;

import androidx.annotation.Keep;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Model class for quiz questions.
 */
@Keep
@IgnoreExtraProperties
public class QuizQuestion {
    
    private String id;
    private String quizId;
    private String text;
    private String imageUrl;
    private List<String> options = new ArrayList<>();
    private String correctAnswer;
    private String feedback;
    private int order;
    private int pointValue = 1;
    private @ServerTimestamp Date createdAt;

    /**
     * Default constructor required for Firestore
     */
    public QuizQuestion() {
        // Required empty constructor
    }

    /**
     * Constructor with basic fields
     */
    public QuizQuestion(String id, String quizId, String text, List<String> options, String correctAnswer) {
        this.id = id;
        this.quizId = quizId;
        this.text = text;
        this.options = options != null ? options : new ArrayList<>();
        this.correctAnswer = correctAnswer;
    }

    // Getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQuizId() {
        return quizId;
    }

    public void setQuizId(String quizId) {
        this.quizId = quizId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options != null ? options : new ArrayList<>();
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getPointValue() {
        return pointValue;
    }

    public void setPointValue(int pointValue) {
        this.pointValue = pointValue;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}