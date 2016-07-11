package com.faendir.kepi.vpviewer.contexts;

import android.app.Application;
import android.content.Intent;
import android.content.res.Resources;
import android.support.annotation.StringRes;

import com.faendir.kepi.vpviewer.R;
import com.faendir.kepi.vpviewer.event.InvalidateRequest;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * Created by Lukas on 14.04.2015.
 * Represents the application, initializes crash tools
 */
@ReportsCrashes(
        mode = ReportingInteractionMode.TOAST,
        httpMethod = HttpSender.Method.PUT,
        reportType = HttpSender.Type.JSON,
        formUri = "https://faendir.smileupps.com/acra-myapp-5c21de/_design/acra-storage/_update/report",
        formUriBasicAuthLogin = "vpviewer",
        formUriBasicAuthPassword = "vpR3p0rt",
        resToastText = R.string.toast_crashSent
)
public class VPViewer extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ACRA.init(this);
        ACRA.getErrorReporter().clearCustomData();
        EventBus.getDefault().register(this);
        resources = getResources();
    }

    private static Resources resources;

    public static String getStringStatic(@StringRes int res) {
        return resources.getString(res);
    }

    @Subscribe
    public void onEvent(InvalidateRequest request) {
        Intent intent = new Intent(this, UpdateService.class);
        intent.putExtra(getString(R.string.extra_isForeground), request.isForeground());
        startService(intent);
    }
}
