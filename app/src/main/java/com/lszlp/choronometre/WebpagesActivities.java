package com.lszlp.choronometre;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem; // Yeni eklendi: MenuItem için
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull; // Yeni eklendi: @NonNull için
import androidx.appcompat.app.AppCompatActivity;

public class WebpagesActivities extends AppCompatActivity {

    private WebView browser; // Geri navigasyon için global yaptık

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_webpages_activities);
        LinearLayout linearLayout = findViewById(R.id.about);
        browser = findViewById(R.id.webView);
        ImageButton backButton = findViewById(R.id.back_button); // Yeni Tanımlama

        Intent intent = getIntent();
        String page = intent.getStringExtra("link");

        // --- Geri Butonuna Tıklama Dinleyicisi Ekleme ---
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        // ----------------------------------------------------


        // --- ActionBar Geri Butonu Etkinleştirme ---
        if (getSupportActionBar() != null) {
            // Geri butonunu görsel olarak aktif eder (<- simgesi)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Bu, genel olarak geri navigasyonun mümkün olduğunu belirtir
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        // ------------------------------------------

        if ("about".equals(page)){ // String karşılaştırması için güvenli kullanım
            System.out.println(page);
            browser.setVisibility(View.GONE);
            linearLayout.setVisibility(View.VISIBLE);
        } else {
            browser.setVisibility(View.VISIBLE);
            linearLayout.setVisibility(View.GONE);

            // WebView ayarları
            browser.setWebViewClient(new WebViewClient());
            browser.clearCache(true);
            browser.loadUrl(page);
            browser.getSettings().setJavaScriptEnabled(true);
        }
    }

    // --- KRİTİK EKLENTİ: Geri Butonuna İşlevsellik Ekleme ---

    /**
     * Kullanıcı ActionBar'daki Geri (Up/Home) butonuna tıkladığında çağrılır.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Geri navigasyonu tetikle
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Cihazın fiziksel/sanal geri tuşuna veya ActionBar geri butonuna basıldığında çağrılır.
     */
    // --- Geri Tuşu Mantığı ---
    @Override
    public void onBackPressed() {
        if (browser != null && browser.getVisibility() == View.VISIBLE && browser.canGoBack()) {
            browser.goBack();
        } else {
            super.onBackPressed();
        }
    }
}