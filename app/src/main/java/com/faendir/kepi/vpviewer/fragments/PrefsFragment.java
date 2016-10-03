package com.faendir.kepi.vpviewer.fragments;

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
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.faendir.kepi.vpviewer.BuildConfig;
import com.faendir.kepi.vpviewer.R;
import com.faendir.kepi.vpviewer.contexts.BootBroadcastReceiver;
import com.faendir.kepi.vpviewer.event.InvalidateRequest;
import com.faendir.kepi.vpviewer.event.UpdateEvent;
import com.faendir.kepi.vpviewer.utils.Logger;
import com.faendir.kepi.vpviewer.utils.PersistManager;
import com.faendir.kepi.vpviewer.utils.ServiceManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;

public class PrefsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final Logger logger = new Logger(this);
    private SharedPreferences sharedPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
        sharedPref = getPreferenceScreen().getSharedPreferences();
        updateStatus();
        EditTextPreference prefUser = (EditTextPreference) findPreference(getString(R.string.pref_user));
        String user = prefUser.getText();
        if (user != null && user.length() != 0) {
            prefUser.setSummary(user);
        } else {
            prefUser.setSummary(R.string.unset);
        }
        MultiSelectListPreference prefClass = (MultiSelectListPreference) findPreference(getString(R.string.pref_class));
        String[] values = new String[0];
        values = prefClass.getValues().toArray(values);
        Arrays.sort(values);
        String className = Arrays.deepToString(values);
        if (!className.equals(getString(R.string.match_emptyArray))) {
            prefClass.setSummary(className.substring(1, className.length() - 1));
        } else {
            prefClass.setSummary(R.string.unset);
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            getPreferenceScreen().removePreference(findPreference(getString(R.string.pref_showAsActions)));
        }
        ListPreference interval = (ListPreference) findPreference(getString(R.string.pref_interval));
        interval.setSummary(interval.getEntry());
        Preference reset = findPreference(getString(R.string.pref_resetNotification));
        reset.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(@NonNull Preference preference) {
                deleteDays();
                Toast.makeText(getActivity(), getString(R.string.done) + getString(R.string.text_exclamation), Toast.LENGTH_LONG).show();
                preference.setEnabled(false);
                return true;
            }
        });
        if (!BuildConfig.DEBUG) {
            //remove enable acra preference
            CheckBoxPreference acraPref = (CheckBoxPreference) findPreference(getString(R.string.pref_enableAcra));
            acraPref.setChecked(true);
            getPreferenceScreen().removePreference(acraPref);
        }
    }

    private void updateStatus() {
        EditTextPreference prefPassword = (EditTextPreference) findPreference(getString(R.string.pref_password));
        String pw = prefPassword.getText();
        if (pw == null || pw.length() == 0) prefPassword.setSummary(R.string.unset);
        else {
            prefPassword.setSummary(sharedPref.getString(getString(R.string.key_login), ""));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        sharedPref.registerOnSharedPreferenceChangeListener(this);
        EventBus.getDefault().register(this);
        if (checkForDays()) {
            findPreference(getString(R.string.pref_resetNotification)).setEnabled(true);
        }
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        sharedPref.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpdateEvent event) {
        updateStatus();
    }


    @Override
    public void onSharedPreferenceChanged(@NonNull SharedPreferences sharedPreferences,
                                          @NonNull String key) {
        Preference pref = findPreference(key);
        if (key.equals(getString(R.string.pref_user))) {
            EditTextPreference etp = (EditTextPreference) pref;
            String text = etp.getText();
            if (text.length() == 0) pref.setSummary(R.string.unset);
            else pref.setSummary(text);
            InvalidateRequest.post(true);
        } else if (key.equals(getString(R.string.pref_password))) {
            EditTextPreference etp = (EditTextPreference) pref;
            String text = etp.getText();
            if (text.length() == 0) etp.setSummary(R.string.unset);
            else etp.setSummary(R.string.Login_unknown);
            InvalidateRequest.post(true);
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
                enableContinuousService(sharedPreferences);
            else disableContinuousService();
        } else if (key.equals(getString(R.string.pref_interval))) {
            ListPreference lp = (ListPreference) pref;
            lp.setSummary(lp.getEntry());
            if (sharedPreferences.getBoolean(getString(R.string.pref_notify), false))
                enableContinuousService(sharedPreferences);
        }
    }

    private void disableContinuousService() {
        ServiceManager.unscheduleService(getActivity());
        PackageManager pm = getActivity().getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(getActivity(), BootBroadcastReceiver.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        logger.log(R.string.log_serviceStopped);
    }

    private void enableContinuousService(@NonNull SharedPreferences sharedPreferences) {
        ServiceManager.scheduleService(getActivity(), Integer.parseInt(sharedPreferences.getString(getString(R.string.pref_interval), String.valueOf(AlarmManager.INTERVAL_HOUR))));
        PackageManager pm = getActivity().getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(getActivity(), BootBroadcastReceiver.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
        logger.log(R.string.log_serviceStarted);
    }

    private boolean checkForDays() {
        return sharedPref.contains(getString(R.string.file_days));
    }

    private void deleteDays() {
        logger.log(R.string.log_resetDays);
        new PersistManager(getActivity()).resetDays();
    }

}