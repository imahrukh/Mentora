package com.fast.mentor;

import com.fast.mentor.course.CourseService;

/**
 * Manages tracking and updating of lesson progress.
 * Communicates with Firestore to update user progress.
 */
public class LessonProgressManager {

    private final CourseService courseService;

    /**
     * Interface for progress update callbacks
     */
    public interface ProgressUpdateCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    /**
     * Constructor
     */
    public LessonProgressManager() {
        courseService = new CourseService();
    }

    /**
     * Mark a lesson as complete (100% progress)
     */
    public void markLessonComplete(String userId, String lessonId, String courseId, 
                                 ProgressUpdateCallback callback) {
        // Update lesson progress
        courseService.updateLessonProgress(userId, lessonId, 100, 
            aVoid -> {
                // After updating lesson progress, update course progress
                updateCourseProgress(userId, courseId, callback);
            },
            e -> {
                if (callback != null) {
                    callback.onFailure(e);
                }
            }
        );
    }

    /**
     * Update lesson progress
     */
    public void updateLessonProgress(String userId, String lessonId, String courseId, int progress, 
                                   ProgressUpdateCallback callback) {
        courseService.updateLessonProgress(userId, lessonId, progress, 
            aVoid -> {
                // For partial progress, only update course progress if 100%
                if (progress >= 100) {
                    updateCourseProgress(userId, courseId, callback);
                } else if (callback != null) {
                    callback.onSuccess();
                }
            },
            e -> {
                if (callback != null) {
                    callback.onFailure(e);
                }
            }
        );
    }

    /**
     * Auto-complete lesson based on current position (for video/document)
     */
    public void autoCompleteLesson(String userId, String lessonId, String courseId, int lastPosition, 
                                   int totalDuration, ProgressUpdateCallback callback) {
        // Calculate progress percentage
        int progress = (int) (((float) lastPosition / totalDuration) * 100);
        
        // If we're near the end (>= 90%), consider it complete
        if (progress >= 90) {
            markLessonComplete(userId, lessonId, courseId, callback);
        } else {
            // Otherwise just update the progress
            updateLessonProgress(userId, lessonId, courseId, progress, callback);
        }
    }

    /**
     * Update overall course progress
     */
    private void updateCourseProgress(String userId, String courseId, ProgressUpdateCallback callback) {
        // Calculate the user's overall progress in the course
        courseService.getUserCourseProgress(userId, courseId, progress -> {
            // Update enrollment with new progress
            courseService.updateCourseProgress(userId, courseId, progress, 
                enrollment -> {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                },
                e -> {
                    if (callback != null) {
                        callback.onFailure(e);
                    }
                }
            );
        }, e -> {
            if (callback != null) {
                callback.onFailure(e);
            }
        });
    }
}