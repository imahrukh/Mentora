package com.fast.mentor;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a lesson within a module.
 */
public class Lesson implements Parcelable {

    public enum LessonType {
        VIDEO,
        DOCUMENT,
        QUIZ,
        ASSIGNMENT
    }

    @DocumentId
    private String id;
    private String moduleId;
    private String title;
    private String description;
    private LessonType type;
    private String contentUrl;
    private String duration;
    private int order;
    private long createdAt;
    private long updatedAt;

    // Fields for progress
    private boolean completed;
    private int progress; // 0-100 percent
    private long lastAccessed;
    private int lastPosition; // For videos, tracking playback position in seconds

    // Fields for resources
    @Exclude
    private List<Resource> resources;

    /**
     * Default constructor for Firebase
     */
    public Lesson() {
        // Required for Firebase
        resources = new ArrayList<>();
    }

    /**
     * Full constructor
     */
    public Lesson(String id, String moduleId, String title, String description, LessonType type,
                  String contentUrl, String duration, int order, long createdAt, long updatedAt) {
        this.id = id;
        this.moduleId = moduleId;
        this.title = title;
        this.description = description;
        this.type = type;
        this.contentUrl = contentUrl;
        this.duration = duration;
        this.order = order;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.completed = false;
        this.progress = 0;
        this.lastAccessed = 0;
        this.lastPosition = 0;
        this.resources = new ArrayList<>();
    }

    /**
     * Check if the lesson has been started but not completed
     * @return true if lesson is in progress
     */
    @Exclude
    public boolean isInProgress() {
        return progress > 0 && progress < 100;
    }

    /**
     * Get the video URL for YouTube videos
     * @return YouTube video URL if type is VIDEO, null otherwise
     */
    @Exclude
    public String getVideoUrl() {
        if (type == LessonType.VIDEO) {
            return contentUrl;
        }
        return null;
    }

    /**
     * Get the document URL for PDF documents
     * @return PDF document URL if type is DOCUMENT, null otherwise
     */
    @Exclude
    public String getDocumentUrl() {
        if (type == LessonType.DOCUMENT) {
            return contentUrl;
        }
        return null;
    }

    /**
     * Check if this lesson is a video
     * @return true if this lesson is a video
     */
    @Exclude
    public boolean isVideo() {
        return type == LessonType.VIDEO;
    }

    /**
     * Check if this lesson is a document (PDF)
     * @return true if this lesson is a document
     */
    @Exclude
    public boolean isDocument() {
        return type == LessonType.DOCUMENT;
    }

    /**
     * Check if this lesson is a quiz
     * @return true if this lesson is a quiz
     */
    @Exclude
    public boolean isQuiz() {
        return type == LessonType.QUIZ;
    }

    /**
     * Check if this lesson is an assignment
     * @return true if this lesson is an assignment
     */
    @Exclude
    public boolean isAssignment() {
        return type == LessonType.ASSIGNMENT;
    }

    /**
     * Add a resource to this lesson
     * @param resource The resource to add
     */
    public void addResource(Resource resource) {
        if (resources == null) {
            resources = new ArrayList<>();
        }
        resources.add(resource);
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
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

    public LessonType getType() {
        return type;
    }

    public void setType(LessonType type) {
        this.type = type;
    }

    public String getContentUrl() {
        return contentUrl;
    }

    public void setContentUrl(String contentUrl) {
        this.contentUrl = contentUrl;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
        if (completed) {
            this.progress = 100;
        }
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
        this.completed = (progress >= 100);
    }

    public long getLastAccessed() {
        return lastAccessed;
    }

    public void setLastAccessed(long lastAccessed) {
        this.lastAccessed = lastAccessed;
    }

    public int getLastPosition() {
        return lastPosition;
    }

    public void setLastPosition(int lastPosition) {
        this.lastPosition = lastPosition;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }

    // Parcelable implementation

    protected Lesson(Parcel in) {
        id = in.readString();
        moduleId = in.readString();
        title = in.readString();
        description = in.readString();
        try {
            type = LessonType.valueOf(in.readString());
        } catch (IllegalArgumentException e) {
            type = LessonType.VIDEO; // Default
        }
        contentUrl = in.readString();
        duration = in.readString();
        order = in.readInt();
        createdAt = in.readLong();
        updatedAt = in.readLong();
        completed = in.readByte() != 0;
        progress = in.readInt();
        lastAccessed = in.readLong();
        lastPosition = in.readInt();
        resources = new ArrayList<>();
        in.readList(resources, Resource.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(moduleId);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(type != null ? type.name() : LessonType.VIDEO.name());
        dest.writeString(contentUrl);
        dest.writeString(duration);
        dest.writeInt(order);
        dest.writeLong(createdAt);
        dest.writeLong(updatedAt);
        dest.writeByte((byte) (completed ? 1 : 0));
        dest.writeInt(progress);
        dest.writeLong(lastAccessed);
        dest.writeInt(lastPosition);
        dest.writeList(resources);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Lesson> CREATOR = new Creator<Lesson>() {
        @Override
        public Lesson createFromParcel(Parcel in) {
            return new Lesson(in);
        }

        @Override
        public Lesson[] newArray(int size) {
            return new Lesson[size];
        }
    };
}