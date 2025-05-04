package com.fast.mentor;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import org.greenrobot.eventbus.EventBus;

public class PdfFragment extends Fragment {
    private PDFView pdfView;
    private String pdfUrl;
    private String courseId;
    private String moduleId;
    private String itemId;
    private ProgressBar progressBar;
    private FirebaseFirestore db;

    public static PdfFragment newInstance(String pdfUrl, String courseId, String moduleId, String itemId) {
        PdfFragment fragment = new PdfFragment();
        Bundle args = new Bundle();
        args.putString("pdfUrl", pdfUrl);
        args.putString("courseId", courseId);
        args.putString("moduleId", moduleId);
        args.putString("itemId", itemId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pdf, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        progressBar = view.findViewById(R.id.progressBar);
        pdfView = view.findViewById(R.id.pdfView);

        if (getArguments() != null) {
            pdfUrl = getArguments().getString("pdfUrl");
            courseId = getArguments().getString("courseId");
            moduleId = getArguments().getString("moduleId");
            itemId = getArguments().getString("itemId");

            loadPdfDocument();
        }
    }

    private void loadPdfDocument() {
        progressBar.setVisibility(View.VISIBLE);

        pdfView.fromUri(Uri.parse(pdfUrl))
                .defaultPage(0)
                .enableSwipe(true)
                .swipeHorizontal(false)
                .pageFitPolicy(FitPolicy.WIDTH)
                .scrollHandle(new DefaultScrollHandle(requireContext()))
                .onLoad(nbPages -> progressBar.setVisibility(View.GONE))
                .onPageScroll((page, positionOffset) -> {
                    if (page == pdfView.getPageCount() - 1 && positionOffset >= 0.99f) {
                        markAsCompleted();
                    }
                })
                .onError(t -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Error loading PDF: " + t.getMessage(), Toast.LENGTH_LONG).show();
                })
                .load();
    }

    private void markAsCompleted() {
        Map<String, Object> progressUpdate = new HashMap<>();
        progressUpdate.put("completed", true);
        progressUpdate.put("completionTime", System.currentTimeMillis());
        progressUpdate.put("type", "pdf");

        db.collection("users").document(getCurrentUserId())
                .collection("enrolledCourses").document(courseId)
                .collection("progress").document(moduleId + "_" + itemId)
                .set(progressUpdate)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "PDF marked as completed!", Toast.LENGTH_SHORT).show();
                    updateCourseProgress();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Progress update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateCourseProgress() {
        db.collection("courses").document(courseId)
                .collection("enrollments").document(getCurrentUserId())
                .update("progress", FieldValue.increment(5))
                .addOnSuccessListener(aVoid -> {
                    // Post event with actual progress data
                    EventBus.getDefault().post(new ProgressUpdateEvent(
                            courseId,
                            moduleId,
                            newProgressValue
                    ));
                });
    }
    private String getCurrentUserId() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
}}