package com.fast.mentor;

public class GradeItem {
    private String itemId;
    private String title;
    private String type;
    private float weight;
    private boolean completed;

    // Constructor, getters, setters
    public GradeItem() {}

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setType(String type) {
        this.type = type;
    }
    public void setWeight(float weight) {
        this.weight = weight;
    }
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
    public GradeItem(String itemId, String title, String type, float weight, boolean completed) {
        this.itemId = itemId;
        this.title = title;
        this.type = type;
        this.weight = weight;
        this.completed = completed;
    }
    public String getItemId() {
        return itemId;
    }
    public String getTitle() {
        return title;
    }
    public String getType() {
        return type;
    }
    public float getWeight() {
        return weight;
    }
    public boolean isCompleted() {
        return completed;
    }
}
