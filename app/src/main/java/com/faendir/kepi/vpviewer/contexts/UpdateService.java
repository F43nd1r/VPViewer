package com.faendir.kepi.vpviewer.contexts;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.faendir.kepi.vpviewer.R;
import com.faendir.kepi.vpviewer.data.Day;
import com.faendir.kepi.vpviewer.data.VPEntry;
import com.faendir.kepi.vpviewer.event.InvalidateRequest;
import com.faendir.kepi.vpviewer.event.UpdateEvent;
import com.faendir.kepi.vpviewer.utils.ConnectionResult;
import com.faendir.kepi.vpviewer.utils.DateFactory;
import com.faendir.kepi.vpviewer.utils.DateFormat;
import com.faendir.kepi.vpviewer.utils.Logger;
import com.faendir.kepi.vpviewer.utils.PersistManager;

import org.acra.ACRA;
import org.apache.commons.io.Charsets;
import org.greenrobot.eventbus.EventBus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Lukas on 06.12.2014.
 * The main service, used to retrieve data from the webserver and to notify the user if something changed at the VP
 */
public class UpdateService extends Service {
    private final Logger logger = new Logger(this);

    private static final String SELECTOR_TITLE = ".mon_title";
    private static final String SELECTOR_TABLE = ".mon_list";
    private static final String SELECTOR_TABLE_ROW = "tr";
    private static final String SELECTOR_TABLE_HEADER = "th";
    private static final String SELECTOR_TABLE_CELL = "td";
    private static final String SELECTOR_STATUS_DATE = "font";
    private static final String SELECTOR_NEWS_TABLE = ".info";

    public static final String HOST = "http://kepiserver.de";
    public static final String PAGE = "/vp_home/VP_Web.php";

    private SharedPreferences sharedPref;
    private PersistManager persistManager;


    @Override
    public void onCreate() {
        super.onCreate();
        logger.log(R.string.log_onCreate);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        persistManager = new PersistManager(this);
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, final int startId) {
        logger.log(R.string.log_onStart);
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (intent.getBooleanExtra(getString(R.string.extra_isForeground), false) || sharedPreferences.getBoolean(getString(R.string.pref_notify), false)
                        && (!sharedPreferences.getBoolean(getString(R.string.pref_wifiOnly), false) || (wifi != null && wifi.isConnected()))) {
                    ConnectionResult result = getAndHandleRawHtml();
                    EventBus.getDefault().post(new UpdateEvent(result));
                }
                stopSelf(startId);
                InvalidateRequest.dispose();
            }
        }).start();
        return Service.START_REDELIVER_INTENT;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void notify(@Nullable Day day, @NonNull Day newDay) {
        logger.log(R.string.log_notify);
        List<VPEntry> oldList = null;
        if (day != null) oldList = day.getEntryList();
        List<VPEntry> newList = newDay.getEntryList();
        List<VPEntry> notifyFor = new ArrayList<>();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        for (int i = 0; i < newList.size(); i++) {
            VPEntry entry = newList.get(i);
            Set<String> set = sharedPreferences.getStringSet(getString(R.string.pref_class), new HashSet<String>());
            if (matches(set, entry.className) && (oldList == null || !oldList.contains(entry)))
                notifyFor.add(entry);
            logger.log(getString(R.string.log_compare) + Arrays.deepToString(set.toArray()) + getString(R.string.text_comma) + entry.className);
            logger.log(getString(R.string.log_lodList) + oldList + (oldList != null ? getString(R.string.log_contains) + entry : ""));
        }
        if (notifyFor.size() > 0) {
            logger.log(R.string.log_buildNotification);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
            builder.setContentTitle(DateFactory.format(newDay.getDate(), DateFormat.READABLE));
            builder.setContentText(notifyFor.size() == 1 ? notifyFor.get(0).toString() : notifyFor.size() + " " + getString(R.string.new_entries));
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            for (int i = 0; i < notifyFor.size(); i++) {
                inboxStyle.addLine(notifyFor.get(i).toString());
            }
            builder.setStyle(inboxStyle);
            builder.setSmallIcon(R.drawable.ic_notification);
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra(getString(R.string.key_loadDay), newDay.getDate());
            builder.setContentIntent(PendingIntent.getActivity(this, newDay.getDate().hashCode(), intent, PendingIntent.FLAG_ONE_SHOT));
            builder.setAutoCancel(true);
            ((NotificationManager) this.getSystemService(NOTIFICATION_SERVICE)).notify(newDay.getDate().hashCode(), builder.build());
        }
    }

    private ConnectionResult getAndHandleRawHtml() {
        try {
            OutputStream output = null;
            InputStream input = null;
            try {
                URLConnection connection = new URL(HOST + PAGE).openConnection();
                connection.setDoOutput(true);
                String user = sharedPref.getString(getString(R.string.pref_user), "").trim().toLowerCase();
                logger.log(getString(R.string.log_user) + user);
                String password = sharedPref.getString(getString(R.string.pref_password), "").trim();
                logger.log(getString(R.string.log_password) + password);
                String params = "user=" + user + "&passwort=" + password + "&submit=Anmelden";
                output = connection.getOutputStream();
                output.write(params.getBytes(Charsets.UTF_8));
                input = connection.getInputStream();
                Document document = Jsoup.parse(input, Charsets.UTF_8.name(), HOST);
                boolean correctLogin = document.select(SELECTOR_TITLE).size() > 0;
                if (correctLogin) {
                    sharedPref.edit()
                            .putString(getString(R.string.key_login), getString(R.string.Login_ok))
                            .putString(getString(R.string.key_update), DateFactory.format(Calendar.getInstance().getTime(), DateFormat.WITH_TIME))
                            .apply();
                    handle(document);
                    logger.log(getString(R.string.log_updateSet) + DateFactory.format(Calendar.getInstance().getTime(), DateFormat.WITH_TIME));
                    return ConnectionResult.SUCCESS;
                } else {
                    sharedPref.edit().putString(getString(R.string.key_login), getString(R.string.Login_failed)).apply();
                    return ConnectionResult.BAD_LOGIN;
                }
            } catch (IOException e) {
                logger.log(e);
                return ConnectionResult.CONNECTION_FAILED;
            } finally {
                try {
                    if (output != null) {
                        output.close();
                    }
                    if (input != null) {
                        input.close();
                    }
                } catch (IOException e) {
                    logger.log(e);
                    ACRA.getErrorReporter().handleException(e);
                }
            }
        } catch (Exception throwable) {
            logger.log(throwable);
            ACRA.getErrorReporter().handleException(throwable);
            return ConnectionResult.CRASH;
        }
    }

    private void handle(@NonNull Document document) {
        for (Element element : document.select("center")) {
            if (element.select("table").size() > 0) handleDay(element);
        }
        persistManager.setRaw(document);
    }

    private void handleDay(@NonNull Element element) {
        String dateString = element.select(SELECTOR_TITLE).text().split(" ")[0];
        String statusDateString = element.select(SELECTOR_STATUS_DATE).text().replace("Uhr", "").replace("Stand:", "").trim();
        Date currentDate = DateFactory.parse(dateString);
        Date statusDate = DateFactory.parse(statusDateString);
        if (statusDate == null) statusDate = currentDate;
        logger.log(getString(R.string.log_readDay) + DateFactory.format(currentDate, DateFormat.READABLE) + getString(R.string.log_fromHtml));
        Day day = persistManager.getDay(currentDate);
        //day was never retrieved before, or it was updated
        if (day == null || day.getStatusDate().compareTo(statusDate) < 0) {
            List<VPEntry> entryList = createEntryList(element.select(SELECTOR_TABLE).first());
            String[] news = createNewsArray(element.select(SELECTOR_NEWS_TABLE).first());
            Day newDay = new Day(currentDate, statusDate, entryList, news);
            persistManager.setDay(newDay);
            notify(day, newDay);
        }
    }

    @NonNull
    private List<VPEntry> createEntryList(@NonNull Element table) {
        ArrayList<VPEntry> list = new ArrayList<>();
        for (Element line : table.select(SELECTOR_TABLE_ROW)) {
            if (line.select(SELECTOR_TABLE_HEADER).size() > 0) continue;
            Elements parts = line.select(SELECTOR_TABLE_CELL);
            if (parts.size() == 8) {
                list.add(new VPEntry(getApplicationContext(), parts.get(0).text(), parts.get(1).text(),
                        parts.get(2).text(), parts.get(3).text(), parts.get(4).text(),
                        parts.get(5).text(), parts.get(6).text(), parts.get(7).text()));
            }
        }
        return list;
    }

    @NonNull
    private String[] createNewsArray(@Nullable Element table) {
        if (table != null) {
            logger.log(R.string.log_readNews);
            ArrayList<String> list = new ArrayList<>();
            for (Element line : table.select(SELECTOR_TABLE_ROW)) {
                if (line.select(SELECTOR_TABLE_HEADER).size() > 0) continue;
                String text = line.html().trim();
                if (text.length() > 0) {
                    list.add(text);
                }
            }
            return list.toArray(new String[list.size()]);
        }
        return new String[0];
    }


    private boolean matches(@NonNull Set<String> set, @NonNull String string) {
        String compare1 = string.toLowerCase().trim();
        for (String s : set) {
            String compare2 = s.toLowerCase().trim();
            if (compare1.contains(compare2)) return true;
        }
        return false;
    }
}
