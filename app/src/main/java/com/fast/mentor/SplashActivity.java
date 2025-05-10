package com.fast.mentor;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.fast.mentor.auth.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Splash screen activity that shows at app startup
 */
public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DURATION = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize views
        ImageView logoImageView = findViewById(R.id.ivLogo);
        TextView appNameTextView = findViewById(R.id.tvAppName);
        TextView taglineTextView = findViewById(R.id.tvTagline);

        // Create fade-in animation
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(1000);
        fadeIn.setFillAfter(true);
        
        // Set animation listener
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // Animation started
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // Start a handler to navigate after splash duration
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    // Check if user is already logged in
                    if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                        // User is logged in, navigate to main activity
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    } else {
                        // User is not logged in, navigate to login activity
                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    }
                    finish(); // Close splash activity so it's not in the back stack
                }, SPLASH_DURATION);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // Animation repeated
            }
        });

        // Apply animations
        logoImageView.startAnimation(fadeIn);
        appNameTextView.startAnimation(fadeIn);
        taglineTextView.startAnimation(fadeIn);
    }
}