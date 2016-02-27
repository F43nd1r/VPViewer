package com.faendir.kepi.vpviewer.data;

import android.content.Context;
import android.support.annotation.NonNull;

import com.faendir.kepi.vpviewer.R;

/**
 * Created by Lukas on 06.12.2014.
 * Single line in VP
 */
public class VPEntry {
    @NonNull
    private final String NOT_TAKING_PLACE;
    public final String hours;
    public final String className;
    public final String teacherFrom;
    public final String subjectFrom;
    public final String teacherTo;
    public final String subjectTo;
    public final String room;
    public final String hourFrom;

    public VPEntry(@NonNull Context context, String hours, String className, String teacherFrom, String subjectFrom, String teacherTo, String subjectTo, String room, String hourFrom) {
        this.hourFrom = hourFrom;
        this.NOT_TAKING_PLACE = context.getString(R.string.not_taking_place);
        this.hours = hours;
        this.className = className;
        this.teacherFrom = teacherFrom;
        this.subjectFrom = subjectFrom;
        this.teacherTo = teacherTo;
        this.subjectTo = subjectTo;
        this.room = room;
    }

    @Override
    public boolean equals(@NonNull Object o) {
        if (o.getClass() != getClass()) return false;
        VPEntry d = (VPEntry) o;
        return this.hours.equals(d.hours) &&
                this.className.equals(d.className) &&
                this.teacherFrom.equals(d.teacherFrom) &&
                this.subjectFrom.equals(d.subjectFrom) &&
                this.teacherTo.equals(d.teacherTo) &&
                this.subjectTo.equals(d.subjectTo) &&
                this.room.equals(d.room);
    }

    @Override
    public int hashCode() {
        return hours.hashCode() + className.hashCode() + teacherFrom.hashCode() + subjectFrom.hashCode() + teacherTo.hashCode() + subjectTo.hashCode() + room.hashCode();
    }

    @NonNull
    @Override
    public String toString() {
        if (teacherTo.equals("---") && subjectTo.equals("---") && room.equals("---"))
            return className + ", " + hours + ": " + subjectFrom + " (" + teacherFrom + ") " + NOT_TAKING_PLACE;
        return className + ", " + hours + ": " + subjectFrom + " (" + teacherFrom + ") | " + subjectTo + " (" + teacherTo + ", " + room + ")";
    }
}
