package com.faendir.kepi.vpviewer.fragments;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.faendir.kepi.vpviewer.BuildConfig;
import com.faendir.kepi.vpviewer.R;
import com.faendir.kepi.vpviewer.contexts.SettingsActivity;
import com.faendir.kepi.vpviewer.contexts.UpdateService;
import com.faendir.kepi.vpviewer.helper.Adapter;
import com.faendir.kepi.vpviewer.utils.ConnectionResult;
import com.faendir.kepi.vpviewer.utils.DateFactory;
import com.faendir.kepi.vpviewer.utils.LayoutManager;
import com.faendir.kepi.vpviewer.utils.Logger;
import com.faendir.kepi.vpviewer.utils.PersistManager;
import com.faendir.kepi.vpviewer.utils.ServiceManager;

import org.solovyev.android.views.llm.LinearLayoutManager;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;


public class MainFragment extends Fragment {

    private final Logger logger = new Logger(MainFragment.class.getSimpleName());
    private UpdateService updateService;
    private boolean bound = false;
    private final CustomServiceConnection connection = new CustomServiceConnection();
    private View view;
    private Adapter adapter;
    private SharedPreferences sharedPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        try {
            String currentVersion = String.valueOf(getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionCode);
            String version = sharedPref.getString(getString(R.string.key_runningVersion), null);
            if (version == null || !version.equals(currentVersion)) {
                updateToCurrentVersion(currentVersion);
            }
        } catch (PackageManager.NameNotFoundException e) {
            logger.log(e);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        logger.log(getString(R.string.log_onCreateView));
        view = inflater.inflate(R.layout.fragment_main, container, false);
        boolean lowSpace = getArguments() != null && getArguments().containsKey(getString(R.string.key_lowSpace)) && getArguments().getBoolean(getString(R.string.key_lowSpace));
        if (lowSpace && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            view.findViewById(R.id.logoView).setVisibility(View.GONE);
        }
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        View empty = view.findViewById(R.id.empty);
        adapter = new Adapter(getActivity(), empty);
        recyclerView.setAdapter(adapter);
        empty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forcedUpdate();
            }
        });
        Date update = statusUpdate();
        if (update != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(update);
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            cal.add(Calendar.MILLISECOND, Integer.parseInt(sharedPreferences.getString(getString(R.string.pref_interval), getString(R.string.update_default))));
            if (cal.before(Calendar.getInstance())) {
                ServiceManager.bindService(getActivity(), connection);
                logger.log(R.string.log_dataOutOfDate);
            }
        } else {
            ServiceManager.bindService(getActivity(), connection);
            logger.log(R.string.log_noData);
        }
        getActivity().findViewById(android.R.id.content).invalidate();
        return view;
    }

    private Date statusUpdate() {
        setButtons();
        logger.log(R.string.log_statusUpdate);
        TextView status = (TextView) view.findViewById(R.id.textView_networkStatus);
        ConnectivityManager connManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifi.isConnected()) status.setText(R.string.wifi_ok);
        else {
            if (sharedPref.getBoolean(getString(R.string.pref_wifiOnly), false))
                status.setText(R.string.wifi_fail);
            NetworkInfo mobile = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (mobile.isConnected()) status.setText(R.string.network_ok);
            else status.setText(R.string.network_fail);
        }
        status.invalidate();
        TextView update = (TextView) view.findViewById(R.id.textView_updateStatus);
        String lastUpdate = sharedPref.getString(getString(R.string.key_update), null);
        Date result = null;
        if (lastUpdate == null) {
            update.setText(getString(R.string.last_update) + getString(R.string.text_colon) + getString(R.string.no_update));
        } else {
            update.setText(getString(R.string.last_update) + getString(R.string.text_colon) + lastUpdate);
            result = DateFactory.parse(lastUpdate);
        }
        update.invalidate();
        return result;
    }

    private void startService() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (sharedPreferences.getBoolean(getString(R.string.pref_notify), false)) {
            ServiceManager.startService(getActivity(), Integer.parseInt(sharedPreferences.getString(getActivity().getString(R.string.pref_interval), String.valueOf(AlarmManager.INTERVAL_HOUR))));
        }
    }

    private void forcedUpdate() {
        if (bound) connection.readIn();
        else {
            ServiceManager.bindService(getActivity(), connection);
            logger.log(R.string.log_forcedUpdate);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main_fragment, menu);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean show = sharedPreferences.getBoolean(getString(R.string.pref_showAsActions), false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            MenuItem update = menu.findItem(R.id.action_force_update);
            if (update != null)
                update.setShowAsActionFlags(show ? MenuItem.SHOW_AS_ACTION_IF_ROOM : MenuItem.SHOW_AS_ACTION_NEVER);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        logger.log(R.string.log_itemSelected);
        int id = item.getItemId();

        if (id == R.id.action_force_update) {
            logger.log(R.string.log_updateSel);
            forcedUpdate();
            return true;
        }
        logger.log(R.string.log_fatalSel);
        return super.onOptionsItemSelected(item);
    }

    private void setButtons() {
        List<Date> dates = new PersistManager(getActivity()).getDaysSorted();
        logger.log(getString(R.string.log_buttonsEnable) + Arrays.toString(dates.toArray()));
        if (dates.size() > 0) {
            Collections.reverse(dates);
            int showDays = sharedPref.getInt(getString(R.string.pref_showDaysCount), 2);
            for (int i = dates.size() - 1; i >= showDays; i--) {
                dates.remove(i);
            }
            adapter.set(dates);
            if (LayoutManager.isTablet(getActivity())) {
                adapter.select(0);
            }
            logger.log(R.string.log_buttonsEnabled);
        } else {
            adapter.reset();
        }
    }

    private void showLoginFailed() {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.title_badLogin)
                .setMessage(R.string.Login_failed)
                .setPositiveButton(R.string.button_login, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent(getActivity(), SettingsActivity.class));
                    }
                })
                .setNegativeButton(R.string.button_close, null)
                .show();
    }

    private void showConnectionFailed() {
        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.title_badConnection))
                .setMessage(getString(R.string.text_badConection))
                .setNegativeButton(R.string.button_close, null)
                .show();

    }

    private void showCrash() {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.title_crash)
                .setMessage(R.string.text_crash)
                .setNegativeButton(R.string.button_close, null)
                .show();
    }

    private void updateToCurrentVersion(String currentVersion) {
        PreferenceManager.setDefaultValues(getActivity(), R.xml.pref_general, false);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
        startService();
        logger.log(getString(R.string.log_versionUpdated) + currentVersion);
        if (!BuildConfig.DEBUG) {
            editor.putBoolean(getString(R.string.pref_enableAcra), true);
        }
        editor.putBoolean(getString(R.string.pref_manualView), false);
        forcedUpdate();
        editor.putString(getString(R.string.key_runningVersion), currentVersion);
        editor.apply();
    }

    @Override
    public void onResume() {
        super.onResume();
        statusUpdate();
    }

    @Override
    public void onStop() {
        super.onStop();
        logger.log(R.string.log_onStop);
        if (bound) {
            ServiceManager.unbindService(getActivity(), connection);
            bound = false;
        }
    }

    public class CustomServiceConnection implements ServiceConnection {

        private AsyncTask<Void, Void, ConnectionResult> task;

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            logger.log(R.string.log_serviceConnected);
            UpdateService.LocalBinder binder = (UpdateService.LocalBinder) service;
            updateService = binder.getService();
            bound = true;
            readIn();
        }

        public void readIn() {
            logger.log(R.string.log_readIn);
            task = new AsyncTask<Void, Void, ConnectionResult>() {
                @Override
                protected ConnectionResult doInBackground(Void... params) {
                    ConnectionResult result = updateService.getAndHandleRawHtml();
                    if (isCancelled()) return ConnectionResult.CANCELLED;
                    return result;
                }

                @Override
                protected void onPostExecute(@NonNull ConnectionResult result) {
                    if (isAdded()) {
                        switch (result) {
                            case SUCCESS:
                                statusUpdate();
                                break;
                            case BAD_LOGIN:
                                showLoginFailed();
                                break;
                            case CONNECTION_FAILED:
                                showConnectionFailed();
                                break;
                            case CRASH:
                                showCrash();
                                break;
                            case CANCELLED:
                                //empty
                                break;
                        }
                    }
                }
            }.execute();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            logger.log(R.string.log_serviceDisconnected);
            bound = false;
            if (task != null && task.getStatus() != AsyncTask.Status.FINISHED) task.cancel(true);
        }

    }
}
