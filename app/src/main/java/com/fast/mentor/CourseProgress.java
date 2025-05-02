package com.fast.mentor;

import java.util.List;
import java.util.Map;

public class CourseProgress {
    private String courseId;
    private String userId;
    private List<WeekProgress> weeks;
    private Map<String, ModuleProgress> moduleProgress; // Key: "week_module_item"
    private float grade;

    public CourseProgress(String courseId, String userId, List<WeekProgress> weeks, Map<String, ModuleProgress> moduleProgress, float grade) {
        this.courseId = courseId;
        this.userId = userId;
        this.weeks = weeks;
        this.moduleProgress = moduleProgress;
        this.grade = grade;
    }
    public Map<String, ModuleProgress> getModuleProgressMap() {
        return moduleProgress;
    }
    public float getGrade() {
        return grade;
    }
    public List<WeekProgress> getWeeks() {
        return weeks;
    }
    public String getCourseId() {
        return courseId;
    }
    public String getUserId() {
        return userId;
    }

}


