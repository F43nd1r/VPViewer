package com.faendir.kepi.vpviewer.event;

/**
 * Request to reload data from the server
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
