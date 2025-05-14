package com.fast.mentor;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Keep;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;


/**
 * Model class for representing a file in an assignment submission.
 */
public class SubmissionFile implements Parcelable {

    private String id;
    private String name;
    private String type;
    private String url;
    private String localUri;
    private long size;

    @ServerTimestamp
    private Timestamp uploadedAt;

    // Required empty constructor for Firestore
    public SubmissionFile() {
    }

    /**
     * Constructor with required fields
     */
    public SubmissionFile(String id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
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

    public String getLocalUri() {
        return localUri;
    }

    public void setLocalUri(String localUri) {
        this.localUri = localUri;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    @PropertyName("uploadedAt")
    public Date getUploadedAt() {
        return uploadedAt != null ? uploadedAt.toDate() : null;
    }

    @PropertyName("uploadedAt")
    public void setUploadedAt(Timestamp uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    // Parcelable implementation

    protected SubmissionFile(Parcel in) {
        id = in.readString();
        name = in.readString();
        type = in.readString();
        url = in.readString();
        localUri = in.readString();
        size = in.readLong();
        long uploadedAtTime = in.readLong();
        uploadedAt = uploadedAtTime != -1 ? new Timestamp(new Date(uploadedAtTime)) : null;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(type);
        dest.writeString(url);
        dest.writeString(localUri);
        dest.writeLong(size);
        dest.writeLong(uploadedAt != null ? uploadedAt.toDate().getTime() : -1);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SubmissionFile> CREATOR = new Creator<SubmissionFile>() {
        @Override
        public SubmissionFile createFromParcel(Parcel in) {
            return new SubmissionFile(in);
        }

        @Override
        public SubmissionFile[] newArray(int size) {
            return new SubmissionFile[size];
        }
    };
}