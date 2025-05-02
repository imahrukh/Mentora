package com.fast.mentor;

import java.util.List;

public class Week {
    private String weekNumber;
    private List<Module> modules;
    Week(String weekNumber, List<Module> modules) {
        this.weekNumber = weekNumber;
        this.modules = modules;
    }
    public String getWeekNumber() {
        return weekNumber;
    }
    public List<Module> getModules() {
        return modules;
    }


}