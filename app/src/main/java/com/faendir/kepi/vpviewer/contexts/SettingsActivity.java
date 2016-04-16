package com.faendir.kepi.vpviewer.contexts;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.faendir.kepi.vpviewer.R;
import com.faendir.kepi.vpviewer.utils.Logger;

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
