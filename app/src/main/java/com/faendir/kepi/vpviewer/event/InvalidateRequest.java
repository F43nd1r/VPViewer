package com.faendir.kepi.vpviewer.event;

/**
 * Created by Lukas on 16.04.2016.
 */
public class InvalidateRequest {
    private final boolean isForeground;

    public InvalidateRequest(boolean isForeground) {
        this.isForeground = isForeground;
    }

    public boolean isForeground() {
        return isForeground;
    }
}
