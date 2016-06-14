package com.example.davidoyeku.custom_classes;

import android.app.Application;

public class MyApp extends Application {

    @Override
    public void onCreate() {
        TypefaceUtil.overrideFont(getApplicationContext(), "SERIF", "fonts/Roboto-Regular.ttf"); // font from assets: "assets/fonts/Roboto-Regular.ttf
    }
}