package com.faendir.kepi.vpviewer.event;

import org.greenrobot.eventbus.EventBus;

/**
 * Request to reload data from the server
 * Created by Lukas on 16.04.2016.
 */
public class InvalidateRequest {
    private final boolean isForeground;

    private InvalidateRequest(boolean isForeground) {
        this.isForeground = isForeground;
    }

    public boolean isForeground() {
        return isForeground;
    }

    public static void post(boolean isForeground){
        if(EventBus.getDefault().getStickyEvent(InvalidateRequest.class) == null){
            EventBus.getDefault().postSticky(new InvalidateRequest(isForeground));
        }
    }

    public static void dispose(){
        EventBus.getDefault().removeStickyEvent(InvalidateRequest.class);
    }
}
