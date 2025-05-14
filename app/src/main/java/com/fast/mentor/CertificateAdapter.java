package com.fast.mentor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fast.mentor.R;
import com.fast.mentor.Certificate;
import com.fast.mentor.Course;

import java.util.List;

/**
 * Adapter for displaying certificates in a RecyclerView
 */
public class CertificateAdapter extends RecyclerView.Adapter<CertificateAdapter.CertificateViewHolder> {
    
    private List<Certificate> certificates;
    private final CertificateClickListener listener;
    
    public interface CertificateClickListener {
        void onViewCertificate(Certificate certificate);
        void onShareCertificate(Certificate certificate);
        void onDownloadCertificate(Certificate certificate);
    }
    
    public CertificateAdapter(List<Certificate> certificates, CertificateClickListener listener) {
        this.certificates = certificates;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public CertificateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_certificate, parent, false);
        return new CertificateViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull CertificateViewHolder holder, int position) {
        Certificate certificate = certificates.get(position);
        holder.bind(certificate, listener);
    }
    
    @Override
    public int getItemCount() {
        return certificates.size();
    }
    
    /**
     * Update the adapter with new certificates
     */
    public void setCertificates(List<Certificate> certificates) {
        this.certificates = certificates;
        notifyDataSetChanged();
    }
    
    /**
     * ViewHolder for certificate items
     */
    static class CertificateViewHolder extends RecyclerView.ViewHolder {
        private final TextView courseNameTextView;
        private final TextView issueDateTextView;
        private final TextView durationTextView;
        private final TextView lessonsTextView;
        private final ImageButton viewButton;
        private final ImageButton shareButton;
        private final ImageButton downloadButton;
        
        public CertificateViewHolder(@NonNull View itemView) {
            super(itemView);
            courseNameTextView = itemView.findViewById(R.id.course_name_text);
            issueDateTextView = itemView.findViewById(R.id.issue_date_text);
            durationTextView = itemView.findViewById(R.id.duration_text);
            lessonsTextView = itemView.findViewById(R.id.lessons_text);
            viewButton = itemView.findViewById(R.id.view_button);
            shareButton = itemView.findViewById(R.id.share_button);
            downloadButton = itemView.findViewById(R.id.download_button);
        }
        
        public void bind(final Certificate certificate, final CertificateClickListener listener) {
            Course course = certificate.getCourse();
            
            // Set course name
            courseNameTextView.setText(course != null ? course.getTitle() : "Unknown Course");
            
            // Set issue date
            issueDateTextView.setText("Issued on: " + certificate.getFormattedDate());
            
            // Set duration and lessons count if available
            if (course != null) {
                int hours = course.getDuration() / 60; // Convert minutes to hours
                durationTextView.setText(hours + (hours == 1 ? " hour" : " hours"));
                lessonsTextView.setText(course.getLessonsCount() + (course.getLessonsCount() == 1 ? " lesson" : " lessons"));
            } else {
                durationTextView.setText("");
                lessonsTextView.setText("");
            }
            
            // Set button click listeners
            viewButton.setOnClickListener(v -> listener.onViewCertificate(certificate));
            shareButton.setOnClickListener(v -> listener.onShareCertificate(certificate));
            downloadButton.setOnClickListener(v -> listener.onDownloadCertificate(certificate));
        }
    }
}