package com.fast.mentor;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class representing a user's profile in the Mentora app.
 */
public class UserProfile {
    private int id;
    private String fullName;
    private String username;
    private String email;
    private String bio;
    private String profilePictureUrl;
    private String preferredLanguage;
    private boolean emailNotifications;
    private boolean pushNotifications;
    private List<String> interests;
    private int coursesCompleted;
    private int hoursSpent;
    private int certificatesEarned;

    /**
     * Default constructor
     */
    public UserProfile() {
        this.interests = new ArrayList<>();
        this.emailNotifications = true;
        this.pushNotifications = true;
        this.preferredLanguage = "English";
    }

    /**
     * Constructor with essential fields
     */
    public UserProfile(int id, String fullName, String username, String email) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.email = email;
        this.interests = new ArrayList<>();
        this.emailNotifications = true;
        this.pushNotifications = true;
        this.preferredLanguage = "English";
    }

    /**
     * Full constructor with all fields
     */
    public UserProfile(int id, String fullName, String username, String email, String bio,
                       String profilePictureUrl, String preferredLanguage,
                       boolean emailNotifications, boolean pushNotifications,
                       List<String> interests, int coursesCompleted, int hoursSpent,
                       int certificatesEarned) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.email = email;
        this.bio = bio;
        this.profilePictureUrl = profilePictureUrl;
        this.preferredLanguage = preferredLanguage;
        this.emailNotifications = emailNotifications;
        this.pushNotifications = pushNotifications;
        this.interests = interests != null ? interests : new ArrayList<>();
        this.coursesCompleted = coursesCompleted;
        this.hoursSpent = hoursSpent;
        this.certificatesEarned = certificatesEarned;
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public boolean isEmailNotifications() {
        return emailNotifications;
    }

    public void setEmailNotifications(boolean emailNotifications) {
        this.emailNotifications = emailNotifications;
    }

    public boolean isPushNotifications() {
        return pushNotifications;
    }

    public void setPushNotifications(boolean pushNotifications) {
        this.pushNotifications = pushNotifications;
    }

    public List<String> getInterests() {
        return interests;
    }

    public void setInterests(List<String> interests) {
        this.interests = interests;
    }

    public void addInterest(String interest) {
        if (this.interests == null) {
            this.interests = new ArrayList<>();
        }
        if (!this.interests.contains(interest)) {
            this.interests.add(interest);
        }
    }

    public void removeInterest(String interest) {
        if (this.interests != null) {
            this.interests.remove(interest);
        }
    }

    public boolean hasInterest(String interest) {
        return this.interests != null && this.interests.contains(interest);
    }

    public int getCoursesCompleted() {
        return coursesCompleted;
    }

    public void setCoursesCompleted(int coursesCompleted) {
        this.coursesCompleted = coursesCompleted;
    }

    public int getHoursSpent() {
        return hoursSpent;
    }

    public void setHoursSpent(int hoursSpent) {
        this.hoursSpent = hoursSpent;
    }

    public int getCertificatesEarned() {
        return certificatesEarned;
    }

    public void setCertificatesEarned(int certificatesEarned) {
        this.certificatesEarned = certificatesEarned;
    }

    @Override
    public String toString() {
        return "UserProfile{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", bio='" + bio + '\'' +
                ", preferredLanguage='" + preferredLanguage + '\'' +
                ", interests=" + interests +
                ", coursesCompleted=" + coursesCompleted +
                ", hoursSpent=" + hoursSpent +
                ", certificatesEarned=" + certificatesEarned +
                '}';
    }
}