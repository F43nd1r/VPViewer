package com.faendir.kepi.vpviewer.legacy;

import android.app.AlarmManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.faendir.kepi.vpviewer.BuildConfig;
import com.faendir.kepi.vpviewer.R;
import com.faendir.kepi.vpviewer.event.InvalidateRequest;
import com.faendir.kepi.vpviewer.utils.Logger;
import com.faendir.kepi.vpviewer.utils.ServiceManager;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Lukas on 16.04.2016.
 */
public final class Migrator {
    private static final Logger logger = new Logger(Migrator.class);

    private Migrator() {
    }

    public static void migrateFromOldVersionIfRequired(Context context) {
        String currentVersion = String.valueOf(BuildConfig.VERSION_CODE);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String version = sharedPref.getString(context.getString(R.string.key_runningVersion), null);
        if (version == null || !version.equals(currentVersion)) {
            updateToCurrentVersion(context, sharedPref, currentVersion);
        }
    }

    private static void updateToCurrentVersion(Context context, SharedPreferences sharedPref, String currentVersion) {
        PreferenceManager.setDefaultValues(context, R.xml.pref_general, false);
        SharedPreferences.Editor editor = sharedPref.edit();
        if (sharedPref.getBoolean(context.getString(R.string.pref_notify), false)) {
            ServiceManager.scheduleService(context, Integer.parseInt(sharedPref.getString(context.getString(R.string.pref_interval), String.valueOf(AlarmManager.INTERVAL_HOUR))));
        }
        logger.log(context.getString(R.string.log_versionUpdated) + currentVersion);
        if (!BuildConfig.DEBUG) {
            editor.putBoolean(context.getString(R.string.pref_enableAcra), true);
        }
        editor.putBoolean(context.getString(R.string.pref_manualView), false);
        editor.putString(context.getString(R.string.key_runningVersion), currentVersion);
        editor.apply();
        EventBus.getDefault().post(new InvalidateRequest(true));
    }

}
