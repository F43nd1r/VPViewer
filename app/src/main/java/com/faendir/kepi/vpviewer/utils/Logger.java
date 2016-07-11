package com.faendir.kepi.vpviewer.utils;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.util.Log;

import com.faendir.kepi.vpviewer.BuildConfig;
import com.faendir.kepi.vpviewer.contexts.VPViewer;

/**
 * Created by Lukas on 04.03.2015.
 * Manages Logs in debug mode.
 */
public class Logger {

    private final String tag;

    public Logger(@NonNull Object object) {
        this(object.getClass());
    }

    public Logger(@NonNull Class clazz){
        this(clazz.getSimpleName());
    }

    public Logger(@NonNull String className) {
        tag = className;
    }

    public void log(String message) {
        if (BuildConfig.DEBUG) Log.d(tag, message);
    }

    public void log(@StringRes int messageId) {
        log(VPViewer.getStringStatic(messageId));
    }

    public void log(Throwable t) {
        log(t.getMessage() + "\n" + TextUtils.join("\n", t.getStackTrace()));
    }
}
