package com.faendir.kepi.vpviewer.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.widget.TextView;

import com.faendir.kepi.vpviewer.R;
import com.faendir.kepi.vpviewer.utils.Logger;

/**
 * Manages the status area
 * <p>
 * Created by Lukas on 16.04.2016.
 */
public class StatusManager {
    private final Logger logger = new Logger(this);
    private final Context context;
    private final TextView status;
    private final TextView update;

    public StatusManager(Context context, TextView status, TextView update) {
        this.context = context;
        this.status = status;
        this.update = update;
    }

    public void update() {
        logger.log(R.string.log_statusUpdate);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifi != null && wifi.isConnected()) status.setText(R.string.wifi_ok);
        else {
            if (sharedPref.getBoolean(context.getString(R.string.pref_wifiOnly), false))
                status.setText(R.string.wifi_fail);
            NetworkInfo mobile = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (mobile != null && mobile.isConnected()) status.setText(R.string.network_ok);
            else status.setText(R.string.network_fail);
        }
        status.invalidate();
        String lastUpdate = sharedPref.getString(context.getString(R.string.key_update), null);
        if (lastUpdate != null) {
            update.setText(context.getString(R.string.last_update, lastUpdate));
        } else {
            update.setText(context.getString(R.string.last_update, context.getString(R.string.no_update)));
        }
        update.invalidate();
    }
}
