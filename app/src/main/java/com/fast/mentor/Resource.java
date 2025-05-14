package com.fast.mentor;

/**
 * Represents an additional resource attached to a lesson.
 */
public class Resource {
    public static final String TYPE_PDF   = "pdf";
    public static final String TYPE_VIDEO = "video";
    public static final String TYPE_AUDIO = "audio";
    public static final String TYPE_IMAGE = "image";
    public static final String TYPE_DOC   = "doc";
    public static final String TYPE_CODE  = "code";
    public static final String TYPE_LINK  = "link";

    private String id;
    private String lessonId;
    private String title;
    private String description;
    private String url;
    private String type; // e.g., TYPE_PDF, TYPE_IMAGE...
    private long size;   // file size in bytes
    private long createdAt;

    /** Default constructor for Firebase */
    public Resource() { }

    /** Full constructor */
    public Resource(String id,
                    String lessonId,
                    String title,
                    String description,
                    String url,
                    String type,
                    long size,
                    long createdAt) {
        this.id = id;
        this.lessonId = lessonId;
        this.title = title;
        this.description = description;
        this.url = url;
        this.type = type;
        this.size = size;
        this.createdAt = createdAt;
    }

    // --- Getters & Setters ---
    public String getId()              { return id; }
    public void   setId(String id)     { this.id = id; }

    public String getLessonId()                { return lessonId; }
    public void   setLessonId(String lessonId) { this.lessonId = lessonId; }

    public String getTitle()               { return title; }
    public void   setTitle(String title)   { this.title = title; }

    public String getDescription()                   { return description; }
    public void   setDescription(String description) { this.description = description; }

    public String getUrl()              { return url; }
    public void   setUrl(String url)    { this.url = url; }

    public String getType()               { return type; }
    public void   setType(String type)    { this.type = type; }

    public long getSize()             { return size; }
    public void setSize(long size)    { this.size = size; }

    public long getCreatedAt()                 { return createdAt; }
    public void setCreatedAt(long createdAt)   { this.createdAt = createdAt; }
}
