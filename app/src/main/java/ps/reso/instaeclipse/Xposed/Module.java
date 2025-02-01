package ps.reso.instaeclipse.Xposed;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import android.annotation.SuppressLint;
import android.app.AndroidAppHelper;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import org.luckypray.dexkit.DexKitBridge;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import ps.reso.instaeclipse.mods.DevOptionsEnable;
import ps.reso.instaeclipse.mods.GhostModeDM;
import ps.reso.instaeclipse.mods.Interceptor;
import ps.reso.instaeclipse.mods.misc.StoryFlipping;


@SuppressLint("UnsafeDynamicallyLoadedCode")
public class Module implements IXposedHookLoadPackage, IXposedHookZygoteInit {
    private static final String IG_PACKAGE_NAME = "com.instagram.android";
    private static final String MY_PACKAGE_NAME = "ps.reso.instaeclipse";
    public static DexKitBridge dexKitBridge;
    public static ClassLoader hostClassLoader;
    private static String moduleSourceDir;
    private static String moduleLibDir;
    List<Predicate<URI>> uriConditions = new ArrayList<>();

    // for dev usage
    public static void showToast(final String text) {
        new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(AndroidAppHelper.currentApplication().getApplicationContext(), text, Toast.LENGTH_LONG).show());
    }

    @Override
    public void initZygote(StartupParam startupParam) {
        XposedBridge.log("(InstaEclipse): Zygote initialized.");
        // Save the module's APK path
        moduleSourceDir = startupParam.modulePath;
        String abi = Build.SUPPORTED_ABIS[0]; // Primary ABI
        abi = abi.replaceAll("-v\\d+a$", ""); // Regex to remove version suffix
        moduleLibDir = moduleSourceDir.substring(0, moduleSourceDir.lastIndexOf("/")) + "/lib/" + abi;


        // XposedBridge.log("InstaEclipse | Module paths initialized:" + "\nSourceDir: " + moduleSourceDir + "\nLibDir: " + moduleLibDir);
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {

        XposedBridge.log("(InstaEclipse): Loaded package: " + lpparam.packageName);

        // Hook into your module
        if (lpparam.packageName.equals(MY_PACKAGE_NAME)) {
            try {
                if (dexKitBridge == null) {
                    // Load the .so file from your module
                    System.load(moduleLibDir + "/libdexkit.so");
                    XposedBridge.log("libdexkit.so loaded successfully.");

                    // Initialize DexKitBridge with your module's APK (for module-specific tasks, if needed)
                    dexKitBridge = DexKitBridge.create(moduleSourceDir);
                    // XposedBridge.log("DexKitBridge initialized for InstaEclipse.");
                }

                // Hook your module
                hookOwnModule(lpparam);

            } catch (Exception e) {
                XposedBridge.log("(InstaEclipse): Failed to initialize DexKitBridge for InstaEclipse: " + e.getMessage());
            }
        }

        // Hook into Instagram
        if (lpparam.packageName.equals(IG_PACKAGE_NAME)) {
            try {
                if (dexKitBridge == null) {
                    // Load the .so file from your module (if not already loaded)
                    System.load(moduleLibDir + "/libdexkit.so");
                    // XposedBridge.log("libdexkit.so loaded successfully.");

                    // Initialize DexKitBridge with Instagram's APK
                    dexKitBridge = DexKitBridge.create(lpparam.appInfo.sourceDir);
                    // XposedBridge.log("DexKitBridge initialized with Instagram's APK: " + lpparam.appInfo.sourceDir);
                }

                // Use Instagram's ClassLoader
                hostClassLoader = lpparam.classLoader;

                // Call the method to hook Instagram
                hookInstagram(lpparam);

            } catch (Exception e) {
                XposedBridge.log("(InstaEclipse): Failed to initialize DexKitBridge for Instagram: " + e.getMessage());
            }
        }
    }

    private void hookOwnModule(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            findAndHookMethod(MY_PACKAGE_NAME + ".MainActivity", lpparam.classLoader, "isModuleActive", XC_MethodReplacement.returnConstant(true));
            // XposedBridge.log("InstaEclipse | Successfully hooked isModuleActive().");
        } catch (Exception e) {
            XposedBridge.log("(InstaEclipse): Failed to hook MainActivity: " + e.getMessage());
        }
    }

    private void hookInstagram(XC_LoadPackage.LoadPackageParam lpparam) {

        try {
            uriConditions.clear();
            XposedBridge.log("(InstaEclipse): Instagram package detected. Hooking...");

            Interceptor interceptor = new Interceptor();

            DevOptionsEnable devOptionsEnable = new DevOptionsEnable();
            devOptionsEnable.handleDevOptions(dexKitBridge);


            GhostModeDM ghostModeDM = new GhostModeDM();
            ghostModeDM.handleGhostMode(lpparam);

            uriConditions.add(uri -> uri.getPath().contains("/api/v2/media/seen/"));


            uriConditions.add(uri -> uri.getPath().contains("/heartbeat_and_get_viewer_count/"));

            uriConditions.add(uri -> uri.getPath().contains("profile_ads/get_profile_ads/"));
            uriConditions.add(uri -> uri.getPath().contains("/ads/"));
            uriConditions.add(uri -> uri.getPath().contains("/feed/injected_reels_media/"));
            uriConditions.add(uri -> uri.getPath().equals("/api/v1/ads/graphql/"));


            uriConditions.add(uri -> uri.getHost().contains("graph.instagram.com"));
            uriConditions.add(uri -> uri.getHost().contains("graph.facebook.com"));
            uriConditions.add(uri -> uri.getPath().contains("/logging_client_events"));
            uriConditions.add(uri -> uri.getPath().endsWith("/activities"));

            StoryFlipping storyFlipping = new StoryFlipping();
            storyFlipping.handleStoryFlippingDisable(dexKitBridge);


            // Pass the dynamically rebuilt conditions to the interceptor
            if (!uriConditions.isEmpty()) {
                interceptor.handleInterceptor(lpparam, uriConditions);
            }


        } catch (Exception e) {
            XposedBridge.log("(InstaEclipse): Failed to hook Instagram: " + e.getMessage());
        }
    }
}