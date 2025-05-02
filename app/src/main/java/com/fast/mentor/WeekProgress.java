package com.fast.mentor;

import java.util.List;

public class WeekProgress {
    private int weekNumber;
    private List<ModuleProgress> modules;

    WeekProgress(int weekNumber, List<ModuleProgress> modules) {
        this.weekNumber = weekNumber;
        this.modules = modules;
    }

    public int getWeekNumber() {
        return weekNumber;
    }

    public List<ModuleProgress> getModules() {
        return modules;
    }
}