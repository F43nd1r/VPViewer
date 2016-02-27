package com.faendir.kepi.vpviewer.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.faendir.kepi.vpviewer.R;
import com.faendir.kepi.vpviewer.contexts.VPViewer;
import com.faendir.kepi.vpviewer.data.Day;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.acra.ACRA;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Lukas on 27.02.2015.
 * Manages all files in data storage
 */
public class PersistManager {
    private final Context context;

    public PersistManager(Context context) {
        this.context = context;
    }

    private static final Gson GSON = new GsonBuilder().setDateFormat(java.text.DateFormat.FULL, java.text.DateFormat.FULL).create();
    private static final TypeToken<HashMap<String, Day>> TYPE_TOKEN = new TypeToken<HashMap<String, Day>>() {
    };

    public void setDay(@NonNull Day day) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        Map<String, Day> map = getMap();
        if (map == null) map = new HashMap<>();
        map.put(DateFactory.format(day.getDate(), DateFormat.FOR_FILE), day);
        sharedPref.edit().putString(VPViewer.getStringStatic(R.string.file_days), GSON.toJson(map, TYPE_TOKEN.getType())).apply();
    }

    @Nullable
    public Day getDay(Date date) {
        Map<String, Day> map = getMap();
        if (map != null) return map.get(DateFactory.format(date, DateFormat.FOR_FILE));
        return null;
    }

    public List<Date> getDaysSorted() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        Map<String, Day> map = getMap();
        if (map == null) return new ArrayList<>();
        ArrayList<Date> dates = new ArrayList<>();
        for (String dateString : map.keySet()) {
            dates.add(DateFactory.parse(dateString));
        }
        Collections.sort(dates);
        boolean any = false;
        for (int i = dates.size() - 5; i >= 0; i--) {
            map.remove(DateFactory.format(dates.get(i), DateFormat.FOR_FILE));
            dates.remove(dates.get(i));
            any = true;
        }
        if (any) {
            sharedPref.edit().putString(VPViewer.getStringStatic(R.string.file_days), GSON.toJson(map, TYPE_TOKEN.getType())).apply();
        }
        return dates;
    }

    private Map<String, Day> getMap() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            return GSON.<HashMap<String, Day>>fromJson(sharedPref.getString(VPViewer.getStringStatic(R.string.file_days), ""), TYPE_TOKEN.getType());
        } catch (JsonSyntaxException e) {
            resetDays();
            ACRA.getErrorReporter().putCustomData("COMMENT", "Reset saved data because of JSON error");
            ACRA.getErrorReporter().handleSilentException(e);
        }
        return null;
    }

    public void setRaw(@NonNull Document document) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPref.edit().putString(VPViewer.getStringStatic(R.string.key_rawHtml), document.outerHtml()).apply();
    }

    public String getRaw() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getString(VPViewer.getStringStatic(R.string.key_rawHtml), null);
    }

    public void resetDays() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPref.edit()
                .remove(VPViewer.getStringStatic(R.string.file_days))
                .remove(VPViewer.getStringStatic(R.string.key_rawHtml))
                .apply();
    }
}
