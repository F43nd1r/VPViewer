package com.faendir.kepi.vpviewer.views;

/**
 * Created by Lukas on 07.12.2014.
 * a preference object showing the current version
 */

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;

class VersionPreference extends Preference {

    public VersionPreference(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
        String versionName;
        final PackageManager packageManager = context.getPackageManager();
        if (packageManager != null) {
            try {
                PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
                versionName = packageInfo.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                versionName = null;
            }
            setSummary(versionName);
        }
    }
}
