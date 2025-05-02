package com.fast.mentor;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    ImageView editIcon;
    FrameLayout contentFrame;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        editIcon = findViewById(R.id.editIcon);
        contentFrame = findViewById(R.id.contentFrame);

        // Navigate to Edit Profile
        editIcon.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });

        // Dummy data
        List<Certificate> certificateList = new ArrayList<>();
        // Uncomment below lines to add sample certificate
        // certificateList.add(new Certificate(R.drawable.certificate, "Android Basics", "Coursera"));
        // certificateList.add(new Certificate(R.drawable.certificate, "Java Fundamentals", "Udemy"));

        if (certificateList.isEmpty()) {
            // Load "No Certificates" view
            View noCertView = LayoutInflater.from(this).inflate(R.layout.item_no_certificates, contentFrame, false);
            contentFrame.addView(noCertView);

            Button startLearningBtn = noCertView.findViewById(R.id.startLearningBtn);
            startLearningBtn.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, ExploreActivity.class);
                startActivity(intent);
            });
        } else {
            // Load RecyclerView
            View certView = LayoutInflater.from(this).inflate(R.layout.recycler_certificates, contentFrame, false);
            RecyclerView recyclerView = certView.findViewById(R.id.recyclerCertificates);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            CertificateAdapter adapter = new CertificateAdapter(this, certificateList);
            recyclerView.setAdapter(adapter);
            contentFrame.addView(certView);
        }
    }
}

