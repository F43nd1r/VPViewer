package com.faendir.kepi.vpviewer.helper;

import android.app.Activity;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.faendir.kepi.vpviewer.R;
import com.faendir.kepi.vpviewer.event.InvalidateRequest;
import com.faendir.kepi.vpviewer.utils.LayoutManager;
import com.faendir.kepi.vpviewer.utils.Logger;
import com.faendir.kepi.vpviewer.utils.PersistManager;

import org.solovyev.android.views.llm.LinearLayoutManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Manages the list of buttons
 *
 * Created by Lukas on 16.04.2016.
 */
public class ListManager {
    private final Logger logger = new Logger(this);
    private final Activity context;
    private final Adapter adapter;

    public ListManager(Activity context, RecyclerView recyclerView, View empty) {
        this.context = context;
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        adapter = new Adapter(context, empty);
        recyclerView.setAdapter(adapter);
        empty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InvalidateRequest.post(true);
                logger.log(R.string.log_forcedUpdate);
            }
        });
    }

    public void update() {
        List<Date> dates = new PersistManager(context).getDaysSorted();
        logger.log(context.getString(R.string.log_buttonsEnable) + Arrays.toString(dates.toArray()));
        if (dates.size() > 0) {
            Collections.reverse(dates);
            int showDays = PreferenceManager.getDefaultSharedPreferences(context).getInt(context.getString(R.string.pref_showDaysCount), 2);
            for (int i = dates.size() - 1; i >= showDays; i--) {
                dates.remove(i);
            }
            adapter.set(dates);
            if (LayoutManager.isTablet(context)) {
                adapter.select(0);
            }
            logger.log(R.string.log_buttonsEnabled);
        } else {
            adapter.reset();
        }
    }
}
