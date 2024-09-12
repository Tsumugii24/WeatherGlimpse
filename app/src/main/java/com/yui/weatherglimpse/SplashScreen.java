package com.yui.weatherglimpse;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.yui.weatherglimpse.databinding.ActivitySplashScreenBinding;

@SuppressLint("CustomSplashScreen")
public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySplashScreenBinding binding = ActivitySplashScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //Removing status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //Setting Splash
        splashScreen();
    }

    // private void splashScreen() {
    //     int SPLASH_TIME = 4000;
    //     new Handler().postDelayed(() -> {
    //         Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
    //         startActivity(intent);
    //         finish();
    //     }, SPLASH_TIME);
    // }

    private void splashScreen() {
        int SPLASH_TIME = 2000;
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        }, SPLASH_TIME);
    }
}