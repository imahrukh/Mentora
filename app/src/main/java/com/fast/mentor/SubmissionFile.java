package com.fast.mentor;

import androidx.annotation.Keep;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

/**
 * Model class for assignment submission files.
 */
@Keep
@IgnoreExtraProperties
public class SubmissionFile {
    
    private String id;
    private String name;
    private String type;
    private String url;
    private long size;
    private @ServerTimestamp Date uploadedAt;
    
    @Exclude
    private boolean isLocal = false;

    /**
     * Default constructor required for Firestore
     */
    public SubmissionFile() {
        // Required empty constructor
    }

    /**
     * Constructor with all fields
     */
    public SubmissionFile(String id, String name, String type, String url, long size, Date uploadedAt) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.url = url;
        this.size = size;
        this.uploadedAt = uploadedAt;
    }

    // Getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUri() {
        return url;
    }

    public void setUri(String uri) {
        this.url = uri;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public Date getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Date uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    @Exclude
    public boolean isLocal() {
        return isLocal;
    }

    @Exclude
    public void setLocal(boolean local) {
        isLocal = local;
    }
}