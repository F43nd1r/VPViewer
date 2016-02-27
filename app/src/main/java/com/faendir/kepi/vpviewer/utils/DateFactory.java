package com.faendir.kepi.vpviewer.utils;

import com.faendir.kepi.vpviewer.R;
import com.faendir.kepi.vpviewer.contexts.VPViewer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Lukas on 12.12.2014.
 * Utility to format strings to dates and the other way round for supported patterns
 */
public final class DateFactory {
    private DateFactory() {
    }

    private static final SimpleDateFormat date_with_time = new SimpleDateFormat("dd.MM.yyyy' um 'HH:mm", Locale.GERMANY);
    private static final SimpleDateFormat date = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
    private static final SimpleDateFormat date_for_file = new SimpleDateFormat("yyyy-MM-dd_cc_zzz", Locale.GERMANY);
    private static final SimpleDateFormat date_readable = new SimpleDateFormat("EEEE, dd. MM.", Locale.GERMANY);
    private static final SimpleDateFormat date_for_file_with_time = new SimpleDateFormat("yyyy-MM-dd-HH:mm_cc_zzz", Locale.GERMANY);

    private static final Lock lock = new Lock();

    public static Date parse(String string) {
        if (string == null || "".equals(string)) return null;
        synchronized (lock) {
            try {
                return date_with_time.parse(string);
            } catch (ParseException ignored) {
            }
            try {
                return date_for_file_with_time.parse(string);
            } catch (ParseException ignored) {
            }
            try {
                return date_for_file.parse(string);
            } catch (ParseException ignored) {
            }
            try {
                return date.parse(string);
            } catch (ParseException ignored) {
            }
            try {
                return date_readable.parse(string);
            } catch (ParseException ignored) {
            }
            new Logger(DateFactory.class.getSimpleName()).log(VPViewer.getStringStatic(R.string.log_dateParseFailed) + string);
            return null;
        }
    }

    public static String format(Date which, DateFormat pattern) {
        if (which == null) return null;
        synchronized (lock) {
            switch (pattern) {
                case NUMERIC_READABLE:
                    return date.format(which);
                case WITH_TIME:
                    return date_with_time.format(which);
                case FOR_FILE:
                    return date_for_file.format(which);
                case READABLE:
                    return date_readable.format(which);
                case FOR_FILE_WITH_TIME:
                    return date_for_file_with_time.format(which);
                default:
                    throw new UnsupportedOperationException(VPViewer.getStringStatic(R.string.log_pattern) + pattern + VPViewer.getStringStatic(R.string.log_notExist));
            }
        }
    }

    public static String parseToPattern(String string, DateFormat pattern) {
        return format(parse(string), pattern);
    }

    private static class Lock {

    }
}

