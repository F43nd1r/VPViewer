package com.faendir.kepi.vpviewer.contexts;

import android.app.AlarmManager;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.faendir.kepi.vpviewer.BuildConfig;
import com.faendir.kepi.vpviewer.R;
import com.faendir.kepi.vpviewer.fragments.PrefsFragment;
import com.faendir.kepi.vpviewer.utils.Logger;
import com.faendir.kepi.vpviewer.utils.PersistManager;
import com.faendir.kepi.vpviewer.utils.ServiceManager;

import java.util.Arrays;

/**
 * Created by Lukas on 07.12.2014.
 * Activity displaying the settings
 */
public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private PrefsFragment fragment;
    private SharedPreferences sharedPref;
    private final Logger logger = new Logger(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logger.log(R.string.log_onCreate);
        setContentView(R.layout.activity_settings);
        fragment = (PrefsFragment) getFragmentManager().findFragmentById(R.id.fragment);
        fragment.getPreferenceManager().createPreferenceScreen(this);
        sharedPref = fragment.getPreferenceScreen().getSharedPreferences();
        EditTextPreference prefPassword = (EditTextPreference) fragment.findPreference(getString(R.string.pref_password));
        String pw = prefPassword.getText();
        if (pw == null || pw.length() == 0) prefPassword.setSummary(R.string.unset);
        else {
            prefPassword.setSummary(sharedPref.getString(getString(R.string.key_login), ""));
        }
        EditTextPreference prefUser = (EditTextPreference) fragment.findPreference(getString(R.string.pref_user));
        String user = prefUser.getText();
        if (user == null || user.length() == 0) prefUser.setSummary(R.string.unset);
        else prefUser.setSummary(user);
        MultiSelectListPreference prefClass = (MultiSelectListPreference) fragment.findPreference(getString(R.string.pref_class));
        String[] values = new String[0];
        values = prefClass.getValues().toArray(values);
        Arrays.sort(values);
        String className = Arrays.deepToString(values);
        if (className.equals(getString(R.string.match_emptyArray)))
            prefClass.setSummary(R.string.unset);
        else prefClass.setSummary(className.substring(1, className.length() - 1));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            CheckBoxPreference asAction = (CheckBoxPreference) fragment.findPreference(getString(R.string.pref_showAsActions));
            asAction.setSummary(R.string.only_for_ics_or_higher);
            asAction.setEnabled(false);
        }
        ListPreference interval = (ListPreference) fragment.findPreference(getString(R.string.pref_interval));
        interval.setSummary(interval.getEntry());
        Preference reset = fragment.findPreference(getString(R.string.pref_resetNotification));
        reset.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(@NonNull Preference preference) {
                deleteDays();
                Toast.makeText(getApplicationContext(), getString(R.string.done) + getString(R.string.text_exclamation), Toast.LENGTH_LONG).show();
                preference.setEnabled(false);
                return true;
            }
        });
        if (!BuildConfig.DEBUG) {
            //remove enable acra preference
            CheckBoxPreference acraPref = (CheckBoxPreference) fragment.findPreference(getString(R.string.pref_enableAcra));
            acraPref.setChecked(true);
            fragment.getPreferenceScreen().removePreference(acraPref);
        }
    }

    protected void onResume() {
        super.onResume();
        sharedPref.registerOnSharedPreferenceChangeListener(this);
        if (checkForDays())
            fragment.findPreference(getString(R.string.pref_resetNotification)).setEnabled(true);
    }

    protected void onPause() {
        super.onPause();
        sharedPref.unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(@NonNull SharedPreferences sharedPreferences,
                                          @NonNull String key) {
        Preference pref = fragment.findPreference(key);
        if (key.equals(getString(R.string.pref_user))) {
            EditTextPreference etp = (EditTextPreference) pref;
            String text = etp.getText();
            if (text.length() == 0) pref.setSummary(R.string.unset);
            else pref.setSummary(text);
        } else if (key.equals(getString(R.string.pref_password))) {
            EditTextPreference etp = (EditTextPreference) pref;
            String text = etp.getText();
            if (text.length() == 0) etp.setSummary(R.string.unset);
            else etp.setSummary(R.string.Login_unknown);
        } else if (key.equals(getString(R.string.pref_class))) {
            MultiSelectListPreference classPref = (MultiSelectListPreference) pref;
            String[] values = new String[0];
            values = classPref.getValues().toArray(values);
            Arrays.sort(values);
            String className = Arrays.deepToString(values);
            if (values.length == 0) classPref.setSummary(R.string.unset);
            else classPref.setSummary(className.substring(1, className.length() - 1));
        } else if (key.equals(getString(R.string.pref_notify))) {
            if (sharedPreferences.getBoolean(getString(R.string.pref_notify), false))
                startService(sharedPreferences);
            else stopService();
        } else if (key.equals(getString(R.string.pref_interval))) {
            ListPreference lp = (ListPreference) pref;
            lp.setSummary(lp.getEntry());
            if (sharedPreferences.getBoolean(getString(R.string.pref_notify), false))
                startService(sharedPreferences);
        }
    }

    private void stopService() {
        ServiceManager.stopService(this);
        PackageManager pm = this.getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(getApplicationContext(), BootBroadcastReceiver.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        logger.log(R.string.log_serviceStopped);
    }

    private void startService(@NonNull SharedPreferences sharedPreferences) {
        ServiceManager.startService(this, Integer.parseInt(sharedPreferences.getString(getString(R.string.pref_interval), String.valueOf(AlarmManager.INTERVAL_HOUR))));
        PackageManager pm = this.getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(getApplicationContext(), BootBroadcastReceiver.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
        logger.log(R.string.log_serviceStarted);
    }

    private boolean checkForDays() {
        return sharedPref.contains(getString(R.string.file_days));
    }

    private void deleteDays() {
        logger.log(R.string.log_resetDays);
        new PersistManager(this).resetDays();
    }
}
