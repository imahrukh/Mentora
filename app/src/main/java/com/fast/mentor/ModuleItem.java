package com.fast.mentor;

public class ModuleItem {
    private String itemId;
    private String title;
    private ItemType type; // LECTURE, QUIZ, ASSIGNMENT, LAB
    private boolean optional;
    private float weightage;
    private boolean completed;

    ModuleItem(String itemId, String title, ItemType type, boolean optional, float weightage, boolean completed){
        this.itemId = itemId;
        this.title = title;
        this.type = type;
        this.optional = optional;
        this.weightage = weightage;
    }

    public String getItemId() {
        return itemId;
    }
    public String getTitle() {
        return title;
    }
    public ItemType getType() {
        return type;
    }
    public boolean isOptional() {
        return optional;
    }
    public float getWeightage() {
        return weightage;
    }
    public boolean isCompleted() {
        return completed;
    }
}