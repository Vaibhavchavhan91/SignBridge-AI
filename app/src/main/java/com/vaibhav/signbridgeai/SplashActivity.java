package com.vaibhav.signbridgeai;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Splash screen for SignBridge AI. Displays branding and transitions to MainActivity.
 */
public class SplashActivity extends AppCompatActivity {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable navigateRunnable = () -> {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Transition after 1.2 seconds or on tap
        handler.postDelayed(navigateRunnable, 1200);

        findViewById(R.id.ivSplashLogo).setOnClickListener(v -> {
            handler.removeCallbacks(navigateRunnable);
            navigateRunnable.run();
        });
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(navigateRunnable);
        super.onDestroy();
    }
}
