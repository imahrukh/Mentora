package com.fast.mentor;

import java.util.List;

public class Module {
    private String moduleId;
    private String title;
    private List<ModuleItem> items;

    Module(String moduleId, String title, List<ModuleItem> items) {
        this.moduleId = moduleId;
        this.title = title;
        this.items = items;

    }
    public String getModuleId() {
        return moduleId;
    }
    public String getTitle() {
        return title;
    }
    public List<ModuleItem> getItems() {
        return items;
    }
    public void setItems(List<ModuleItem> items) {
        this.items = items;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }
}

