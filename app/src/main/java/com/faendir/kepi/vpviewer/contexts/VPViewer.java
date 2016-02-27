package com.faendir.kepi.vpviewer.contexts;

import android.app.Application;
import android.content.Context;
import android.support.annotation.StringRes;

import com.faendir.kepi.vpviewer.R;
import com.faendir.kepi.vpviewer.utils.Logger;

import org.acra.ACRA;
import org.acra.ErrorReporter;
import org.acra.ExceptionHandlerInitializer;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;

/**
 * Created by Lukas on 14.04.2015.
 * Represents the application, initializes crash tools
 */
@ReportsCrashes(
        httpMethod = HttpSender.Method.PUT,
        reportType = HttpSender.Type.JSON,
        formUri = "https://faendir.smileupps.com/acra-myapp-5c21de/_design/acra-storage/_update/report",
        formUriBasicAuthLogin = "vpviewer",
        formUriBasicAuthPassword = "vpR3p0rt"
)
public class VPViewer extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ACRA.init(this);
        ACRA.getErrorReporter().clearCustomData();
        ACRA.getErrorReporter().setExceptionHandlerInitializer(new ExceptionHandlerInitializer() {
            @Override
            public void initializeExceptionHandler(ErrorReporter reporter) {
                reporter.putCustomData(getString(R.string.key_log), Logger.globalLog());
            }
        });
        setContext(getApplicationContext());
    }

    private static Context context;

    public static String getStringStatic(@StringRes int res) {
        return context.getString(res);
    }

    private static void setContext(Context c) {
        context = c;
    }
}
