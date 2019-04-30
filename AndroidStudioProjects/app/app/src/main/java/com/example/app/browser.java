package com.example.app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

public class browser extends AppCompatActivity {
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        String url =(String)extras.get("resultado");

        webView = (WebView)findViewById(R.id.Webview);
        webView.getSettings().setJavaScriptEnabled(true);

        webView.loadUrl(url);




    }
}