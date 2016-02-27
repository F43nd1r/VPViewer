package com.faendir.kepi.vpviewer.helper;

import android.app.Activity;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.annotation.DrawableRes;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.faendir.kepi.vpviewer.R;
import com.faendir.kepi.vpviewer.utils.DateFactory;
import com.faendir.kepi.vpviewer.utils.DateFormat;
import com.faendir.kepi.vpviewer.utils.LayoutManager;
import com.faendir.kepi.vpviewer.utils.PersistManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by Lukas on 18.01.2016.
 * adapter for the main fragment list view
 */
public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    private final Activity context;
    private int selected = -1;
    private List<Date> dates;
    private View empty;
    private @DrawableRes int background;

    public Adapter(Activity context, View empty) {
        super();
        dates = new ArrayList<>();
        this.context = context;
        this.empty = empty;
        setHasStableIds(true);
        TypedValue out = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, out, true);
        background = out.resourceId;
        reset();
    }

    public void set(Collection<? extends Date> collection) {
        reset();
        dates.addAll(collection);
        notifyChange();
    }

    private void notifyChange() {
        notifyDataSetChanged();
        if (getItemCount() == 0) empty.setVisibility(View.VISIBLE);
        else empty.setVisibility(View.GONE);
    }

    private boolean hasManual() {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.pref_manualView), false) && new PersistManager(context).getRaw() != null;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TextView v = (TextView) LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
        v.setGravity(Gravity.CENTER);
        v.setBackgroundResource(background);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setDate(position >= dates.size() ? null : dates.get(position));
        if (position == selected) {
            holder.txt.setBackgroundColor(Color.LTGRAY);
        } else {
            holder.txt.setBackgroundResource(background);
        }
    }

    @Override
    public long getItemId(int position) {
        if (isManual(position)) return -2;
        return dates.get(position).hashCode();
    }


    @Override
    public int getItemCount() {
        return dates.size() + (hasManual() ? 1 : 0);
    }

    private void select(int position, Date date) {
        if (date != null) {
            LayoutManager.vpFragment(context, date);
        } else if (hasManual()) {
            LayoutManager.webViewFragment(context);
        }
        if (selected != position) {
            int tmp = selected;
            selected = position;
            notifyItemChanged(tmp);
            notifyItemChanged(position);
        }
    }

    public void select(int position) {
        if (position < dates.size()) {
            select(position, dates.get(position));
        } else {
            select(position, null);
        }
    }

    public boolean isManual(int position) {
        return position == dates.size();
    }

    public void reset() {
        dates.clear();
        notifyChange();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView txt;
        private Date date;
        private String string;


        public ViewHolder(View itemView) {
            super(itemView);
            txt = (TextView) itemView;
            txt.setOnClickListener(this);
        }

        public void setDate(Date date) {
            if (date == null) {
                string = context.getString(R.string.manualView);
            } else if (this.date != date) {
                string = DateFactory.format(date, DateFormat.READABLE);
            }
            this.date = date;
            txt.setText(string);
        }

        @Override
        public void onClick(View v) {
            select(getAdapterPosition(), date);
        }
    }
}
