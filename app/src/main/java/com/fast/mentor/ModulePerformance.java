package com.fast.mentor;

public class ModulePerformance {
    private long timeSpent; // milliseconds
    private int attempts;
    private double score;
    private String difficultyLevel; // Beginner/Intermediate/Advanced

    public ModulePerformance(long timeSpent, int attempts, double score, String difficultyLevel) {
        this.timeSpent = timeSpent;
        this.attempts = attempts;
        this.score = score;
        this.difficultyLevel = difficultyLevel;
    }
    public long getTimeSpent() {
        return timeSpent;
    }
    public int getAttempts() {
        return attempts;
    }
    public double getScore() {
        return score;
    }
    public String getDifficultyLevel() {
        return difficultyLevel;
    }
    public void setTimeSpent(long timeSpent) {
        this.timeSpent = timeSpent;
    }
    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }
    public void setScore(double score) {
        this.score = score;
    }
    public void setDifficultyLevel(String difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }
    public ModulePerformance(){
        this.timeSpent=0;
        this.attempts=0;
        this.score=0.0;
        this.difficultyLevel="";
    }
}
