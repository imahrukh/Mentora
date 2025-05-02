package com.fast.mentor;

import java.util.Date;

public class ModuleProgress {
    private String itemId;
    private boolean completed;
    private Date completedAt;

    // Constructor, getters & setters
    public ModuleProgress(String itemId, boolean completed, Date completedAt) {
        this.itemId = itemId;
        this.completed = completed;
        this.completedAt = completedAt;
    }
    public String getItemId() {
        return itemId;
    }
    public boolean isCompleted() {
        return completed;
    }
    public Date getCompletedAt() {
        return completedAt;
    }
}
