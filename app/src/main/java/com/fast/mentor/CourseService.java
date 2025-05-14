package com.fast.mentor;

import com.fast.mentor.Assignment;
import com.fast.mentor.AssignmentSubmission;
import com.fast.mentor.Course;
import com.fast.mentor.Enrollment;
import com.fast.mentor.Lesson;
import com.fast.mentor.Module;
import com.fast.mentor.Resource;
import com.fast.mentor.SubmissionFile;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service class for course-related operations using Firestore.
 */
public class CourseService {

    private FirebaseFirestore db;

    public void getUserCourseProgress(String userId, String courseId, OnProgressCalculatedCallback callback, OnFailureListener onFailure) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Fetch all modules in the course
        db.collection("courses")
                .document(courseId)
                .collection("modules")
                .get()
                .addOnSuccessListener(moduleSnapshots -> {
                    int totalModules = moduleSnapshots.size();

                    if (totalModules == 0) {
                        callback.onProgressCalculated(0.0); // No modules = 0% progress
                        return;
                    }

                    // Fetch completed modules by the user
                    db.collection("progress")
                            .whereEqualTo("userId", userId)
                            .whereEqualTo("courseId", courseId)
                            .whereEqualTo("isCompleted", true)
                            .get()
                            .addOnSuccessListener(progressSnapshots -> {
                                int completedModules = progressSnapshots.size();
                                double progress = (completedModules * 100.0) / totalModules;
                                callback.onProgressCalculated(progress);
                            })
                            .addOnFailureListener((com.google.android.gms.tasks.OnFailureListener) onFailure);

                })
                .addOnFailureListener((com.google.android.gms.tasks.OnFailureListener) onFailure);
    }
    public interface OnSuccessListener<T> {
        void onSuccess(T result);
    }

    public interface OnFailureListener {
        void onFailure(Exception e);
    }

    public CourseService() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Get a specific course by ID
     */
    public void getCourse(String courseId, OnSuccessListener<Course> successListener,
                          OnFailureListener failureListener) {
        db.collection("courses").document(courseId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Course course = documentSnapshot.toObject(Course.class);
                        course.setCourseId(documentSnapshot.getId());
                        successListener.onSuccess(course);
                    } else {
                        failureListener.onFailure(new Exception("Course not found"));
                    }
                })
                .addOnFailureListener(e -> failureListener.onFailure(e));
    }

    /**
     * Get all courses
     */
    public void getAllCourses(OnSuccessListener<List<Course>> successListener,
                              OnFailureListener failureListener) {
        db.collection("courses")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Course> courses = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Course course = document.toObject(Course.class);
                        course.setCourseId(document.getId());
                        courses.add(course);
                    }
                    successListener.onSuccess(courses);
                })
                .addOnFailureListener(e -> failureListener.onFailure(e));
    }

    /**
     * Get courses by category
     */
    public void getCoursesByCategory(String category, OnSuccessListener<List<Course>> successListener,
                                     OnFailureListener failureListener) {
        db.collection("courses")
                .whereEqualTo("category", category)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Course> courses = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Course course = document.toObject(Course.class);
                        course.setCourseId(document.getId());
                        courses.add(course);
                    }
                    successListener.onSuccess(courses);
                })
                .addOnFailureListener(e -> failureListener.onFailure(e));
    }

    /**
     * Search courses by title or description
     */
    public void searchCourses(String query, OnSuccessListener<List<Course>> successListener,
                              OnFailureListener failureListener) {
        // Firestore doesn't support direct text search, so we need to use a more complex approach
        // For simplicity, we'll just fetch all courses and filter them client-side
        // In a production app, you might want to use a more efficient approach like Algolia

        db.collection("courses")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Course> courses = new ArrayList<>();
                    String lowercaseQuery = query.toLowerCase();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Course course = document.toObject(Course.class);
                        course.setCourseId(document.getId());

                        // Check if title or description contains the search query
                        if (course.getTitle().toLowerCase().contains(lowercaseQuery) ||
                                course.getDescription().toLowerCase().contains(lowercaseQuery)) {
                            courses.add(course);
                        }
                    }

                    successListener.onSuccess(courses);
                })
                .addOnFailureListener(e -> failureListener.onFailure(e));
    }

    /**
     * Get all modules for a course
     */
    public void getCourseModules(String courseId, OnSuccessListener<List<Module>> successListener,
                                 OnFailureListener failureListener) {
        db.collection("modules")
                .whereEqualTo("courseId", courseId)
                .orderBy("order", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Module> modules = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Module module = document.toObject(Module.class);
                        module.setModuleId(document.getId());
                        modules.add(module);
                    }

                    // Now fetch the lessons for each module
                    fetchLessonsForModules(modules, successListener, failureListener);
                })
                .addOnFailureListener(e -> failureListener.onFailure(e));
    }

    /**
     * Fetch lessons for a list of modules
     */
    private void fetchLessonsForModules(List<Module> modules,
                                        OnSuccessListener<List<Module>> successListener,
                                        OnFailureListener failureListener) {
        if (modules.isEmpty()) {
            successListener.onSuccess(modules);
            return;
        }

        // Track completion for all modules
        final int[] completedModules = {0};

        for (Module module : modules) {
            db.collection("lessons")
                    .whereEqualTo("moduleId", module.getModuleId())
                    .orderBy("order", Query.Direction.ASCENDING)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<Lesson> lessons = new ArrayList<>();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Lesson lesson = document.toObject(Lesson.class);
                            lesson.setId(document.getId());
                            lessons.add(lesson);
                        }

                        module.setItems(lessons);

                        // Check if all modules have been processed
                        completedModules[0]++;
                        if (completedModules[0] == modules.size()) {
                            successListener.onSuccess(modules);
                        }
                    })
                    .addOnFailureListener(e -> failureListener.onFailure(e));
        }
    }

    /**
     * Get a specific lesson by ID
     */
    public void getLesson(String lessonId, OnSuccessListener<Lesson> successListener,
                          OnFailureListener failureListener) {
        db.collection("lessons").document(lessonId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Lesson lesson = documentSnapshot.toObject(Lesson.class);
                        lesson.setId(documentSnapshot.getId());
                        successListener.onSuccess(lesson);
                    } else {
                        failureListener.onFailure(new Exception("Lesson not found"));
                    }
                })
                .addOnFailureListener(e -> failureListener.onFailure(e));
    }

    /**
     * Get resources for a lesson
     */
    public void getLessonResources(String lessonId, OnSuccessListener<List<Resource>> successListener,
                                   OnFailureListener failureListener) {
        db.collection("resources")
                .whereEqualTo("lessonId", lessonId)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Resource> resources = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Resource resource = document.toObject(Resource.class);
                        resource.setId(document.getId());
                        resources.add(resource);
                    }
                    successListener.onSuccess(resources);
                })
                .addOnFailureListener(e -> failureListener.onFailure(e));
    }

    /**
     * Get user's enrollment for a specific course
     */
    public void getUserEnrollment(String userId, String courseId,
                                  OnSuccessListener<Enrollment> successListener,
                                  OnFailureListener failureListener) {
        db.collection("enrollments")
                .whereEqualTo("userId", userId)
                .whereEqualTo("courseId", courseId)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        Enrollment enrollment = document.toObject(Enrollment.class);
                        enrollment.setId(document.getId());
                        successListener.onSuccess(enrollment);
                    } else {
                        successListener.onSuccess(null); // Not enrolled
                    }
                })
                .addOnFailureListener(e -> failureListener.onFailure(e));
    }

    /**
     * Get all enrollments for a user
     */
    public void getUserEnrollments(String userId, OnSuccessListener<List<Enrollment>> successListener,
                                   OnFailureListener failureListener) {
        db.collection("enrollments")
                .whereEqualTo("userId", userId)
                .orderBy("enrolledAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Enrollment> enrollments = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Enrollment enrollment = document.toObject(Enrollment.class);
                        enrollment.setId(document.getId());
                        enrollments.add(enrollment);
                    }
                    successListener.onSuccess(enrollments);
                })
                .addOnFailureListener(e -> failureListener.onFailure(e));
    }

    /**
     * Enroll a user in a course
     */
    public void enrollInCourse(String userId, String courseId,
                               OnSuccessListener<Enrollment> successListener,
                               OnFailureListener failureListener) {
        // First check if user is already enrolled
        getUserEnrollment(userId, courseId, enrollment -> {
            if (enrollment != null) {
                // Already enrolled
                successListener.onSuccess(enrollment);
                return;
            }

            // Create a new enrollment
            DocumentReference enrollmentRef = db.collection("enrollments").document();
            Enrollment newEnrollment = new Enrollment(enrollmentRef.getId(), userId, courseId);

            // Create a batch operation to update both enrollment and course
            WriteBatch batch = db.batch();

            // Add enrollment
            batch.set(enrollmentRef, newEnrollment);

            // Increment enrolled count in course
            DocumentReference courseRef = db.collection("courses").document(courseId);
            batch.update(courseRef, "enrolledCount", FieldValue.increment(1));

            // Commit the batch
            batch.commit()
                    .addOnSuccessListener(aVoid -> successListener.onSuccess(newEnrollment))
                    .addOnFailureListener(e -> failureListener.onFailure(e));

        }, failureListener);
    }

    /**
     * Update user's progress in a course
     */
    public void updateCourseProgress(String userId, String courseId, int progress,
                                     OnSuccessListener<Enrollment> successListener,
                                     OnFailureListener failureListener) {
        getUserEnrollment(userId, courseId, enrollment -> {
            if (enrollment == null) {
                failureListener.onFailure(new Exception("User is not enrolled in this course"));
                return;
            }

            // Update progress
            enrollment.setProgress(progress);
            enrollment.updateLastAccessed();

            // Check if course is completed
            if (progress >= 100) {
                enrollment.markAsCompleted();
            }

            // Update enrollment in Firestore
            db.collection("enrollments").document(enrollment.getId())
                    .set(enrollment)
                    .addOnSuccessListener(aVoid -> successListener.onSuccess(enrollment))
                    .addOnFailureListener(e -> failureListener.onFailure(e));

        }, failureListener);
    }

    /**
     * Mark lesson progress for a user
     */
    public void updateLessonProgress(String userId, String lessonId, int progress,
                                     OnSuccessListener<Void> successListener,
                                     OnFailureListener failureListener) {
        // Create a map for the progress document
        Map<String, Object> progressData = new HashMap<>();
        progressData.put("userId", userId);
        progressData.put("lessonId", lessonId);
        progressData.put("progress", progress);
        progressData.put("lastUpdated", FieldValue.serverTimestamp());

        // For existing progress, use a compound key
        String progressId = userId + "_" + lessonId;

        db.collection("progress").document(progressId)
                .set(progressData)
                .addOnSuccessListener(aVoid -> {
                    // After updating lesson progress, we should update course progress
                    // but for simplicity, we'll skip that for now
                    successListener.onSuccess(null);
                })
                .addOnFailureListener(e -> failureListener.onFailure(e));
    }

    /**
     * Get assignment for a lesson
     */
    public void getAssignment(String lessonId, OnSuccessListener<Assignment> successListener,
                              OnFailureListener failureListener) {
        db.collection("assignments")
                .whereEqualTo("lessonId", lessonId)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        Assignment assignment = document.toObject(Assignment.class);
                        assignment.setId(document.getId());
                        successListener.onSuccess(assignment);
                    } else {
                        failureListener.onFailure(new Exception("Assignment not found"));
                    }
                })
                .addOnFailureListener(e -> failureListener.onFailure(e));
    }

    /**
     * Get assignment submission for a user
     */
    public void getAssignmentSubmission(String userId, String assignmentId,
                                        OnSuccessListener<AssignmentSubmission> successListener,
                                        OnFailureListener failureListener) {
        db.collection("assignment_submissions")
                .whereEqualTo("userId", userId)
                .whereEqualTo("assignmentId", assignmentId)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        AssignmentSubmission submission = document.toObject(AssignmentSubmission.class);
                        submission.setId(document.getId());
                        successListener.onSuccess(submission);
                    } else {
                        successListener.onSuccess(null); // No submission yet
                    }
                })
                .addOnFailureListener(e -> failureListener.onFailure(e));
    }

    /**
     * Submit assignment
     */
    public void submitAssignment(String userId, String assignmentId, String comment,
                                 List<SubmissionFile> files, String submissionId,
                                 OnSuccessListener<AssignmentSubmission> successListener,
                                 OnFailureListener failureListener) {
        // Create or update submission
        DocumentReference submissionRef;

        if (submissionId != null && !submissionId.isEmpty()) {
            // Update existing submission
            submissionRef = db.collection("assignment_submissions").document(submissionId);
        } else {
            // Create new submission
            submissionRef = db.collection("assignment_submissions").document();
        }

        // Create submission object
        AssignmentSubmission submission = new AssignmentSubmission(
                submissionRef.getId(), userId, assignmentId);
        submission.setComment(comment);
        submission.setFiles(files);

        // Reset grading if resubmitting
        if (submissionId != null && !submissionId.isEmpty()) {
            submission.setGraded(false);
            submission.setGrade(0);
            submission.setFeedback(null);
        }

        // Save to Firestore
        submissionRef.set(submission)
                .addOnSuccessListener(aVoid -> {
                    // Mark the lesson as complete
                    getAssignment(assignmentId, assignment -> {
                        String lessonId = assignment.getLessonId();
                        updateLessonProgress(userId, lessonId, 100,
                                aVoid2 -> successListener.onSuccess(submission),
                                failureListener);
                    }, failureListener);
                })
                .addOnFailureListener(e -> failureListener.onFailure(e));
    }
}