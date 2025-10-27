package com.lszlp.choronometre; // Kendi paket adınızı kullanın

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- Durum Çubuğunu Gizleme Kodu Bitişi ---
        setContentView(R.layout.activity_splash);

        ImageView splashLogo = findViewById(R.id.splash_logo);
        ImageView rotateLogo = findViewById(R.id.title);
        ImageView outbackLogo = findViewById(R.id.outback);



        // 1. Animasyonu yükle
        Animation scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_in);
        Animation rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_scale);
        Animation fadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_out);

        rotateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        // 2. Animasyonun bitişini dinle
        scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // Animasyon başladığında yapılacaklar (isteğe bağlı)
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // 3. Animasyon bittiğinde MainActivity'yi çağır
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);

                // Splash ekranını geri tuşuyla açılmasını engellemek için bitir
                finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // Tekrarlama (gerek yok)
            }
        });

        // 4. Animasyonu ImageView üzerinde başlat
        splashLogo.startAnimation(scaleAnimation);
        rotateLogo.startAnimation(rotateAnimation);
        outbackLogo.startAnimation(fadeOutAnimation);
    }
}