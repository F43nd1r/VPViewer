package com.faendir.kepi.vpviewer.utils;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.SystemClock;
import android.support.annotation.NonNull;

import com.faendir.kepi.vpviewer.R;
import com.faendir.kepi.vpviewer.contexts.UpdateService;

/**
 * Created by Lukas on 12.12.2014.
 * A small set of static methods required at several places, targeting the service
 */
public final class ServiceManager {
    private ServiceManager() {
    }

    private static final Logger logger = new Logger(ServiceManager.class.getSimpleName());

    public static void startService(Context context, int interval) {
        logger.log(R.string.log_attemtStartService);
        Intent i = new Intent(context, UpdateService.class);
        PendingIntent pIntent = PendingIntent.getService(context, 0, i, 0);
        AlarmManager am = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
        am.cancel(pIntent);
        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), interval, pIntent);
    }

    public static void stopService(Context context) {
        logger.log(R.string.log_ateemtStopService);
        Intent i = new Intent(context, UpdateService.class);
        PendingIntent pIntent = PendingIntent.getService(context, 0, i, 0);
        AlarmManager am = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
        am.cancel(pIntent);
    }

    public static void bindService(Context context, @NonNull ServiceConnection connection) {
        logger.log(R.string.log_attemtBindService);
        context.bindService(new Intent(context, UpdateService.class), connection, Activity.BIND_AUTO_CREATE);
    }

    public static void unbindService(Context context, @NonNull ServiceConnection connection) {
        logger.log(R.string.log_attemtUnbindService);
        context.unbindService(connection);
    }
}
