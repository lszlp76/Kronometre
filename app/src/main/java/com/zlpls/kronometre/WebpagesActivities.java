package com.zlpls.kronometre;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebpagesActivities extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webpages_activities);
        Intent intent = getIntent();
        String page =intent.getStringExtra("link");
        WebView browser = findViewById(R.id.webView);
        browser.setWebViewClient(new WebViewClient());
        browser.clearCache(true);
        browser.loadUrl(page);
        browser.getSettings().setJavaScriptEnabled(true);

    }
}