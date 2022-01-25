package com.lszlp.choronometre;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class WebpagesActivities extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webpages_activities);
        LinearLayout linearLayout = findViewById(R.id.about);
        WebView browser = findViewById(R.id.webView);
        Intent intent = getIntent();
        String page = intent.getStringExtra("link");


        if (String.valueOf(page).equals("about")){
            System.out.println(page);
            browser.setVisibility(View.GONE);
            linearLayout.setVisibility(View.VISIBLE);
        }else {


            browser.setVisibility(View.VISIBLE);
            linearLayout.setVisibility(View.GONE);
            browser.setWebViewClient(new WebViewClient());
            browser.clearCache(true);
            browser.loadUrl(page);
            browser.getSettings().setJavaScriptEnabled(true);
        }








    }
}