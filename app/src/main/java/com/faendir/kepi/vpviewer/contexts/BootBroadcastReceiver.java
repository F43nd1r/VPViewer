package com.faendir.kepi.vpviewer.contexts;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.faendir.kepi.vpviewer.R;
import com.faendir.kepi.vpviewer.utils.Logger;
import com.faendir.kepi.vpviewer.utils.ServiceManager;

/**
 * Created by Lukas on 06.12.2014.
 * Starts the service at device startup, if enabled
 */
public class BootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        if (intent.getAction().equals(context.getString(R.string.intent_bootCompleted))) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            if (sharedPreferences.getBoolean(context.getString(R.string.pref_notify), false)) {
                ServiceManager.scheduleService(context, Integer.parseInt(sharedPreferences.getString(context.getString(R.string.pref_interval), String.valueOf(AlarmManager.INTERVAL_HOUR))));
                new Logger(context).log(R.string.log_startedBoot);
            }
        } else new Logger(context).log(R.string.log_badIntent);
    }
}
