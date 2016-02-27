package com.faendir.kepi.vpviewer.data;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Lukas on 06.12.2014.
 * Represents a Day on the VP
 */
public class Day {
    private final Date day;
    private final Date statusDate;
    private final List<VPEntry> entryList;
    private final String[] news;

    public Day(Date day, Date statusDate, List<VPEntry> entryList, @NonNull String[] news) {
        this.day = new Date(day.getTime());
        this.statusDate = new Date(statusDate.getTime());
        this.entryList = new ArrayList<>(entryList);
        this.news = news.clone();
    }

    public Date getDate() {
        return new Date(day.getTime());
    }

    public Date getStatusDate() {
        return new Date(statusDate.getTime());
    }

    public List<VPEntry> getEntryList() {
        return new ArrayList<>(entryList);
    }

    public String[] getNews() {
        return news.clone();
    }
}
