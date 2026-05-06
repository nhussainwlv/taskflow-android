package com.taskflow.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.taskflow.R;
import com.taskflow.databinding.ActivityMainBinding;
import com.taskflow.util.PreferencesManager;

/**
 * MainActivity - Single activity for the entire app.
 *
 * Uses Navigation Component for fragment management and
 * implements edge-to-edge design with proper inset handling.
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;

    private ConnectivityManager.NetworkCallback networkCallback;
    private boolean networkCallbackRegistered;
    private boolean networkBannerDismissed;
    private boolean isOffline;
    private boolean offlineDialogShownForCurrentOutage;

    @Override
    protected void attachBaseContext(Context newBase) {
        float mult = PreferencesManager.getInstance(newBase).getFontScaleMultiplier();
        Configuration config = new Configuration(newBase.getResources().getConfiguration());
        config.fontScale = config.fontScale * mult;
        super.attachBaseContext(newBase.createConfigurationContext(config));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        if (PreferencesManager.getInstance(this).isReduceMotionEnabled()) {
            setTheme(R.style.Theme_TaskFlow_Calm);
        }
        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupWindowInsets();
        setupNavigation();
        setupFab();
        setupNetworkBanner();
        handleIntent(getIntent());
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.coordinatorLayout, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

            v.setPadding(insets.left, insets.top, insets.right, 0);

            binding.bottomNavigation.setPadding(0, 0, 0, insets.bottom);

            return WindowInsetsCompat.CONSUMED;
        });
    }

    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

            NavigationUI.setupWithNavController(binding.bottomNavigation, navController);

            navController.addOnDestinationChangedListener(this::onDestinationChanged);
        }
    }

    private void onDestinationChanged(NavController controller, NavDestination destination, Bundle arguments) {
        int destId = destination.getId();

        if (destId == R.id.searchFragment || destId == R.id.settingsFragment) {
            binding.bottomNavigation.setVisibility(View.GONE);
            binding.fabQuickAdd.hide();
        } else if (destId == R.id.taskDetailBottomSheet || destId == R.id.quickAddBottomSheet) {
            // Bottom sheets — keep FAB / nav visibility
        } else {
            binding.bottomNavigation.setVisibility(View.VISIBLE);
            binding.fabQuickAdd.show();
        }
    }

    private void setupFab() {
        binding.fabQuickAdd.setOnClickListener(v -> {
            if (navController != null) {
                navController.navigate(R.id.quickAddBottomSheet);
            }
        });
    }

    private void setupNetworkBanner() {
        binding.networkBannerDismiss.setOnClickListener(v -> {
            networkBannerDismissed = true;
            binding.networkBanner.setVisibility(View.GONE);
        });

        ConnectivityManager cm = getSystemService(ConnectivityManager.class);
        if (cm == null) {
            return;
        }

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                runOnUiThread(() -> applyNetworkBannerState(false));
            }

            @Override
            public void onLost(Network network) {
                runOnUiThread(() -> applyNetworkBannerState(true));
            }
        };

        cm.registerDefaultNetworkCallback(networkCallback);
        networkCallbackRegistered = true;

        applyNetworkBannerState(cm.getActiveNetwork() == null);
    }

    private void applyNetworkBannerState(boolean offline) {
        if (binding == null) return;
        boolean justWentOffline = offline && !isOffline;
        if (justWentOffline) {
            showOfflineDialog();
        }
        if (!offline) {
            offlineDialogShownForCurrentOutage = false;
        }
        isOffline = offline;
        if (!offline) {
            networkBannerDismissed = false;
        }
        boolean visible = offline && !networkBannerDismissed;
        binding.networkBanner.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void showOfflineDialog() {
        if (offlineDialogShownForCurrentOutage || isFinishing() || isDestroyed()) {
            return;
        }
        offlineDialogShownForCurrentOutage = true;

        new AlertDialog.Builder(this)
                .setTitle(R.string.error_network_title)
                .setMessage(R.string.network_offline_message)
                .setCancelable(true)
                .setPositiveButton(R.string.action_close, null)
                .show();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent == null || navController == null) return;

        // Launcher tap should always open TaskFlow on Home.
        if (Intent.ACTION_MAIN.equals(intent.getAction())
                && intent.hasCategory(Intent.CATEGORY_LAUNCHER)) {
            navController.navigate(R.id.homeFragment);
            return;
        }

        navController.handleDeepLink(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    public NavController getNavController() {
        return navController;
    }

    public BottomNavigationView getBottomNavigationView() {
        return binding.bottomNavigation;
    }

    public FloatingActionButton getFab() {
        return binding.fabQuickAdd;
    }

    @Override
    protected void onDestroy() {
        if (networkCallbackRegistered && networkCallback != null) {
            ConnectivityManager cm = getSystemService(ConnectivityManager.class);
            if (cm != null) {
                try {
                    cm.unregisterNetworkCallback(networkCallback);
                } catch (RuntimeException ignored) {
                    // Already unregistered on some OEM builds
                }
            }
            networkCallbackRegistered = false;
        }
        super.onDestroy();
        binding = null;
    }
}
