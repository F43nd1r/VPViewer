package com.faendir.kepi.vpviewer.fragments;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
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

import com.faendir.kepi.vpviewer.R;
import com.faendir.kepi.vpviewer.event.InvalidateRequest;
import com.faendir.kepi.vpviewer.event.UpdateEvent;
import com.faendir.kepi.vpviewer.helper.Dialogs;
import com.faendir.kepi.vpviewer.helper.ListManager;
import com.faendir.kepi.vpviewer.helper.StatusManager;
import com.faendir.kepi.vpviewer.legacy.Migrator;
import com.faendir.kepi.vpviewer.utils.Logger;
import com.faendir.kepi.vpviewer.utils.ServiceManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class MainFragment extends Fragment {

    private final Logger logger = new Logger(this);
    private ListManager listManager;
    private StatusManager statusManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Migrator.migrateFromOldVersionIfRequired(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        logger.log(getString(R.string.log_onCreateView));
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        boolean lowSpace = getArguments() != null && getArguments().getBoolean(getString(R.string.key_lowSpace), false);
        if (lowSpace && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            view.findViewById(R.id.logoView).setVisibility(View.GONE);
        }
        listManager = new ListManager(getActivity(), (RecyclerView) view.findViewById(R.id.recyclerView), view.findViewById(R.id.empty));
        statusManager = new StatusManager(getActivity(), (TextView) view.findViewById(R.id.textView_networkStatus), (TextView) view.findViewById(R.id.textView_updateStatus));
        updateViews();
        ServiceManager.startServiceIfRequired(getActivity());
        getActivity().findViewById(android.R.id.content).invalidate();
        return view;
    }

    private void updateViews() {
        listManager.update();
        statusManager.update();
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
            InvalidateRequest.post(true);
            logger.log(R.string.log_forcedUpdate);
            return true;
        }
        logger.log(R.string.log_fatalSel);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        updateViews();
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        logger.log(R.string.log_onStop);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpdateEvent event) {
        switch (event.getConnectionResult()) {
            case SUCCESS:
                updateViews();
                break;
            case BAD_LOGIN:
                Dialogs.showLoginFailed(getActivity());
                break;
            case CONNECTION_FAILED:
                Dialogs.showConnectionFailed(getActivity());
                break;
            case CRASH:
                Dialogs.showCrash(getActivity());
                break;
        }
    }
}
