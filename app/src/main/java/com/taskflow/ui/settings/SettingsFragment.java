/**
 * Student Name : Naeem Hussain
 * ID : 2365963
 * Module Name : Project and Professionalism
 * Note: Comments in this file are kept brief and readable.
 */

package com.taskflow.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.appcompat.app.AlertDialog;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;
import com.taskflow.R;
import com.taskflow.data.repo.TaskRepository;
import com.taskflow.databinding.FragmentSettingsBinding;
import com.taskflow.model.User;
import com.taskflow.util.PreferencesManager;
import com.taskflow.viewmodel.TaskViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

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
    private TaskRepository taskRepository;
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
        taskRepository = new TaskRepository(requireActivity().getApplication());
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

        binding.btnDemoSignIn.setOnClickListener(v -> showSignInDialog());
        binding.btnDemoSignUp.setOnClickListener(v -> showSignUpDialog());
        binding.btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        binding.btnDemoSignOut.setOnClickListener(v -> taskRepository.signOutDemoSession(() ->
                Snackbar.make(binding.getRoot(), R.string.settings_demo_signed_out, Snackbar.LENGTH_SHORT).show()
        ));

        taskRepository.getCurrentUser().observe(getViewLifecycleOwner(), this::renderAccountState);

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

    private void renderAccountState(User user) {
        if (binding == null) return;
        String email = user != null ? user.getEmail() : null;
        boolean signedIn = email != null && email.equalsIgnoreCase(TaskRepository.DEMO_USER_EMAIL);

        if (signedIn) {
            binding.textAccountStatus.setText(getString(R.string.auth_account_status_signed_in, email));
        } else {
            binding.textAccountStatus.setText(R.string.auth_account_status_signed_out);
        }
        binding.btnDemoSignIn.setVisibility(signedIn ? View.GONE : View.VISIBLE);
        binding.btnDemoSignUp.setVisibility(signedIn ? View.GONE : View.VISIBLE);
        binding.btnChangePassword.setVisibility(signedIn ? View.VISIBLE : View.GONE);
        binding.btnDemoSignOut.setVisibility(signedIn ? View.VISIBLE : View.GONE);
    }

    private void showSignInDialog() {
        View content = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_sign_in, null, false);
        TextInputLayout emailLayout = content.findViewById(R.id.input_layout_email);
        TextInputLayout passwordLayout = content.findViewById(R.id.input_layout_password);
        TextInputEditText emailInput = content.findViewById(R.id.input_email);
        TextInputEditText passwordInput = content.findViewById(R.id.input_password);
        if (emailInput != null) emailInput.setText(TaskRepository.DEMO_USER_EMAIL);
        if (passwordInput != null) {
            passwordInput.setText("");
            passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.auth_sign_in_title)
                .setView(content)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.settings_demo_sign_in, null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String email = emailInput != null && emailInput.getText() != null
                    ? emailInput.getText().toString().trim()
                    : "";
            String password = passwordInput != null && passwordInput.getText() != null
                    ? passwordInput.getText().toString()
                    : "";

            if (emailLayout != null) emailLayout.setError(null);
            if (passwordLayout != null) passwordLayout.setError(null);

            boolean valid = true;
            if (email.isEmpty()) {
                valid = false;
                if (emailLayout != null) emailLayout.setError(getString(R.string.auth_email_required));
            }
            if (password.isEmpty()) {
                valid = false;
                if (passwordLayout != null) passwordLayout.setError(getString(R.string.auth_password_required));
            }
            if (!valid) {
                return;
            }

            taskRepository.signInLocalSession(email, password, ok -> {
                if (!ok) {
                    if (emailLayout != null) emailLayout.setError(getString(R.string.auth_invalid_credentials));
                    if (passwordLayout != null) passwordLayout.setError(getString(R.string.auth_invalid_credentials));
                    return;
                }
                if (binding != null) {
                    Snackbar.make(binding.getRoot(), R.string.settings_demo_signed_in, Snackbar.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            });
        }));

        dialog.show();
    }

    private void showSignUpDialog() {
        View content = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_sign_up, null, false);
        TextInputLayout nameLayout = content.findViewById(R.id.input_layout_name);
        TextInputLayout emailLayout = content.findViewById(R.id.input_layout_email);
        TextInputLayout passwordLayout = content.findViewById(R.id.input_layout_password);
        TextInputEditText nameInput = content.findViewById(R.id.input_name);
        TextInputEditText emailInput = content.findViewById(R.id.input_email);
        TextInputEditText passwordInput = content.findViewById(R.id.input_password);
        if (passwordInput != null) {
            passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.auth_create_account_title)
                .setView(content)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.auth_create_account_action, null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = nameInput != null && nameInput.getText() != null
                    ? nameInput.getText().toString().trim()
                    : "";
            String email = emailInput != null && emailInput.getText() != null
                    ? emailInput.getText().toString().trim()
                    : "";
            String password = passwordInput != null && passwordInput.getText() != null
                    ? passwordInput.getText().toString()
                    : "";

            if (nameLayout != null) nameLayout.setError(null);
            if (emailLayout != null) emailLayout.setError(null);
            if (passwordLayout != null) passwordLayout.setError(null);

            boolean valid = true;
            if (name.isEmpty()) {
                valid = false;
                if (nameLayout != null) nameLayout.setError(getString(R.string.auth_name_required));
            }
            if (email.isEmpty()) {
                valid = false;
                if (emailLayout != null) emailLayout.setError(getString(R.string.auth_email_required));
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                valid = false;
                if (emailLayout != null) emailLayout.setError(getString(R.string.auth_email_invalid));
            }
            if (password.isEmpty()) {
                valid = false;
                if (passwordLayout != null) passwordLayout.setError(getString(R.string.auth_password_required));
            } else if (password.length() < 6) {
                valid = false;
                if (passwordLayout != null) passwordLayout.setError(getString(R.string.auth_password_too_short));
            }
            if (!valid) {
                return;
            }

            taskRepository.signUpLocalSession(name, email, password, ok -> {
                if (!ok) {
                    if (emailLayout != null) emailLayout.setError(getString(R.string.auth_email_exists));
                    return;
                }
                if (binding != null) {
                    Snackbar.make(binding.getRoot(), R.string.auth_account_created, Snackbar.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            });
        }));

        dialog.show();
    }

    private void showChangePasswordDialog() {
        View content = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_change_password, null, false);
        TextInputLayout currentLayout = content.findViewById(R.id.input_layout_current_password);
        TextInputLayout newLayout = content.findViewById(R.id.input_layout_new_password);
        TextInputEditText currentInput = content.findViewById(R.id.input_current_password);
        TextInputEditText newInput = content.findViewById(R.id.input_new_password);
        if (currentInput != null) {
            currentInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        if (newInput != null) {
            newInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.auth_change_password_title)
                .setView(content)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.auth_change_password_button, null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String currentPassword = currentInput != null && currentInput.getText() != null
                    ? currentInput.getText().toString()
                    : "";
            String newPassword = newInput != null && newInput.getText() != null
                    ? newInput.getText().toString()
                    : "";

            if (currentLayout != null) currentLayout.setError(null);
            if (newLayout != null) newLayout.setError(null);

            boolean valid = true;
            if (currentPassword.isEmpty()) {
                valid = false;
                if (currentLayout != null) currentLayout.setError(getString(R.string.auth_password_required));
            }
            if (newPassword.isEmpty()) {
                valid = false;
                if (newLayout != null) newLayout.setError(getString(R.string.auth_password_required));
            } else if (newPassword.length() < 6) {
                valid = false;
                if (newLayout != null) newLayout.setError(getString(R.string.auth_password_too_short));
            }
            if (!valid) {
                return;
            }

            taskRepository.changePasswordForCurrentUser(currentPassword, newPassword, ok -> {
                if (!ok) {
                    if (currentLayout != null) {
                        currentLayout.setError(getString(R.string.auth_current_password_incorrect));
                    }
                    return;
                }
                if (binding != null) {
                    Snackbar.make(binding.getRoot(), R.string.auth_password_updated, Snackbar.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            });
        }));

        dialog.show();
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
