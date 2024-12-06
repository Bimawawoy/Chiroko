package com.selenoid;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.card.MaterialCardView;


public class AboutActivity extends AppCompatActivity {

    private boolean isDevInfoVisible = false;
    private ValueAnimator currentAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Toolbar setup
        Toolbar toolbar = findViewById(R.id.about_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Developer Card setup
        MaterialCardView devCard1 = findViewById(R.id.dev_card1);
        View devLinks1 = findViewById(R.id.dev_links1);

        Button githubButton = findViewById(R.id.github_button1);
        Button telegramButton = findViewById(R.id.telegram_button1);

        githubButton.setOnClickListener(v -> openUrl("https://github.com/bimawawoy"));
        telegramButton.setOnClickListener(v -> openUrl("https://t.me/sweetaf"));

        devCard1.setOnClickListener(v -> toggleCard(devLinks1));

        // About App Buttons
        Button appGithubButton = findViewById(R.id.github_button);
        Button appContributorsButton = findViewById(R.id.contributors_button);
        Button appChangelogButton = findViewById(R.id.changelog_button);

        appGithubButton.setOnClickListener(v -> openUrl("https://github.com/bimawawoy/"));
        appContributorsButton.setOnClickListener(v -> openUrl("https://github.com/bimawawoy/project1/contributors"));
        appChangelogButton.setOnClickListener(v -> openUrl("https://github.com/bimawawoy/project1/releases"));
    }

    // Helper method to open URLs
    private void openUrl(String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    // Method to toggle visibility with animation
    private void toggleCard(View hiddenView) {
        if (currentAnimator != null && currentAnimator.isRunning()) {
            currentAnimator.cancel(); // Cancel running animation to avoid conflict
        }

        if (hiddenView.getVisibility() == View.GONE) {
            expandCard(hiddenView);
        } else {
            collapseCard(hiddenView);
        }
    }

    private void expandCard(View hiddenView) {
        hiddenView.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int targetHeight = hiddenView.getMeasuredHeight();
        hiddenView.getLayoutParams().height = 0;
        hiddenView.setVisibility(View.VISIBLE);

        currentAnimator = ValueAnimator.ofInt(0, targetHeight);
        currentAnimator.addUpdateListener(animation -> {
            hiddenView.getLayoutParams().height = (int) animation.getAnimatedValue();
            hiddenView.requestLayout();
        });
        currentAnimator.setDuration(300);
        currentAnimator.start();
    }

    private void collapseCard(View hiddenView) {
        final int initialHeight = hiddenView.getHeight();

        currentAnimator = ValueAnimator.ofInt(initialHeight, 0);
        currentAnimator.addUpdateListener(animation -> {
            hiddenView.getLayoutParams().height = (int) animation.getAnimatedValue();
            hiddenView.requestLayout();
        });
        currentAnimator.setDuration(300);
        currentAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                hiddenView.setVisibility(View.GONE);
            }
        });
        currentAnimator.start();
    }
}
