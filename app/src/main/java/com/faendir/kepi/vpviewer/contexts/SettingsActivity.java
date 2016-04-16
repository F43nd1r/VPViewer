package com.faendir.kepi.vpviewer.contexts;

import android.app.AlarmManager;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.faendir.kepi.vpviewer.BuildConfig;
import com.faendir.kepi.vpviewer.R;
import com.faendir.kepi.vpviewer.fragments.PrefsFragment;
import com.faendir.kepi.vpviewer.utils.Logger;
import com.faendir.kepi.vpviewer.utils.PersistManager;
import com.faendir.kepi.vpviewer.utils.ServiceManager;

import java.util.Arrays;

/**
 * Created by Lukas on 07.12.2014.
 * Activity displaying the settings
 */
public class SettingsActivity extends AppCompatActivity {

    private final Logger logger = new Logger(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logger.log(R.string.log_onCreate);
        setContentView(R.layout.activity_settings);
    }
}
