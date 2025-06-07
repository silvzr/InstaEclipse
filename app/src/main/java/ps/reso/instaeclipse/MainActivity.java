package ps.reso.instaeclipse;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import ps.reso.instaeclipse.fragments.HelpFragment;
import ps.reso.instaeclipse.fragments.HomeFragment;
import ps.reso.instaeclipse.utils.version.VersionCheckUtility;

public class MainActivity extends AppCompatActivity {

    public static boolean hasRootAccess = checkForRootAccess();

    public static boolean isModuleActive() {
        return false;
    }

    private static boolean checkForRootAccess() {
        try {
            Process process = Runtime.getRuntime().exec("su -c echo success");
            int exitCode = process.waitFor();

            return exitCode == 0; // Root access granted
        } catch (Exception e) {
            // Root access denied
            return false; // No root access or error occurred
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        VersionCheckUtility.checkForUpdates(this);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.top_app_bar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);

        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);

        // Load the HomeFragment by default
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }

        // Select Home by default in the navbar
        bottomNavigation.setSelectedItemId(R.id.nav_home);

        // Handle bottom navigation item clicks
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (item.getItemId() == R.id.nav_help) {
                selectedFragment = new HelpFragment();
            }


            if (selectedFragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            // If there are fragments in the back stack, pop the stack
            getSupportFragmentManager().popBackStack();
        } else {
            // Otherwise, handle the default back button behavior
            super.onBackPressed();
        }
    }

}
