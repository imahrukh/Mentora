package com.fast.mentor;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Keep;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Model class for representing an assignment submission.
 */
public class AssignmentSubmission implements Parcelable {

    @DocumentId
    private String id;
    private String userId;
    private String assignmentId;
    private String comment;
    private List<SubmissionFile> files;
    private boolean graded;
    private int grade;
    private String feedback;

    @ServerTimestamp
    private Timestamp submittedAt;
    private Timestamp gradedAt;

    // Required empty constructor for Firestore
    public AssignmentSubmission() {
        files = new ArrayList<>();
    }

    /**
     * Constructor with required fields
     */
    public AssignmentSubmission(String id, String userId, String assignmentId) {
        this.id = id;
        this.userId = userId;
        this.assignmentId = assignmentId;
        this.graded = false;
        this.grade = 0;
        this.files = new ArrayList<>();
    }

    // Getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(String assignmentId) {
        this.assignmentId = assignmentId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<SubmissionFile> getFiles() {
        return files;
    }

    public void setFiles(List<SubmissionFile> files) {
        this.files = files != null ? files : new ArrayList<>();
    }

    public boolean getGraded() {
        return graded;
    }

    public void setGraded(boolean graded) {
        this.graded = graded;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    @PropertyName("submittedAt")
    public Date getSubmittedAt() {
        return submittedAt != null ? submittedAt.toDate() : null;
    }

    @PropertyName("submittedAt")
    public void setSubmittedAt(Timestamp submittedAt) {
        this.submittedAt = submittedAt;
    }

    @PropertyName("gradedAt")
    public Date getGradedAt() {
        return gradedAt != null ? gradedAt.toDate() : null;
    }

    @PropertyName("gradedAt")
    public void setGradedAt(Timestamp gradedAt) {
        this.gradedAt = gradedAt;
    }

    // Parcelable implementation

    protected AssignmentSubmission(Parcel in) {
        id = in.readString();
        userId = in.readString();
        assignmentId = in.readString();
        comment = in.readString();
        files = in.createTypedArrayList(SubmissionFile.CREATOR);
        graded = in.readByte() != 0;
        grade = in.readInt();
        feedback = in.readString();
        long submittedAtTime = in.readLong();
        submittedAt = submittedAtTime != -1 ? new Timestamp(new Date(submittedAtTime)) : null;
        long gradedAtTime = in.readLong();
        gradedAt = gradedAtTime != -1 ? new Timestamp(new Date(gradedAtTime)) : null;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(userId);
        dest.writeString(assignmentId);
        dest.writeString(comment);
        dest.writeTypedList(files);
        dest.writeByte((byte) (graded ? 1 : 0));
        dest.writeInt(grade);
        dest.writeString(feedback);
        dest.writeLong(submittedAt != null ? submittedAt.toDate().getTime() : -1);
        dest.writeLong(gradedAt != null ? gradedAt.toDate().getTime() : -1);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AssignmentSubmission> CREATOR = new Creator<AssignmentSubmission>() {
        @Override
        public AssignmentSubmission createFromParcel(Parcel in) {
            return new AssignmentSubmission(in);
        }

        @Override
        public AssignmentSubmission[] newArray(int size) {
            return new AssignmentSubmission[size];
        }
    };
}