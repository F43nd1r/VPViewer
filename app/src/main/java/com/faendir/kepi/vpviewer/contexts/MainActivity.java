package com.faendir.kepi.vpviewer.contexts;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.faendir.kepi.vpviewer.R;
import com.faendir.kepi.vpviewer.utils.LayoutManager;
import com.faendir.kepi.vpviewer.utils.Logger;

import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private final Logger logger = new Logger(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        logger.log(R.string.log_onCreate);
        LayoutManager.mainFragment(this);
        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.hasExtra(getString(R.string.key_loadDay))) {
            Date date = (Date) intent.getSerializableExtra(getString(R.string.key_loadDay));
            LayoutManager.vpFragment(this, date);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            boolean show = sharedPreferences.getBoolean(getString(R.string.pref_showAsActions), false);
            MenuItem settings = menu.findItem(R.id.action_settings);
            if (settings != null)
                settings.setShowAsActionFlags(show ? MenuItem.SHOW_AS_ACTION_IF_ROOM : MenuItem.SHOW_AS_ACTION_NEVER);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        logger.log(R.string.log_itemSelected);
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            logger.log(R.string.log_settingsSel);
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == android.R.id.home) {
            LayoutManager.mainFragment(this);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        LayoutManager.back(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
    }
}
