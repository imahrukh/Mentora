package com.fast.mentor;

import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class AdminUploadActivity extends AppCompatActivity {
    private void uploadCourse(Course course) {
        FirebaseFirestore.getInstance()
                .collection("courses")
                .document(course.getCourseId())
                .set(course)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Course uploaded!", Toast.LENGTH_SHORT).show();
                });
    }
}