package com.faendir.kepi.vpviewer.fragments;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.faendir.kepi.vpviewer.R;
import com.faendir.kepi.vpviewer.data.Day;
import com.faendir.kepi.vpviewer.data.VPEntry;
import com.faendir.kepi.vpviewer.utils.LayoutManager;
import com.faendir.kepi.vpviewer.utils.Logger;
import com.faendir.kepi.vpviewer.utils.PersistManager;

import java.util.Date;

/**
 * Created by Lukas on 08.12.2014.
 * an activity displaying a table for one day
 */
public class VPFragment extends Fragment {


    private final Logger logger = new Logger(this);
    private TableRow.LayoutParams titleParams;
    private TableRow.LayoutParams rowParams;
    private TableRow.LayoutParams textParams;
    private int ten;
    private int five;
    private int two;
    private View view;
    private Date date;
    private int textSize;
    private SharedPreferences sharedPref;
    private boolean isSecondaryLoad = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_vp, container, false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        titleParams = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        ten = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
        five = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
        two = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
        titleParams.setMargins(ten, two, ten, two);
        rowParams = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textParams = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textParams.setMargins(ten, 0, ten, 0);
        Bundle args = getArguments();
        date = (Date) args.getSerializable(getString(R.string.key_loadDay));
        safeDisplay();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(isSecondaryLoad) safeDisplay();
    }

    @Override
    public void onPause() {
        super.onPause();
        isSecondaryLoad = true;
    }

    private void safeDisplay(){
        if (date != null) {
            display(date);
        } else {
            logger.log(getString(R.string.log_intentNoData));
            LayoutManager.mainFragment(getActivity());
        }
    }

    private void display(Date which) {
        logger.log(R.string.log_display);
        LinearLayout root = (LinearLayout) view.findViewById(R.id.root);
        TableLayout table = (TableLayout) view.findViewById(R.id.table);
        table.removeAllViews();
        Day day = new PersistManager(getActivity()).getDay(which);
        if (day == null) {
            Toast.makeText(getActivity(), getString(R.string.error_unkownDay), Toast.LENGTH_LONG).show();
            LayoutManager.mainFragment(getActivity());
            return;
        }
        textSize = sharedPref.getInt(getString(R.string.pref_textSize), 12);
        String[] news = day.getNews();
        if (news.length == 0) {
            root.removeView(view.findViewById(R.id.news_layout));
        } else {
            SpannableStringBuilder builder = new SpannableStringBuilder();
            for (String s : news) {
                builder.append(Html.fromHtml(s)).append("\n\n");
            }
            TextView title = (TextView) view.findViewById(R.id.news_title);
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
            TextView text = (TextView) view.findViewById(R.id.news_text);
            text.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
            text.setText(builder);
        }
        TableRow titleRow = new TableRow(getActivity());
        addTitleToRow(R.string.className, titleRow, Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
        addTitleToRow(R.string.hour, titleRow, Gravity.END | Gravity.BOTTOM);
        addTitleToRow(R.string.instead_teacher, titleRow, Gravity.START | Gravity.BOTTOM);
        addTitleToRow(R.string.instead_subject, titleRow, Gravity.START | Gravity.BOTTOM);
        addTitleToRow(R.string.teacher, titleRow, Gravity.START | Gravity.BOTTOM);
        addTitleToRow(R.string.subject, titleRow, Gravity.START | Gravity.BOTTOM);
        addTitleToRow(R.string.room, titleRow, Gravity.END | Gravity.BOTTOM);
        addTitleToRow(R.string.instead_hour, titleRow, Gravity.END | Gravity.BOTTOM);
        table.addView(titleRow, rowParams);
        TableRow dividerRow = new TableRow(getActivity());
        ImageView divider = new ImageView(getActivity());
        divider.setImageResource(android.R.color.darker_gray);
        TableRow.LayoutParams dividerParams = new TableRow.LayoutParams();
        dividerParams.span = 8;
        dividerParams.setMargins(five, 0, five, five);
        dividerParams.height = two;
        divider.setLayoutParams(dividerParams);
        dividerRow.addView(divider);
        table.addView(dividerRow);
        boolean shouldUseOddRows = sharedPref.getBoolean(getString(R.string.pref_oddRows), false);
        boolean isOdd = false;
        for (VPEntry entry : day.getEntryList()) {
            TableRow row = new TableRow(getActivity());
            if(isOdd) {
                row.setBackgroundColor(getResources().getColor(R.color.lighter_gray));
            }
            addToRow(entry.hours, row, Gravity.CENTER_HORIZONTAL);
            addToRow(entry.className, row, Gravity.END);
            addToRow(entry.teacherFrom, row, Gravity.START);
            addToRow(entry.subjectFrom, row, Gravity.START);
            addToRow(entry.teacherTo, row, Gravity.START);
            addToRow(entry.subjectTo, row, Gravity.START);
            addToRow(entry.room, row, Gravity.END);
            addToRow(entry.hourFrom, row, Gravity.END);
            table.addView(row, rowParams);
            isOdd = !isOdd && shouldUseOddRows;
        }
    }

    private void addTitleToRow(int textId, @NonNull TableRow row, int gravity) {
        TextView txt = new TextView(getActivity());
        txt.setText(textId);
        txt.setGravity(gravity);
        txt.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        txt.setTextColor(getResources().getColor(android.R.color.black));
        row.addView(txt, titleParams);
    }

    private void addToRow(@NonNull String text, @NonNull TableRow row, int gravity) {
        TextView txt = new TextView(getActivity());
        txt.setText(text.replace("&nbsp;", " - "));
        txt.setGravity(gravity);
        txt.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        txt.setTextColor(getResources().getColor(android.R.color.black));
        row.addView(txt, textParams);
    }

    public Date getDate() {
        return date;
    }

}
