package com.faendir.kepi.vpviewer.utils;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.faendir.kepi.vpviewer.R;
import com.faendir.kepi.vpviewer.contexts.UpdateService;
import com.faendir.kepi.vpviewer.event.InvalidateRequest;

import org.greenrobot.eventbus.EventBus;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Lukas on 12.12.2014.
 * A small set of static methods required at several places, targeting the service
 */
public final class ServiceManager {
    private ServiceManager() {
    }

    private static final Logger logger = new Logger(ServiceManager.class);

    public static void scheduleService(Context context, int interval) {
        logger.log(R.string.log_attemtStartService);
        Intent i = new Intent(context, UpdateService.class);
        PendingIntent pIntent = PendingIntent.getService(context, 0, i, 0);
        AlarmManager am = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
        am.cancel(pIntent);
        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), interval, pIntent);
    }

    public static void unscheduleService(Context context) {
        logger.log(R.string.log_ateemtStopService);
        Intent i = new Intent(context, UpdateService.class);
        PendingIntent pIntent = PendingIntent.getService(context, 0, i, 0);
        AlarmManager am = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
        am.cancel(pIntent);
    }

    public static void startServiceIfRequired(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        Date update = DateFactory.parse(sharedPref.getString(context.getString(R.string.key_update), null));
        if (update != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(update);
            cal.add(Calendar.MILLISECOND, Integer.parseInt(sharedPref.getString(context.getString(R.string.pref_interval), context.getString(R.string.update_default))));
            if (cal.before(Calendar.getInstance())) {
                EventBus.getDefault().post(new InvalidateRequest(true));
                logger.log(R.string.log_dataOutOfDate);
            }
        } else {
            EventBus.getDefault().post(new InvalidateRequest(true));
            logger.log(R.string.log_noData);
        }
    }
}
