package sugar.free.sightremote.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;

import sugar.free.sightremote.R;
import sugar.free.sightremote.dialogs.ActivationWarningDialogChain;
import sugar.free.sightremote.dialogs.ChangePINDialog;
import sugar.free.sightremote.dialogs.ConfirmPINDialog;
import sugar.free.sightremote.dialogs.ConfirmationDialog;
import sugar.free.sightremote.utils.HTMLUtil;

import static sugar.free.sightremote.utils.Preferences.*;

public class SettingsActivity extends SightActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.activity_settings);
        if (savedInstanceState == null)
            getFragmentManager().beginTransaction().replace(R.id.frame_layout, new SettingsFragment()).commit();
    }

    @Override
    protected void connectedToService() {
        getServiceConnector().connect();
    }

    @Override
    protected boolean snackbarEnabled() {
        return false;
    }

    @Override
    protected boolean useOverlay() {
        return false;
    }

    public static class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {
        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);
            getPreferenceScreen().findPreference("enter_password").setOnPreferenceClickListener(this);
            getPreferenceScreen().findPreference("enable_confirmation_challenges").setOnPreferenceClickListener(this);
            getPreferenceScreen().findPreference(PREF_BOOLEAN_ENABLE_CONFIRMATION_CHALLENGES).setOnPreferenceChangeListener(this);
            getPreferenceScreen().findPreference(PREF_BOOLEAN_CONFIRMATION_USE_FINGERPRINT).setOnPreferenceChangeListener(this);
            getPreferenceScreen().findPreference(PREF_BOOLEAN_CONFIRMATION_USE_PIN).setOnPreferenceChangeListener(this);
            getPreferenceScreen().findPreference(PREF_STRING_CONFIRMATION_PIN).setOnPreferenceClickListener(this);
            if (!ConfirmationDialog.isFingerprintScannerAvailable()) {
                getPreferenceScreen().findPreference(PREF_BOOLEAN_CONFIRMATION_USE_FINGERPRINT).setEnabled(false);
            }
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (preference.getKey().equals("enter_password")) {
                new ActivationWarningDialogChain(getSettingsActivity(), getSettingsActivity().getServiceConnector()).doActivationWarning();
                return true;
            } else if (preference.getKey().equals("delete_pairing")) {
                new AlertDialog.Builder(getSettingsActivity())
                        .setTitle(R.string.confirmation)
                        .setMessage(HTMLUtil.getHTML(R.string.delete_pairing_confirmation))
                        .setPositiveButton(R.string.yes, (dialog, which) -> {
                            getSettingsActivity().getServiceConnector().reset();
                            Intent intent = new Intent(getSettingsActivity(), SetupActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            getSettingsActivity().finish();
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
                return true;
            } else if (preference.getKey().equals(PREF_STRING_CONFIRMATION_PIN)) {
                new ChangePINDialog(getSettingsActivity(), null).show();
                return true;
            }
            return false;
        }

        private SettingsActivity getSettingsActivity() {
            return (SettingsActivity) getActivity();
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object o) {
            if (preference.getKey().equals(PREF_BOOLEAN_CONFIRMATION_USE_PIN)) {
                if (getBooleanPref(PREF_BOOLEAN_CONFIRMATION_USE_PIN)) {
                    new ConfirmPINDialog(getSettingsActivity(), () -> {
                        ((CheckBoxPreference) preference).setChecked(false);
                        setBooleanPref(PREF_BOOLEAN_CONFIRMATION_USE_PIN, false);
                        setStringPref(PREF_STRING_CONFIRMATION_PIN, null);
                    }).show();
                } else new ChangePINDialog(getSettingsActivity(), () -> {
                    ((CheckBoxPreference) preference).setChecked(true);
                    setBooleanPref(PREF_BOOLEAN_CONFIRMATION_USE_PIN, true);
                }).show();
                return false;
            } else if (preference.getKey().equals(PREF_BOOLEAN_CONFIRMATION_USE_FINGERPRINT)) {
                if (getBooleanPref(PREF_BOOLEAN_CONFIRMATION_USE_PIN)) {
                    new ConfirmPINDialog(getSettingsActivity(), () -> {
                        CheckBoxPreference useFingerprint = (CheckBoxPreference) preference;
                        useFingerprint.setChecked(!useFingerprint.isChecked());
                        setBooleanPref(PREF_BOOLEAN_CONFIRMATION_USE_FINGERPRINT, useFingerprint.isChecked());
                    }).show();
                    return false;
                }
            } else if (preference.getKey().equals(PREF_BOOLEAN_ENABLE_CONFIRMATION_CHALLENGES)) {
                if (getBooleanPref(PREF_BOOLEAN_CONFIRMATION_USE_PIN)) {
                    new ConfirmPINDialog(getSettingsActivity(), () -> {
                        ((CheckBoxPreference) preference).setChecked(false);
                        ((CheckBoxPreference) getPreferenceScreen().findPreference(PREF_BOOLEAN_CONFIRMATION_USE_PIN)).setChecked(false);
                        setBooleanPref(PREF_BOOLEAN_ENABLE_CONFIRMATION_CHALLENGES, false);
                        setBooleanPref(PREF_BOOLEAN_CONFIRMATION_USE_PIN, false);
                        setStringPref(PREF_STRING_CONFIRMATION_PIN, null);
                    }).show();
                    return false;
                }
            }
            return true;
        }
    }

    @Override
    protected int getSelectedNavItemID() {
        return R.id.nav_settings;
    }
}
