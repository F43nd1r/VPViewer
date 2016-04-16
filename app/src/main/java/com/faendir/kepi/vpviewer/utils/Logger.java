package com.faendir.kepi.vpviewer.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.util.Log;

import com.faendir.kepi.vpviewer.BuildConfig;
import com.faendir.kepi.vpviewer.contexts.VPViewer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Lukas on 04.03.2015.
 * Manages Logs in debug mode.
 */
public class Logger {
    private static final LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();
    private static final File file = new File(System.getProperty("java.io.tmpdir"),Logger.class.getName()+"-log.txt");
    private static Thread thread;
    static {
        queue.add("--------- beginning of process ---------");
    }

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
        List<String> lines = Arrays.asList(message.split("\n"));
        lines.set(0, tag + ": " + lines.get(0));
        queue.addAll(lines);
        ensureRunning();
    }

    public void log(@StringRes int messageId) {
        log(VPViewer.getStringStatic(messageId));
    }

    public void log(Throwable t) {
        log(t.getMessage() + "\n" + TextUtils.join("\n", t.getStackTrace()));
    }

    public static String getPath(){
        return file.getPath();
    }

    private static void ensureRunning() {
        if (thread == null || !thread.isAlive()) {
            thread = new FileLogThread();
            thread.start();
        }
    }

    private static class FileLogThread extends Thread {
        @Override
        public void run() {
            Writer writer = null;
            try {
                writer = new BufferedWriter(new FileWriter(file, true));
                //noinspection InfiniteLoopStatement
                while (true) {
                    writer.append(queue.take()).append("\n");
                    if (queue.isEmpty()) {
                        writer.flush();
                    }
                }
            } catch (IOException | InterruptedException ignored) {
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }
    }
}
