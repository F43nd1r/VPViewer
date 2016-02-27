package com.faendir.kepi.vpviewer.utils;

import android.content.Context;
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
    private static final StringBuilder GLOBAL = new StringBuilder();
    private static final int MAX_LENGTH = 2048;

    private final String clazz;

    public Logger(@NonNull Context context) {
        clazz = context.getClass().getSimpleName();
    }

    public Logger(String className) {
        clazz = className;
    }

    public void log(String message) {
        if (BuildConfig.DEBUG) Log.d(clazz, message);
        if (GLOBAL.length() > MAX_LENGTH) {
            GLOBAL.delete(0, GLOBAL.indexOf("\n", 512) + 1);
        }
        GLOBAL.append(clazz).append(": ").append(message).append("|::|");
    }

    public void log(@StringRes int messageId) {
        log(VPViewer.getStringStatic(messageId));
    }

    public static String globalLog() {
        return GLOBAL.toString();
    }

    public void log(Throwable t) {
        log(t.getMessage() + "\n" + TextUtils.join("\n", t.getStackTrace()));
    }
}
