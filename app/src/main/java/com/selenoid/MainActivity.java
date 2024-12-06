package com.selenoid;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.content.Intent;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.selenoid.databinding.ActivityMainBinding;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private Handler handler;
    private Runnable ramUpdateRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // init binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        toolbar.setOnMenuItemClickListener(menuItem -> {
            int itemId = menuItem.getItemId();
            if (itemId == R.id.logs) {
                showLogsDialog();
                return true;
            } else if (itemId == R.id.switch_theme) {
                showThemeDialog();
                return true;
            } else if (itemId == R.id.settings) {
                showSettings();
                return true;
            } else if (itemId == R.id.about) {
                showAbout();
                return true;
            } else {
                return false;
            }
        });

        AppBarLayout appBarLayout = findViewById(R.id.barlayout);
        appBarLayout.addOnOffsetChangedListener((appBarLayout1, verticalOffset) -> {
            if (verticalOffset == 0) {
                setStatusBarColor(R.color.colorPrimary);
            } else {
                setStatusBarColor(R.color.colorPrimary);
            }
        });

        // Update RAM dynamically
        handler = new Handler();
        ramUpdateRunnable = () -> {
            updateRamUsage();
            handler.postDelayed(ramUpdateRunnable, 500);
        };

        handler.post(ramUpdateRunnable);

        // Add listener to RAM card
        MaterialCardView ramCard = findViewById(R.id.ram_card);
        ramCard.setOnClickListener(v -> {
            if (isDeviceRooted()) {
                clearRamWithRoot();
            } else {
                Toast.makeText(this, "Device is not rooted!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_app_bar_menu, menu);
        return true;
    }

    // Show logs in a Material Dialog
    private void showLogsDialog() {
        new Thread(() -> {
            StringBuilder logBuilder = new StringBuilder();
            try {
                Process process = Runtime.getRuntime().exec("logcat -d *:V");
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    logBuilder.append(line).append("\n");
                }

                process.destroy();
            } catch (IOException e) {
                logBuilder.append("Failed to retrieve logs.");
            }

            String logs = logBuilder.toString();
            runOnUiThread(() -> {
                new MaterialAlertDialogBuilder(this)
                        .setTitle("System Logs")
                        .setMessage(logs.isEmpty() ? "No logs available." : logs)
                        .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                        .show();
            });
        }).start();
    }

    // Show settings placeholder
    private void showSettings() {
        Toast.makeText(this, "Settings clicked!", Toast.LENGTH_SHORT).show();
    }

    // Show about placeholder
    private void showAbout() {
        startActivity(new Intent(this, AboutActivity.class));
    }


    // Update RAM usage
    private void updateRamUsage() {
        try {
            ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            if (activityManager != null) {
                ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
                activityManager.getMemoryInfo(memoryInfo);

                long availableMemory = memoryInfo.availMem / (1024 * 1024); // MB
                long totalMemory = memoryInfo.totalMem / (1024 * 1024); // MB
                int usedPercentage = (int) (((totalMemory - availableMemory) * 100) / totalMemory);

                // Update TextView
                String ramInfo = "Memory Available: " + availableMemory + "MB / Memory Total: " + totalMemory + "MB";
                binding.ram.setText(ramInfo);

                // Update ProgressBar
                binding.ramProgress.setProgress(usedPercentage);
            } else {
                binding.ram.setText("Failed to retrieve memory information.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            binding.ram.setText("Error retrieving memory info.");
        }
    }

    // Clear RAM using root
    private void clearRamWithRoot() {
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("am kill-all\n");  // Kill all background apps
            os.writeBytes("exit\n");
            os.flush();

            process.waitFor();
            Toast.makeText(this, "RAM Cleared!", Toast.LENGTH_SHORT).show();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error clearing RAM", Toast.LENGTH_SHORT).show();
        }
    }

    // Check if the device is rooted
    private boolean isDeviceRooted() {
        String buildTags = android.os.Build.TAGS;
        if (buildTags != null && buildTags.contains("test-keys")) {
            return true;
        }

        String[] paths = {"/system/xbin/su", "/system/bin/su", "/data/local/bin/su", "/data/local/su"};
        for (String path : paths) {
            if (new java.io.File(path).exists()) {
                return true;
            }
        }

        return false;
    }

    // Change status bar color
    private void setStatusBarColor(int colorResId) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, colorResId));
        }
    }

    // Toggle theme between Light and Dark
    private void showThemeDialog() {
    String[] themes = {"Follow System", "Light", "Dark"};
    int checkedItem = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES ? 2 :
            AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO ? 1 : 0;

    new MaterialAlertDialogBuilder(this)
            .setTitle("Choose Theme")
            .setSingleChoiceItems(themes, checkedItem, (dialog, which) -> {
                switch (which) {
                    case 0: // Follow System
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                        break;
                    case 1: // Light
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        break;
                    case 2: // Dark
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        break;
                }
                dialog.dismiss();
            })
            .setPositiveButton("OK", null)
            .show();
}


    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(ramUpdateRunnable);
    }
}
