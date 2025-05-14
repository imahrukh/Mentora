package com.fast.mentor;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Keep;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.List;

public class Assignment implements Parcelable {

    @DocumentId
    private String id;
    private String lessonId;
    private String title;
    private String description;
    private String instructions;
    private int points;
    private int passingGrade;
    private Date deadline;
    private List<String> allowedFileTypes;
    private int maxFilesAllowed;

    @ServerTimestamp
    private Timestamp createdAt;

    // Required empty constructor for Firestore
    public Assignment() {
    }

    /**
     * Constructor with required fields
     */
    public Assignment(String id, String lessonId, String title, String description, int points) {
        this.id = id;
        this.lessonId = lessonId;
        this.title = title;
        this.description = description;
        this.points = points;
        this.passingGrade = (int) (points * 0.6); // Default passing grade is 60%
    }

    // Getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLessonId() {
        return lessonId;
    }

    public void setLessonId(String lessonId) {
        this.lessonId = lessonId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getPassingGrade() {
        return passingGrade;
    }

    public void setPassingGrade(int passingGrade) {
        this.passingGrade = passingGrade;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public List<String> getAllowedFileTypes() {
        return allowedFileTypes;
    }

    public void setAllowedFileTypes(List<String> allowedFileTypes) {
        this.allowedFileTypes = allowedFileTypes;
    }

    public int getMaxFilesAllowed() {
        return maxFilesAllowed;
    }

    public void setMaxFilesAllowed(int maxFilesAllowed) {
        this.maxFilesAllowed = maxFilesAllowed;
    }

    @PropertyName("createdAt")
    public Date getCreatedAt() {
        return createdAt != null ? createdAt.toDate() : null;
    }

    @PropertyName("createdAt")
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    // Parcelable implementation

    protected Assignment(Parcel in) {
        id = in.readString();
        lessonId = in.readString();
        title = in.readString();
        description = in.readString();
        instructions = in.readString();
        points = in.readInt();
        passingGrade = in.readInt();
        long deadlineTime = in.readLong();
        deadline = deadlineTime != -1 ? new Date(deadlineTime) : null;
        allowedFileTypes = in.createStringArrayList();
        maxFilesAllowed = in.readInt();
        long createdAtTime = in.readLong();
        createdAt = createdAtTime != -1 ? new Timestamp(new Date(createdAtTime)) : null;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(lessonId);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(instructions);
        dest.writeInt(points);
        dest.writeInt(passingGrade);
        dest.writeLong(deadline != null ? deadline.getTime() : -1);
        dest.writeStringList(allowedFileTypes);
        dest.writeInt(maxFilesAllowed);
        dest.writeLong(createdAt != null ? createdAt.toDate().getTime() : -1);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Assignment> CREATOR = new Creator<Assignment>() {
        @Override
        public Assignment createFromParcel(Parcel in) {
            return new Assignment(in);
        }

        @Override
        public Assignment[] newArray(int size) {
            return new Assignment[size];
        }
    };
}