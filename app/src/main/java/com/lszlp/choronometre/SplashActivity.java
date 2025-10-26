package com.lszlp.choronometre;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // ActionBar'ı gizle
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Handler'ı doğru şekilde kullan
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                goToMainActivity();
            }
        }, SPLASH_DURATION);
    }

    private void goToMainActivity() {
        try {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Basit geçiş
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}