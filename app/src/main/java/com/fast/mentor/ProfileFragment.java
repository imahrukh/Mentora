package com.fast.mentor;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.fast.mentor.R;
import com.fast.mentor.auth.LoginActivity;
import com.fast.mentor.models.UserProfile;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Fragment for displaying user profile information
 */
public class ProfileFragment extends Fragment {

    // UI components
    private CircleImageView ivProfilePicture;
    private TextView tvFullName, tvUsername, tvBio;
    private TextView tvCoursesCompletedCount, tvHoursSpentCount, tvCertificatesEarnedCount;
    private ChipGroup chipGroupInterests;
    private Button btnEditProfile, btnLogout;
    private LinearLayout llChangePassword, llNotifications, llPaymentMethods;
    private LinearLayout llHelpSupport, llPrivacyPolicy, llTermsConditions;
    private View progressBar;

    // Firebase components
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    // Data
    private UserProfile userProfile;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Initialize UI components
        initializeViews(view);
        setupListeners();

        // Load user profile data
        loadUserProfile();
    }

    private void initializeViews(View view) {
        ivProfilePicture = view.findViewById(R.id.ivProfilePicture);
        tvFullName = view.findViewById(R.id.tvFullName);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvBio = view.findViewById(R.id.tvBio);
        tvCoursesCompletedCount = view.findViewById(R.id.tvCoursesCompletedCount);
        tvHoursSpentCount = view.findViewById(R.id.tvHoursSpentCount);
        tvCertificatesEarnedCount = view.findViewById(R.id.tvCertificatesEarnedCount);
        chipGroupInterests = view.findViewById(R.id.chipGroupInterests);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnLogout = view.findViewById(R.id.btnLogout);
        llChangePassword = view.findViewById(R.id.llChangePassword);
        llNotifications = view.findViewById(R.id.llNotifications);
        llPaymentMethods = view.findViewById(R.id.llPaymentMethods);
        llHelpSupport = view.findViewById(R.id.llHelpSupport);
        llPrivacyPolicy = view.findViewById(R.id.llPrivacyPolicy);
        llTermsConditions = view.findViewById(R.id.llTermsConditions);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        // Edit profile button
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ProfileEditActivity.class);
            startActivity(intent);
        });

        // Logout button
        btnLogout.setOnClickListener(v -> showLogoutConfirmationDialog());

        // Settings items
        llChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ChangePasswordActivity.class);
            startActivity(intent);
        });

        llNotifications.setOnClickListener(v -> {
            // TODO: Implement notifications settings
            Toast.makeText(getContext(), "Notifications settings coming soon", Toast.LENGTH_SHORT).show();
        });

        llPaymentMethods.setOnClickListener(v -> {
            // TODO: Implement payment methods
            Toast.makeText(getContext(), "Payment methods coming soon", Toast.LENGTH_SHORT).show();
        });

        // Support and legal items
        llHelpSupport.setOnClickListener(v -> {
            // TODO: Implement help & support
            Toast.makeText(getContext(), "Help & Support coming soon", Toast.LENGTH_SHORT).show();
        });

        llPrivacyPolicy.setOnClickListener(v -> {
            // TODO: Implement privacy policy
            Toast.makeText(getContext(), "Privacy Policy coming soon", Toast.LENGTH_SHORT).show();
        });

        llTermsConditions.setOnClickListener(v -> {
            // TODO: Implement terms & conditions
            Toast.makeText(getContext(), "Terms & Conditions coming soon", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadUserProfile() {
        showLoading(true);

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            // Not logged in, redirect to login
            navigateToLogin();
            return;
        }

        String userId = currentUser.getUid();
        firestore.collection("userProfiles").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Create user profile from Firestore document
                        userProfile = new UserProfile();
                        userProfile.setId(Integer.parseInt(userId));
                        userProfile.setFullName((String) documentSnapshot.get("fullName"));
                        userProfile.setUsername((String) documentSnapshot.get("username"));
                        userProfile.setEmail((String) documentSnapshot.get("email"));
                        userProfile.setBio((String) documentSnapshot.get("bio"));
                        userProfile.setProfilePictureUrl((String) documentSnapshot.get("profilePictureUrl"));
                        userProfile.setPreferredLanguage((String) documentSnapshot.get("preferredLanguage"));
                        
                        // Get statistics
                        if (documentSnapshot.get("coursesCompleted") != null) {
                            userProfile.setCoursesCompleted(((Long) documentSnapshot.get("coursesCompleted")).intValue());
                        }
                        if (documentSnapshot.get("hoursSpent") != null) {
                            userProfile.setHoursSpent(((Long) documentSnapshot.get("hoursSpent")).intValue());
                        }
                        if (documentSnapshot.get("certificatesEarned") != null) {
                            userProfile.setCertificatesEarned(((Long) documentSnapshot.get("certificatesEarned")).intValue());
                        }
                        
                        // Get interests
                        List<String> interests = (List<String>) documentSnapshot.get("interests");
                        if (interests != null) {
                            userProfile.setInterests(interests);
                        }
                        
                        // Update UI with the loaded data
                        updateUIWithProfileData();
                    } else {
                        // Create a basic profile with user data from Firebase Auth
                        userProfile = new UserProfile();
                        userProfile.setId(Integer.parseInt(userId));
                        userProfile.setEmail(currentUser.getEmail());
                        userProfile.setFullName(currentUser.getDisplayName());
                        String username = currentUser.getEmail() != null ? 
                                currentUser.getEmail().split("@")[0] : "user" + userId;
                        userProfile.setUsername(username);
                        
                        // Update UI with basic data
                        updateUIWithProfileData();
                    }
                    showLoading(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error loading profile: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    showLoading(false);
                });
    }

    private void updateUIWithProfileData() {
        if (userProfile == null || getContext() == null) return;

        // Set basic profile info
        tvFullName.setText(userProfile.getFullName());
        tvUsername.setText("@" + userProfile.getUsername());
        tvBio.setText(userProfile.getBio());

        // Set stats
        tvCoursesCompletedCount.setText(String.valueOf(userProfile.getCoursesCompleted()));
        tvHoursSpentCount.setText(String.valueOf(userProfile.getHoursSpent()));
        tvCertificatesEarnedCount.setText(String.valueOf(userProfile.getCertificatesEarned()));

        // Load profile picture if available
        String profilePictureUrl = userProfile.getProfilePictureUrl();
        if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
            // In a real app, you would use a library like Glide or Picasso to load the image
            // For example:
            // Glide.with(this).load(profilePictureUrl).into(ivProfilePicture);
        }

        // Add interests chips
        chipGroupInterests.removeAllViews();
        List<String> interests = userProfile.getInterests();
        if (interests != null && !interests.isEmpty()) {
            for (String interest : interests) {
                Chip chip = new Chip(getContext());
                chip.setText(interest);
                chip.setClickable(false);
                chip.setCheckable(false);
                chipGroupInterests.addView(chip);
            }
        } else {
            // Show a placeholder message if no interests
            TextView tvNoInterests = new TextView(getContext());
            tvNoInterests.setText("Add interests in your profile settings");
            tvNoInterests.setTextColor(getResources().getColor(R.color.textSecondary, null));
            chipGroupInterests.addView(tvNoInterests);
        }
    }

    private void showLogoutConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> logout())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void logout() {
        firebaseAuth.signOut();
        navigateToLogin();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    private void showLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload user profile when coming back to this fragment
        loadUserProfile();
    }
}