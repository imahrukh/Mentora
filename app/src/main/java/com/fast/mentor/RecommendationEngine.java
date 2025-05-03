package com.fast.mentor;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class RecommendationEngine {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void getRecommendations(String userId, String courseId,
                                   Consumer<List<Recommendation>> callback) {
        // Get user progress
        db.collection("users").document(userId)
                .collection("progress").document(courseId)
                .get().addOnSuccessListener(userProgressDoc -> {
                    UserProgress progress = userProgressDoc.toObject(UserProgress.class);

                    // Get course structure
                    db.collection("courses").document(courseId)
                            .get().addOnSuccessListener(courseDoc -> {
                                Course course = courseDoc.toObject(Course.class);
                                List<Recommendation> recommendations = generateRecommendations(progress, course);
                                callback.accept(recommendations);
                            });
                });
    }

    private List<Recommendation> generateRecommendations(UserProgress progress, Course course) {
        List<Recommendation> recommendations = new ArrayList<>();

        // 1. Adaptive Learning Recommendations
        for (Map.Entry<String, ModulePerformance> entry : progress.getModulePerformance().entrySet()) {
            ModulePerformance perf = entry.getValue();
            Module module = findModuleById(course, entry.getKey());

            long expectedTime = module.getExpectedDuration() * 60 * 1000; // mins to ms
            double timeRatio = (double) perf.getTimeSpent() / expectedTime;

            if(timeRatio > 1.2) { // Taking 20% longer than expected
                recommendations.add(new Recommendation(
                        "Foundations of " + module.getTitle(),
                        "Based on your pace in " + module.getTitle(),
                        "Beginner",
                        module.getExpectedDuration() * 2
                ));
            }
            else if(timeRatio < 0.8) { // Finishing 20% faster
                recommendations.add(new Recommendation(
                        "Advanced " + module.getTitle(),
                        "You're progressing quickly in " + module.getTitle(),
                        "Advanced",
                        module.getExpectedDuration() / 2
                ));
            }
        }

        // 2. Next Steps Recommendations
        course.getRelatedCourses().forEach(relatedCourse -> {
            recommendations.add(new Recommendation(
                    relatedCourse,
                    "Natural progression after completing this course",
                    "Next Step",
                    -1 // Duration calculated separately
            ));
        });

        return recommendations;
    }

    private Module findModuleById(Course course, String key) {
        for (Module module : course.getModules()) {
            if (module.getId().equals(key)) {
                return module;
            }
        }
    }
}

