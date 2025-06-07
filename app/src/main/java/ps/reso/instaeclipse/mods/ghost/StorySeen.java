package ps.reso.instaeclipse.mods.ghost;

import org.luckypray.dexkit.DexKitBridge;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.ClassDataList;
import org.luckypray.dexkit.result.MethodData;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import ps.reso.instaeclipse.Xposed.Module;
import ps.reso.instaeclipse.utils.feature.FeatureFlags;
import ps.reso.instaeclipse.utils.feature.FeatureStatusTracker;

public class StorySeen {

    public void handleStorySeenBlock(DexKitBridge bridge) {
        try {
            // Step 1: Find methods containing the string "media/seen/"
            List<MethodData> methods = bridge.findMethod(FindMethod.create()
                    .matcher(MethodMatcher.create().usingStrings("media/seen/")));

            if (methods.isEmpty()) {
                XposedBridge.log("(InstaEclipse | StoryBlock): ❌ No methods found containing 'media/seen/'");
                return;
            }

            for (MethodData method : methods) {
                ClassDataList paramTypes = method.getParamTypes();
                String returnType = String.valueOf(method.getReturnType());

                Method reflectMethod;
                try {
                    reflectMethod = method.getMethodInstance(Module.hostClassLoader);
                } catch (Throwable e) {
                    continue; // Skip if cannot reflect
                }

                int modifiers = reflectMethod.getModifiers();

                // Match: final void method with no params
                if (Modifier.isFinal(modifiers) &&
                        returnType.contains("void") &&
                        paramTypes.size() == 0) {

                    try {
                        XposedBridge.hookMethod(reflectMethod, new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) {
                                if (FeatureFlags.isGhostStory) {
                                    param.setResult(null); // Block if GhostStory is enabled
                                }
                            }
                        });

                        XposedBridge.log("(InstaEclipse | StoryBlock): ✅ Hooked (dynamic check): " +
                                method.getClassName() + "." + method.getName());
                        FeatureStatusTracker.setHooked("GhostStories");
                        return;

                    } catch (Throwable e) {
                        XposedBridge.log("(InstaEclipse | StoryBlock): ❌ Hook error: " + e.getMessage());
                    }
                }
            }

        } catch (Throwable t) {
            XposedBridge.log("(InstaEclipse | StoryBlock): ❌ Exception: " + t.getMessage());
        }
    }
}
