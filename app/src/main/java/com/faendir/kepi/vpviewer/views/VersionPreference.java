package com.faendir.kepi.vpviewer.views;

/**
 * Created by Lukas on 07.12.2014.
 * a preference object showing the current version
 */

import android.content.Context;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;

import com.faendir.kepi.vpviewer.BuildConfig;

class VersionPreference extends Preference {

    public VersionPreference(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
        setSummary(BuildConfig.VERSION_NAME);
    }
}
