package com.ghappk.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

public class SplashActivity extends Activity {

    private static final int SPLASH_DURATION = 3000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        setContentView(R.layout.activity_splash);

        // Logo zoom-in + fade-in animation
        ImageView logo = findViewById(R.id.splash_logo);
        ScaleAnimation scale = new ScaleAnimation(
            0.5f, 1.0f, 0.5f, 1.0f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        );
        scale.setDuration(900);
        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(900);
        AnimationSet logoAnim = new AnimationSet(true);
        logoAnim.addAnimation(scale);
        logoAnim.addAnimation(fadeIn);
        logo.startAnimation(logoAnim);

        // Developer name slide-up + fade-in (delayed)
        TextView devName = findViewById(R.id.splash_dev_name);
        AlphaAnimation devFade = new AlphaAnimation(0f, 1f);
        devFade.setDuration(700);
        devFade.setStartOffset(900);
        devFade.setFillAfter(true);
        devName.startAnimation(devFade);

        // Tagline fade-in (further delayed)
        TextView tagline = findViewById(R.id.splash_tagline);
        AlphaAnimation tagFade = new AlphaAnimation(0f, 1f);
        tagFade.setDuration(600);
        tagFade.setStartOffset(1400);
        tagFade.setFillAfter(true);
        tagline.startAnimation(tagFade);

        // Navigate to MainActivity after splash duration
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }, SPLASH_DURATION);
    }
}
