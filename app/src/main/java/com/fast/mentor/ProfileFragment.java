package com.fast.mentor;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Fragment for displaying user profile information
 */
public class ProfileFragment extends Fragment {

    // UI components
    private CircleImageView imageProfile;
    private TextView textProfileName, textProfileEmail;
    private Button buttonCertificates, buttonEditProfile;   // ← add this

    // Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    public ProfileFragment() { /* ... */ }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Initialize UI
        imageProfile       = view.findViewById(R.id.image_profile);
        textProfileName    = view.findViewById(R.id.text_profile_name);
        textProfileEmail   = view.findViewById(R.id.text_profile_email);
        buttonCertificates = view.findViewById(R.id.button_certificates);
        buttonEditProfile  = view.findViewById(R.id.buttonEditProfile);   // ← find it

        // Load user info
        loadUserProfile();

        // “My Certificates” placeholder
        buttonCertificates.setOnClickListener(v ->
                Toast.makeText(getContext(),
                        "Certificates screen coming soon!",
                        Toast.LENGTH_SHORT).show()
        );

        // Launch ProfileEditActivity
        buttonEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ProfileEditActivity.class);
            startActivity(intent);
        });

    }

    private void loadUserProfile() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) return;

        String name  = currentUser.getDisplayName();
        String email = currentUser.getEmail();

        textProfileName .setText(name  != null && !name.isEmpty()  ? name  : "No Name");
        textProfileEmail.setText(email != null && !email.isEmpty() ? email : "No Email");

        // Load photo if available
        if (currentUser.getPhotoUrl() != null) {
            Glide.with(this)
                    .load(currentUser.getPhotoUrl())
                    .placeholder(R.drawable.placeholder_profile)
                    .into(imageProfile);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserProfile(); // refresh in case the user edited their profile
    }
}
