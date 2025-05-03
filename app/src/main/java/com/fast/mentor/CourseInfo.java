package com.fast.mentor;

import java.util.List;

public class CourseInfo {
    private String title;
    private String description;
    private List<Instructor> instructors;

    public CourseInfo(String title, String description, List<Instructor> instructors) {
        this.title = title;
        this.description = description;
        this.instructors = instructors;
    }
    // Empty constructor for Firestore
    public CourseInfo() {}

    // Getters and setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setInstructors(List<Instructor> instructors) {
        this.instructors = instructors;
    }
    public String getDescription() {
        return description;
    }
    public List<Instructor> getInstructors() {
        return instructors;
    }
    public static class Instructor {
        private String name;
        private String role;
        private String affiliation;

        public Instructor() {}

        public Instructor(String name, String role, String affiliation) {
            this.name = name;
            this.role = role;
            this.affiliation = affiliation;
        }

        public void setAffiliation(String affiliation) {
            this.affiliation = affiliation;
        }
        public void setName(String name) {
            this.name = name;
        }
        public void setRole(String role) {
            this.role = role;

        }
        public String getAffiliation() {
            return affiliation;
        }
        public String getName() {
            return name;
        }
        public String getRole() {
            return role;
        }
    }
}