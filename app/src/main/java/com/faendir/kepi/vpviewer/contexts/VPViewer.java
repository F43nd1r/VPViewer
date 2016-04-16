package com.faendir.kepi.vpviewer.contexts;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.StringRes;

import com.faendir.kepi.vpviewer.R;
import com.faendir.kepi.vpviewer.event.InvalidateRequest;
import com.faendir.kepi.vpviewer.utils.Logger;

import org.acra.ACRA;
import org.acra.ACRAConstants;
import org.acra.ReportField;
import org.acra.annotation.ReportsCrashes;
import org.acra.config.ConfigurationBuilder;
import org.acra.sender.HttpSender;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Arrays;

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
        ArrayList<ReportField> fields = new ArrayList<>(Arrays.asList(ACRAConstants.DEFAULT_REPORT_FIELDS));
        fields.add(ReportField.APPLICATION_LOG);
        ConfigurationBuilder builder = new ConfigurationBuilder(this)
                .setApplicationLogFile(Logger.getPath())
                .setCustomReportContent(fields.toArray(new ReportField[fields.size()]));
        ACRA.init(this, builder.build());
        ACRA.getErrorReporter().clearCustomData();
        EventBus.getDefault().register(this);
        setContext(getApplicationContext());
    }

    private static Context context;

    public static String getStringStatic(@StringRes int res) {
        return context.getString(res);
    }

    private static void setContext(Context c) {
        context = c;
    }

    @Subscribe
    public void onEvent(InvalidateRequest request) {
        Intent intent = new Intent(this, UpdateService.class);
        intent.putExtra(getString(R.string.extra_isForeground), request.isForeground());
        startService(intent);
    }
}
