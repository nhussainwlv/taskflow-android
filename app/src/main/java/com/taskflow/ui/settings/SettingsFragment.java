package com.taskflow.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.taskflow.R;
import com.taskflow.databinding.FragmentSettingsBinding;
import com.taskflow.util.PreferencesManager;
import com.taskflow.viewmodel.TaskViewModel;

import java.text.DateFormat;
import java.util.Date;

/**
 * Settings for dissertation comparison: readability, reduced motion, system accessibility.
 */
public class SettingsFragment extends Fragment {

    private static final float FONT_MULT_STANDARD = 1f;
    private static final float FONT_MULT_LARGE = 1.12f;
    private static final float FONT_MULT_LARGER = 1.25f;

    private FragmentSettingsBinding binding;
    private PreferencesManager prefs;
    private boolean suppressListeners;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prefs = PreferencesManager.getInstance(requireContext());
        suppressListeners = true;

        binding.toolbar.setNavigationOnClickListener(v ->
                NavHostFragment.findNavController(SettingsFragment.this).navigateUp());

        syncFontScaleUi();
        binding.switchReduceMotion.setChecked(prefs.isReduceMotionEnabled());

        suppressListeners = false;

        binding.radioFontScale.setOnCheckedChangeListener(this::onFontScaleChanged);
        binding.switchReduceMotion.setOnCheckedChangeListener(this::onReduceMotionChanged);

        binding.btnOpenAccessibility.setOnClickListener(v -> openSystemAccessibility());

        binding.btnExportData.setOnClickListener(v -> exportWorkspaceJson());

        binding.btnRecordSync.setOnClickListener(v -> {
            prefs.setLastSuccessfulSyncWallClockMs(System.currentTimeMillis());
            refreshLastSyncLabel();
            Snackbar.make(binding.getRoot(), R.string.settings_sync_recorded, Snackbar.LENGTH_SHORT).show();
        });
        refreshLastSyncLabel();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (prefs != null && binding != null) {
            refreshLastSyncLabel();
        }
    }

    private void refreshLastSyncLabel() {
        long t = prefs.getLastSuccessfulSyncWallClockMs();
        if (t <= 0L) {
            binding.textLastSync.setText(R.string.settings_last_sync_never);
            return;
        }
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
        binding.textLastSync.setText(getString(R.string.settings_last_sync_at, df.format(new Date(t))));
    }

    private void syncFontScaleUi() {
        float m = prefs.getFontScaleMultiplier();
        int id;
        if (m >= FONT_MULT_LARGER - 0.01f) {
            id = R.id.radio_font_larger;
        } else if (m >= FONT_MULT_LARGE - 0.01f) {
            id = R.id.radio_font_large;
        } else {
            id = R.id.radio_font_standard;
        }
        binding.radioFontScale.check(id);
    }

    private void onFontScaleChanged(RadioGroup group, int checkedId) {
        if (suppressListeners) return;
        float next = FONT_MULT_STANDARD;
        if (checkedId == R.id.radio_font_large) {
            next = FONT_MULT_LARGE;
        } else if (checkedId == R.id.radio_font_larger) {
            next = FONT_MULT_LARGER;
        }
        if (Math.abs(next - prefs.getFontScaleMultiplier()) < 0.001f) {
            return;
        }
        prefs.setFontScaleMultiplier(next);
        Snackbar.make(binding.getRoot(), R.string.settings_text_size_hint, Snackbar.LENGTH_SHORT).show();
        requireActivity().recreate();
    }

    private void onReduceMotionChanged(android.widget.CompoundButton buttonView, boolean isChecked) {
        if (suppressListeners) return;
        prefs.setReduceMotionEnabled(isChecked);
        requireActivity().recreate();
    }

    private void exportWorkspaceJson() {
        TaskViewModel vm = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
        vm.exportAllDataAsJson(json -> {
            if (json == null || json.startsWith("{\"error\"")) {
                Snackbar.make(binding.getRoot(), R.string.error_generic_desc, Snackbar.LENGTH_LONG).show();
                return;
            }
            Intent send = new Intent(Intent.ACTION_SEND);
            send.setType("text/plain");
            send.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.settings_export_subject));
            send.putExtra(Intent.EXTRA_TEXT, json);
            startActivity(Intent.createChooser(send, getString(R.string.settings_export)));
            prefs.setLastSuccessfulSyncWallClockMs(System.currentTimeMillis());
            refreshLastSyncLabel();
            Snackbar.make(binding.getRoot(), R.string.settings_export_done, Snackbar.LENGTH_SHORT).show();
        });
    }

    private void openSystemAccessibility() {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Context ctx = requireContext();
        try {
            ctx.startActivity(intent);
        } catch (Exception e) {
            Snackbar.make(binding.getRoot(), R.string.error_generic_desc, Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
