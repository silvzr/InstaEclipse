package ps.reso.instaeclipse.utils.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.io.File;
import java.util.Objects;

import de.robv.android.xposed.XposedBridge;
import ps.reso.instaeclipse.mods.devops.config.ConfigManager;
import ps.reso.instaeclipse.mods.ghost.ui.GhostEmojiManager;
import ps.reso.instaeclipse.mods.ui.UIHookManager;
import ps.reso.instaeclipse.utils.core.SettingsManager;
import ps.reso.instaeclipse.utils.feature.FeatureFlags;
import ps.reso.instaeclipse.utils.ghost.GhostModeUtils;

public class DialogUtils {

    private static AlertDialog currentDialog;

    @SuppressLint("UseCompatLoadingForDrawables")
    public static void showEclipseOptionsDialog(Context context) {
        SettingsManager.init(context);
        Context themedContext = new ContextThemeWrapper(context, android.R.style.Theme_Material_Dialog_Alert);

        LinearLayout mainLayout = buildMainMenuLayout(themedContext);
        ScrollView scrollView = new ScrollView(themedContext);
        scrollView.addView(mainLayout);

        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
        }

        currentDialog = new AlertDialog.Builder(themedContext).setView(scrollView).setTitle(null).setCancelable(true).create();

        Objects.requireNonNull(currentDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        currentDialog.show();
    }

    public static void showSimpleDialog(Context context, String title, String message) {
        try {
            new AlertDialog.Builder(context).setTitle(title).setMessage(message).setPositiveButton("OK", null).show();
        } catch (Exception e) {
            // handle UI crash fallback
        }
    }

    @SuppressLint("SetTextI18n")
    private static LinearLayout buildMainMenuLayout(Context context) {
        LinearLayout mainLayout = new LinearLayout(context);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(dpToPx(context, 24), dpToPx(context, 24), dpToPx(context, 24), dpToPx(context, 16));

        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.parseColor("#262626"));
        background.setCornerRadius(dpToPx(context, 28));
        mainLayout.setBackground(background);

        // Title with M3 typography
        TextView title = new TextView(context);
        title.setText("InstaEclipse 🌘");
        title.setTextColor(Color.WHITE);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        title.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dpToPx(context, 8), 0, dpToPx(context, 16));
        mainLayout.addView(title);

        mainLayout.addView(createDivider(context));

        // Now building menu manually

        // 0 - Developer Options => OPEN PAGE
        mainLayout.addView(createClickableSection(context, "🎛 Developer Options", () -> showDevOptions(context)));

        // 1 - Ghost Mode Settings => OPEN PAGE
        mainLayout.addView(createClickableSection(context, "👻 Ghost Mode Settings", () -> showGhostOptions(context)));

        // 2 - Ad/Analytics Block => OPEN PAGE
        mainLayout.addView(createClickableSection(context, "🛡 Ad/Analytics Block", () -> showAdOptions(context)));

        // 3 - Distraction-Free Instagram => OPEN PAGE
        mainLayout.addView(createClickableSection(context, "🧘 Distraction-Free Instagram", () -> showDistractionOptions(context)));

        // 4 - Misc Features => OPEN PAGE
        mainLayout.addView(createClickableSection(context, "⚙ Misc Features", () -> showMiscOptions(context)));

        // 5 - About => OPEN PAGE
        mainLayout.addView(createClickableSection(context, "ℹ️ About", () -> showAboutDialog(context)));

        // 6 - Restart Instagram => OPEN PAGE
        mainLayout.addView(createClickableSection(context, "🔁 Restart App", () -> showRestartSection(context)));

        mainLayout.addView(createDivider(context));

        // Footer Credit with M3 labelMedium
        TextView footer = new TextView(context);
        footer.setText("@reso7200");
        footer.setTextColor(Color.GRAY);
        footer.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        footer.setPadding(0, dpToPx(context, 16), 0, dpToPx(context, 8));
        footer.setGravity(Gravity.CENTER_HORIZONTAL);
        mainLayout.addView(footer);

        // Close Button with ripple effect
        TextView closeButton = new TextView(context);
        closeButton.setText("Close");
        closeButton.setTextColor(Color.parseColor("#CF6679"));
        closeButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        closeButton.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        closeButton.setPadding(dpToPx(context, 16), dpToPx(context, 20), dpToPx(context, 16), dpToPx(context, 20));
        closeButton.setGravity(Gravity.CENTER);
        closeButton.setBackground(createRippleDrawable(Color.parseColor("#40FFFFFF")));

        closeButton.setOnClickListener(v -> {
            if (currentDialog != null) currentDialog.dismiss();
        });

        mainLayout.addView(createDivider(context));
        mainLayout.addView(closeButton);

        SettingsManager.saveAllFlags();

        Activity activity = UIHookManager.getCurrentActivity();
        if (activity != null) {
            GhostEmojiManager.addGhostEmojiNextToInbox(activity, GhostModeUtils.isGhostModeActive());
        }

        return mainLayout;
    }


    private static void showGhostQuickToggleOptions(Context context) {
        LinearLayout layout = createSwitchLayout(context);

        // Create switches for customizing what gets toggled
        MaterialSwitch[] toggleSwitches = new MaterialSwitch[]{createSwitch(context, "Include Hide Seen", FeatureFlags.quickToggleSeen), createSwitch(context, "Include Hide Typing", FeatureFlags.quickToggleTyping), createSwitch(context, "Include Disable Screenshot Detection", FeatureFlags.quickToggleScreenshot), createSwitch(context, "Include Hide View Once", FeatureFlags.quickToggleViewOnce), createSwitch(context, "Include Hide Story Seen", FeatureFlags.quickToggleStory), createSwitch(context, "Include Hide Live Seen", FeatureFlags.quickToggleLive)};

        // Create Enable/Disable All switch
        MaterialSwitch enableAllSwitch = createSwitch(context, "Enable/Disable All", areAllEnabled(toggleSwitches));

        // Master listener
        enableAllSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            for (MaterialSwitch s : toggleSwitches) {
                s.setChecked(isChecked);
            }
        });

        // Individual switch listeners (update master switch automatically)
        for (int i = 0; i < toggleSwitches.length; i++) {
            final int index = i;
            toggleSwitches[i].setOnCheckedChangeListener((buttonView, isChecked) -> {
                enableAllSwitch.setOnCheckedChangeListener(null);
                enableAllSwitch.setChecked(areAllEnabled(toggleSwitches));
                enableAllSwitch.setOnCheckedChangeListener((buttonView2, isChecked2) -> {
                    for (MaterialSwitch s2 : toggleSwitches) {
                        s2.setChecked(isChecked2);
                    }
                });

                // Update corresponding FeatureFlag instantly
                switch (index) {
                    case 0:
                        FeatureFlags.quickToggleSeen = isChecked;
                        break;
                    case 1:
                        FeatureFlags.quickToggleTyping = isChecked;
                        break;
                    case 2:
                        FeatureFlags.quickToggleScreenshot = isChecked;
                        break;
                    case 3:
                        FeatureFlags.quickToggleViewOnce = isChecked;
                        break;
                    case 4:
                        FeatureFlags.quickToggleStory = isChecked;
                        break;
                    case 5:
                        FeatureFlags.quickToggleLive = isChecked;
                        break;
                }

                // Save immediately
                SettingsManager.saveAllFlags();

                // Update ghost emoji immediately
                Activity activity = UIHookManager.getCurrentActivity();
                if (activity != null) {
                    GhostEmojiManager.addGhostEmojiNextToInbox(activity, GhostModeUtils.isGhostModeActive());
                }
            });
        }


        // Add views to layout
        layout.addView(createDivider(context)); // Divider above
        layout.addView(createEnableAllSwitch(context, enableAllSwitch)); // Styled enable all switch
        layout.addView(createDivider(context)); // Divider below

        for (MaterialSwitch s : toggleSwitches) {
            layout.addView(s);
        }

        // Show dialog
        showSectionDialog(context, "Customize Quick Toggle 🛠️", layout, () -> {
        });

    }


    private static View createDivider(Context context) {
        View divider = new View(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2);
        params.setMargins(0, 20, 0, 20);
        divider.setLayoutParams(params);
        divider.setBackgroundColor(Color.DKGRAY);
        return divider;
    }

    /**
     * Clears the application's cache and restarts it.
     * Works for any package name this module is running in.
     *
     * @param context The application context.
     */
    private static void restartApp(Context context) {
        try {
            String packageName = context.getPackageName();
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);

            if (intent != null) {
                clearAppCache(context); // Clear cache first
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                // Forcibly kill the current process to ensure a clean restart
                Runtime.getRuntime().exit(0);
            } else {
                Toast.makeText(context, "Could not find the app to restart.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            String packageName = context.getPackageName();
            XposedBridge.log("InstaEclipse: Restart failed for " + packageName + " - " + e.getMessage());
            Toast.makeText(context, "Restart failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Clears the cache directory for the current application.
     *
     * @param context The application context.
     */
    private static void clearAppCache(Context context) {
        try {
            File cacheDir = context.getCacheDir();
            if (cacheDir != null && cacheDir.isDirectory()) {
                deleteRecursive(cacheDir);
                XposedBridge.log("InstaEclipse: Cache cleared for " + context.getPackageName());
            } else {
                XposedBridge.log("InstaEclipse: Cache directory not found for " + context.getPackageName());
            }
        } catch (Exception e) {
            XposedBridge.log("InstaEclipse: Failed to clear cache for " + context.getPackageName() + " - " + e.getMessage());
        }
    }

    /**
     * Recursively deletes a file or directory.
     *
     * @param fileOrDirectory The file or directory to delete.
     */
    private static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            File[] children = fileOrDirectory.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursive(child);
                }
            }
        }
        // A direct result for a file or an empty directory
        fileOrDirectory.delete();
    }


    // ==== SECTIONS ====

    @SuppressLint("SetTextI18n")
    private static void showDevOptions(Context context) {
        LinearLayout layout = createSwitchLayout(context);

        // Developer Mode Switch
        MaterialSwitch devModeSwitch = createSwitch(context, "Enable Developer Mode", FeatureFlags.isDevEnabled);
        devModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            FeatureFlags.isDevEnabled = isChecked;
            SettingsManager.saveAllFlags();
        });

        layout.addView(devModeSwitch);
        layout.addView(createDivider(context));

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        btnParams.gravity = Gravity.CENTER_HORIZONTAL;
        btnParams.setMargins(0, dpToPx(context, 8), 0, dpToPx(context, 8));

        // 📥 Import Dev Config Button
        MaterialButton importButton = new MaterialButton(context);
        importButton.setText("📥 Import Dev Config");
        importButton.setLayoutParams(btnParams);
        importButton.setOnClickListener(v -> {
            Activity instagramActivity = UIHookManager.getCurrentActivity();
            if (instagramActivity != null && !instagramActivity.isFinishing()) {
                FeatureFlags.isImportingConfig = true;

                Intent importIntent = new Intent();
                importIntent.setComponent(new ComponentName("ps.reso.instaeclipse", "ps.reso.instaeclipse.mods.devops.config.JsonImportActivity"));
                importIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                try {
                    instagramActivity.startActivity(importIntent);
                } catch (Exception e) {
                    XposedBridge.log("InstaEclipse | ❌ Failed to start JsonImportActivity: " + e.getMessage());
                    showSimpleDialog(context, "Error", "Unable to open InstaEclipse UI.");
                }

            } else {
                showSimpleDialog(context, "Error", "Instagram is not open or ready.");
            }
        });

        layout.addView(importButton);


        // 📤 Export Dev Config Button
        MaterialButton exportButton = new MaterialButton(context);
        exportButton.setText("📤 Export Dev Config");
        exportButton.setLayoutParams(btnParams);
        exportButton.setOnClickListener(v -> {
            FeatureFlags.isExportingConfig = true;
            Activity instagramActivity = UIHookManager.getCurrentActivity();
            if (instagramActivity != null && !instagramActivity.isFinishing()) {
                ConfigManager.exportCurrentDevConfig(instagramActivity);

                // Launch InstaEclipse export screen
                Intent exportIntent = new Intent();
                exportIntent.setComponent(new ComponentName("ps.reso.instaeclipse", "ps.reso.instaeclipse.mods.devops.config.JsonExportActivity"));
                exportIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                try {
                    instagramActivity.startActivity(exportIntent);
                } catch (Exception e) {
                    showSimpleDialog(context, "Error", "Unable to open InstaEclipse UI.");
                }

            } else {
                showSimpleDialog(context, "Error", "Instagram is not open or ready.");
            }
        });

        layout.addView(exportButton);

        // Save current dev mode flag when dialog is closed
        showSectionDialog(context, "Developer Options 🎛", layout, SettingsManager::saveAllFlags);
    }

    private static void showGhostOptions(Context context) {
        LinearLayout layout = createSwitchLayout(context);

        MaterialSwitch[] switches = new MaterialSwitch[]{createSwitch(context, "Hide Seen", FeatureFlags.isGhostSeen), createSwitch(context, "Hide Typing", FeatureFlags.isGhostTyping), createSwitch(context, "Disable Screenshot Detection", FeatureFlags.isGhostScreenshot), createSwitch(context, "Hide View Once", FeatureFlags.isGhostViewOnce), createSwitch(context, "Hide Story Seen", FeatureFlags.isGhostStory), createSwitch(context, "Hide Live Seen", FeatureFlags.isGhostLive)};

        layout.addView(createClickableSection(context, "🛠 Customize Quick Toggle", () -> showGhostQuickToggleOptions(context)));

        MaterialSwitch enableAllSwitch = createSwitch(context, "Enable/Disable All", areAllEnabled(switches));

        enableAllSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            for (MaterialSwitch s : switches) {
                s.setChecked(isChecked);
            }
        });

        for (int i = 0; i < switches.length; i++) {
            final int index = i;
            switches[i].setOnCheckedChangeListener((buttonView, isChecked) -> {
                enableAllSwitch.setOnCheckedChangeListener(null);
                enableAllSwitch.setChecked(areAllEnabled(switches));
                enableAllSwitch.setOnCheckedChangeListener((buttonView2, isChecked2) -> {
                    for (MaterialSwitch s2 : switches) {
                        s2.setChecked(isChecked2);
                    }
                });

                // Set FeatureFlag immediately
                switch (index) {
                    case 0:
                        FeatureFlags.isGhostSeen = isChecked;
                        break;
                    case 1:
                        FeatureFlags.isGhostTyping = isChecked;
                        break;
                    case 2:
                        FeatureFlags.isGhostScreenshot = isChecked;
                        break;
                    case 3:
                        FeatureFlags.isGhostViewOnce = isChecked;
                        break;
                    case 4:
                        FeatureFlags.isGhostStory = isChecked;
                        break;
                    case 5:
                        FeatureFlags.isGhostLive = isChecked;
                        break;
                }

                // Save immediately
                SettingsManager.saveAllFlags();

                // Update ghost emoji immediately
                Activity activity = UIHookManager.getCurrentActivity();
                if (activity != null) {
                    GhostEmojiManager.addGhostEmojiNextToInbox(activity, GhostModeUtils.isGhostModeActive());
                }
            });
        }

        layout.addView(createDivider(context));
        layout.addView(createEnableAllSwitch(context, enableAllSwitch));
        layout.addView(createDivider(context));

        for (MaterialSwitch s : switches) {
            layout.addView(s);
        }

        showSectionDialog(context, "Ghost Mode 👻", layout, () -> {
            // No need to set FeatureFlags here anymore because handled instantly
        });
    }


    private static void showAdOptions(Context context) {
        LinearLayout layout = createSwitchLayout(context);

        // Create switches
        MaterialSwitch adBlock = createSwitch(context, "Block Ads", FeatureFlags.isAdBlockEnabled);

        MaterialSwitch analytics = createSwitch(context, "Block Analytics", FeatureFlags.isAnalyticsBlocked);

        MaterialSwitch trackingLinks = createSwitch(context, "Disable Tracking Links", FeatureFlags.disableTrackingLinks);

        MaterialSwitch[] switches = new MaterialSwitch[]{adBlock, analytics, trackingLinks};

        // Create Enable/Disable All switch
        MaterialSwitch enableAllSwitch = createSwitch(context, "Enable/Disable All", areAllEnabled(switches));

        // Master listener
        enableAllSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            for (MaterialSwitch s : switches) {
                s.setChecked(isChecked);
            }
        });

        // Individual switch listeners
        for (int i = 0; i < switches.length; i++) {
            final int index = i;
            switches[i].setOnCheckedChangeListener((buttonView, isChecked) -> {
                enableAllSwitch.setOnCheckedChangeListener(null);
                enableAllSwitch.setChecked(areAllEnabled(switches));
                enableAllSwitch.setOnCheckedChangeListener((buttonView2, isChecked2) -> {
                    for (MaterialSwitch s2 : switches) {
                        s2.setChecked(isChecked2);
                    }
                });

                // Update FeatureFlag immediately
                if (index == 0) FeatureFlags.isAdBlockEnabled = isChecked;
                if (index == 1) FeatureFlags.isAnalyticsBlocked = isChecked;
                if (index == 2) FeatureFlags.disableTrackingLinks = isChecked;

                // Save immediately
                SettingsManager.saveAllFlags();
            });
        }


        // Add views
        layout.addView(createDivider(context));
        layout.addView(createEnableAllSwitch(context, enableAllSwitch));
        layout.addView(createDivider(context));

        for (MaterialSwitch s : switches) {
            layout.addView(s);
        }

        // Show the dialog
        showSectionDialog(context, "Ad/Analytics Block 🛡️", layout, () -> {
        });
    }


    private static void showDistractionOptions(Context context) {
        LinearLayout layout = createSwitchLayout(context);

        // Child switches
        MaterialSwitch extremeModeSwitch = createSwitch(context, "Extreme Mode 🔒 (Irreversible until reinstall)", FeatureFlags.isExtremeMode);
        MaterialSwitch disableStoriesSwitch = createSwitch(context, "Disable Stories", FeatureFlags.disableStories);
        MaterialSwitch disableFeedSwitch = createSwitch(context, "Disable Feed", FeatureFlags.disableFeed);
        MaterialSwitch disableReelsSwitch = createSwitch(context, "Disable Reels", FeatureFlags.disableReels);
        MaterialSwitch onlyInDMSwitch = createSwitch(context, "Disable Reels Except in DMs", FeatureFlags.disableReelsExceptDM);
        MaterialSwitch disableExploreSwitch = createSwitch(context, "Disable Explore", FeatureFlags.disableExplore);
        MaterialSwitch disableCommentsSwitch = createSwitch(context, "Disable Comments", FeatureFlags.disableComments);

        MaterialSwitch[] switches = new MaterialSwitch[]{disableStoriesSwitch, disableFeedSwitch, disableReelsSwitch, onlyInDMSwitch, disableExploreSwitch, disableCommentsSwitch};


        // Enable/Disable All
        MaterialSwitch enableAllSwitch = createSwitch(context, "Enable/Disable All", areAllEnabled(switches));

        if (FeatureFlags.isExtremeMode) {
            disableAllSwitches(switches, enableAllSwitch, onlyInDMSwitch);
            extremeModeSwitch.setChecked(true);
            extremeModeSwitch.setEnabled(false);
        }

        extremeModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Activate Extreme Mode?");
                builder.setMessage("Once activated, you cannot disable Distraction-Free Mode until you reinstall the app. Continue?");
                builder.setPositiveButton("Yes", (dialog, which) -> {
                    FeatureFlags.isExtremeMode = true;
                    FeatureFlags.isDistractionFree = true;

                    // Save user’s current selections before freezing them
                    FeatureFlags.disableStories = disableStoriesSwitch.isChecked();
                    FeatureFlags.disableFeed = disableFeedSwitch.isChecked();
                    FeatureFlags.disableReels = disableReelsSwitch.isChecked();
                    FeatureFlags.disableReelsExceptDM = onlyInDMSwitch.isChecked();
                    FeatureFlags.disableExplore = disableExploreSwitch.isChecked();
                    FeatureFlags.disableComments = disableCommentsSwitch.isChecked();
                    SettingsManager.saveAllFlags();

                    // Disable all UI switches to lock them
                    disableAllSwitches(switches, enableAllSwitch, onlyInDMSwitch);
                    extremeModeSwitch.setEnabled(false);
                });
                builder.setNegativeButton("Cancel", (dialog, which) -> extremeModeSwitch.setChecked(false));
                builder.show();
            }
        });


        // Master switch listener
        enableAllSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            for (MaterialSwitch s : switches) {
                s.setChecked(isChecked);
                s.setEnabled(true);
            }
            if (!isChecked) {
                onlyInDMSwitch.setChecked(false);
                onlyInDMSwitch.setEnabled(false);
            }
        });

        // Parent-child logic for Reels
        disableReelsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            onlyInDMSwitch.setEnabled(isChecked);
            if (!isChecked) {
                onlyInDMSwitch.setChecked(false); // turn off child immediately
                onlyInDMSwitch.setEnabled(false);
            }
            updateMasterSwitch(enableAllSwitch, switches, disableReelsSwitch, onlyInDMSwitch);
            SettingsManager.saveAllFlags();
        });

        // Child logic for "Except in DMs"
        onlyInDMSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !disableReelsSwitch.isChecked()) {
                // Auto-enable parent if user enables child
                disableReelsSwitch.setChecked(true);
            }
            updateMasterSwitch(enableAllSwitch, switches, disableReelsSwitch, onlyInDMSwitch);
            SettingsManager.saveAllFlags();
        });

        // All other switches
        for (MaterialSwitch s : new MaterialSwitch[]{disableStoriesSwitch, disableFeedSwitch, disableExploreSwitch, disableCommentsSwitch}) {
            s.setOnCheckedChangeListener((buttonView, isChecked) -> {
                updateMasterSwitch(enableAllSwitch, switches, disableReelsSwitch, onlyInDMSwitch);
                SettingsManager.saveAllFlags();
            });
        }

        // Init "Except in DMs" state
        onlyInDMSwitch.setEnabled(disableReelsSwitch.isChecked());

        // Layout building
        layout.addView(extremeModeSwitch);
        layout.addView(createDivider(context));
        layout.addView(createEnableAllSwitch(context, enableAllSwitch));
        layout.addView(createDivider(context));

        for (MaterialSwitch s : switches) {
            layout.addView(s);
        }

        showSectionDialog(context, "Distraction-Free Instagram 🧘", layout, () -> {
            FeatureFlags.disableStories = disableStoriesSwitch.isChecked();
            FeatureFlags.disableFeed = disableFeedSwitch.isChecked();
            FeatureFlags.disableReels = disableReelsSwitch.isChecked();
            FeatureFlags.disableReelsExceptDM = onlyInDMSwitch.isChecked();
            FeatureFlags.disableExplore = disableExploreSwitch.isChecked();
            FeatureFlags.disableComments = disableCommentsSwitch.isChecked();
        });

        SettingsManager.saveAllFlags();
    }

    private static void disableAllSwitches(MaterialSwitch[] switches, MaterialSwitch master, MaterialSwitch onlyInDMSwitch) {

        for (MaterialSwitch s : switches) {
            if (s == onlyInDMSwitch) {
                // Special rule for onlyInDM
                s.setEnabled(s.isChecked()); // editable only if it was checked
            } else {
                // Normal switches: lock if checked, editable if unchecked
                s.setEnabled(!s.isChecked());
            }
        }

        // Master switch always frozen ON
        master.setEnabled(false);
    }


    private static void updateMasterSwitch(MaterialSwitch enableAllSwitch, MaterialSwitch[] switches, MaterialSwitch disableReelsSwitch, MaterialSwitch onlyInDMSwitch) {
        enableAllSwitch.setOnCheckedChangeListener(null);
        enableAllSwitch.setChecked(areAllEnabled(switches));
        enableAllSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            for (MaterialSwitch s : switches) {
                s.setChecked(isChecked);
            }
            onlyInDMSwitch.setEnabled(disableReelsSwitch.isChecked());
        });
    }


    private static void showMiscOptions(Context context) {
        LinearLayout layout = createSwitchLayout(context);

        // Create all child switches
        MaterialSwitch[] switches = new MaterialSwitch[]{createSwitch(context, "Disable Story Auto-Swipe", FeatureFlags.disableStoryFlipping), createSwitch(context, "Disable Video Autoplay", FeatureFlags.disableVideoAutoPlay), createSwitch(context, "Show Follower Toast", FeatureFlags.showFollowerToast), createSwitch(context, "Show Feature Toasts", FeatureFlags.showFeatureToasts)};

        // Create Enable/Disable All switch
        MaterialSwitch enableAllSwitch = createSwitch(context, "Enable/Disable All", areAllEnabled(switches));

        enableAllSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            for (MaterialSwitch s : switches) {
                s.setChecked(isChecked);
            }
        });

        for (int i = 0; i < switches.length; i++) {
            final int index = i;
            switches[i].setOnCheckedChangeListener((buttonView, isChecked) -> {
                enableAllSwitch.setOnCheckedChangeListener(null);
                enableAllSwitch.setChecked(areAllEnabled(switches));
                enableAllSwitch.setOnCheckedChangeListener((buttonView2, isChecked2) -> {
                    for (MaterialSwitch s2 : switches) {
                        s2.setChecked(isChecked2);
                    }
                });

                // Update FeatureFlags
                switch (index) {
                    case 0:
                        FeatureFlags.disableStoryFlipping = isChecked;
                        break;
                    case 1:
                        FeatureFlags.disableVideoAutoPlay = isChecked;
                        break;
                    case 2:
                        FeatureFlags.showFollowerToast = isChecked;
                        break;
                    case 3:
                        FeatureFlags.showFeatureToasts = isChecked;
                        break;
                }

                SettingsManager.saveAllFlags();
            });
        }

        // Add views to layout
        layout.addView(createDivider(context));
        layout.addView(createEnableAllSwitch(context, enableAllSwitch));
        layout.addView(createDivider(context));

        for (MaterialSwitch s : switches) {
            layout.addView(s);
        }

        // Show dialog
        showSectionDialog(context, "Miscellaneous ⚙️", layout, () -> {
        });
    }


    @SuppressLint("SetTextI18n")
    private static void showAboutDialog(Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dpToPx(context, 24), dpToPx(context, 16), dpToPx(context, 24), dpToPx(context, 8));
        layout.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView title = new TextView(context);
        title.setText("InstaEclipse 🌘");
        title.setTextColor(Color.WHITE);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        title.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, dpToPx(context, 8));

        TextView creator = new TextView(context);
        creator.setText("Created by @reso7200");
        creator.setTextColor(Color.LTGRAY);
        creator.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        creator.setGravity(Gravity.CENTER);
        creator.setPadding(0, 0, 0, dpToPx(context, 16));

        MaterialButton githubButton = new MaterialButton(context);
        githubButton.setText("🌐 GitHub Repo");
        githubButton.setTextColor(Color.WHITE);
        githubButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#3F51B5")));
        githubButton.setPadding(dpToPx(context, 16), dpToPx(context, 8), dpToPx(context, 16), dpToPx(context, 8));

        LinearLayout.LayoutParams githubParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        githubParams.gravity = Gravity.CENTER_HORIZONTAL;
        githubButton.setLayoutParams(githubParams);


        githubButton.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://github.com/ReSo7200/InstaEclipse"));
            browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(browserIntent);
        });

        layout.addView(title);
        layout.addView(creator);
        layout.addView(githubButton);

        showSectionDialog(context, "About", layout, () -> {
        });
    }

    @SuppressLint("SetTextI18n")
    private static void showRestartSection(Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dpToPx(context, 24), dpToPx(context, 16), dpToPx(context, 24), dpToPx(context, 16));
        layout.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView message = new TextView(context);
        message.setText("⚠️ Clear app cache and restart?");
        message.setTextColor(Color.WHITE);
        message.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        message.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        message.setGravity(Gravity.CENTER);
        message.setPadding(0, 0, 0, dpToPx(context, 16));

        MaterialButton restartButton = new MaterialButton(context);
        restartButton.setText("🔁 Restart Now");
        restartButton.setTextColor(Color.WHITE);
        restartButton.setPadding(dpToPx(context, 16), dpToPx(context, 8), dpToPx(context, 16), dpToPx(context, 8));

        restartButton.setOnClickListener(v -> restartApp(context));

        layout.addView(message);
        layout.addView(restartButton);

        showSectionDialog(context, "Restart App", layout, () -> {
        });
    }


    // ==== HELPERS ====

    @SuppressLint("SetTextI18n")
    private static void showSectionDialog(Context context, String title, LinearLayout contentLayout, Runnable onSave) {
        if (currentDialog != null) currentDialog.dismiss();

        // Wrap in a card-style layout
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dpToPx(context, 24), dpToPx(context, 24), dpToPx(context, 24), dpToPx(context, 16));

        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.parseColor("#262626"));
        background.setCornerRadius(dpToPx(context, 28));
        container.setBackground(background);

        // Section Title
        TextView titleView = new TextView(context);
        titleView.setText(title);
        titleView.setTextColor(Color.WHITE);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        titleView.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        titleView.setGravity(Gravity.CENTER);
        titleView.setPadding(0, dpToPx(context, 16), 0, dpToPx(context, 16));
        container.addView(titleView);

        container.addView(createDivider(context));
        container.addView(contentLayout);
        container.addView(createDivider(context));

        // Footer button (M3 style back)
        TextView backBtn = new TextView(context);
        backBtn.setText("← Back");
        backBtn.setTextColor(Color.WHITE);
        backBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        backBtn.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        backBtn.setGravity(Gravity.CENTER);
        backBtn.setBackground(createRippleDrawable(Color.parseColor("#40FFFFFF")));

        backBtn.setPadding(dpToPx(context, 16), dpToPx(context, 12), dpToPx(context, 16), dpToPx(context, 12));
        backBtn.setOnClickListener(v -> {
            onSave.run();
            SettingsManager.saveAllFlags();
            showEclipseOptionsDialog(context);
        });

        container.addView(backBtn);

        ScrollView scrollView = new ScrollView(context);
        scrollView.addView(container);

        currentDialog = new AlertDialog.Builder(context).setView(scrollView).setCancelable(true).create();

        Objects.requireNonNull(currentDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        currentDialog.show();
    }


    private static LinearLayout createSwitchLayout(Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dpToPx(context, 16), dpToPx(context, 12), dpToPx(context, 16), dpToPx(context, 12));
        return layout;
    }

    private static MaterialSwitch createSwitch(Context context, String label, boolean defaultState) {
        MaterialSwitch toggle = new MaterialSwitch(context);
        toggle.setText(label);
        toggle.setChecked(defaultState);
        toggle.setPadding(dpToPx(context, 16), dpToPx(context, 12), dpToPx(context, 16), dpToPx(context, 12));
        toggle.setTextColor(Color.WHITE);
        toggle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        toggle.setThumbTintList(createThumbColor());
        toggle.setTrackTintList(createTrackColor());
        return toggle;
    }

    private static ColorStateList createThumbColor() {
        return new ColorStateList(new int[][]{
                new int[]{-android.R.attr.state_enabled},
                new int[]{android.R.attr.state_checked},
                new int[]{-android.R.attr.state_checked}
        }, new int[]{
                Color.parseColor("#555555"),
                Color.parseColor("#448AFF"),
                Color.parseColor("#FFFFFF")
        });
    }

    private static ColorStateList createTrackColor() {
        return new ColorStateList(new int[][]{
                new int[]{-android.R.attr.state_enabled},
                new int[]{android.R.attr.state_checked},
                new int[]{-android.R.attr.state_checked}
        }, new int[]{
                Color.parseColor("#777777"),
                Color.parseColor("#1C4C78"),
                Color.parseColor("#CFD8DC")
        });
    }

    private static View createClickableSection(Context context, String label, Runnable onClick) {
        TextView section = new TextView(context);
        section.setText(label);
        section.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        section.setTextColor(Color.WHITE);
        section.setPadding(dpToPx(context, 16), dpToPx(context, 16), dpToPx(context, 16), dpToPx(context, 16));
        section.setBackground(createRippleDrawable(Color.parseColor("#40FFFFFF")));
        section.setOnClickListener(v -> onClick.run());
        return section;
    }

    private static LinearLayout createEnableAllSwitch(Context context, MaterialSwitch enableAllSwitch) {
        enableAllSwitch.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        enableAllSwitch.setTextColor(Color.WHITE);
        enableAllSwitch.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        enableAllSwitch.setPadding(dpToPx(context, 16), dpToPx(context, 16), dpToPx(context, 16), dpToPx(context, 16));

        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dpToPx(context, 8), dpToPx(context, 8), dpToPx(context, 8), dpToPx(context, 8));

        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.parseColor("#333333"));
        background.setCornerRadius(dpToPx(context, 16));
        container.setBackground(background);

        container.addView(enableAllSwitch);

        return container;
    }

    private static boolean areAllEnabled(MaterialSwitch[] switches) {
        for (MaterialSwitch s : switches) {
            if (!s.isChecked()) return false;
        }
        return true;
    }

    private static RippleDrawable createRippleDrawable(int rippleColor) {
        return new RippleDrawable(
                ColorStateList.valueOf(rippleColor),
                null,
                new ColorDrawable(Color.WHITE)
        );
    }

    private static int dpToPx(Context context, int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.getResources().getDisplayMetrics()
        );
    }

}
