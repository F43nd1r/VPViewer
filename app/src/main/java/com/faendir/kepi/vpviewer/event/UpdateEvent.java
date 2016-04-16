package com.faendir.kepi.vpviewer.event;

import com.faendir.kepi.vpviewer.utils.ConnectionResult;

/**
 * Event after data has changed
 * Created by Lukas on 16.04.2016.
 */
public class UpdateEvent {
    private final ConnectionResult connectionResult;

    public UpdateEvent(ConnectionResult connectionResult) {
        this.connectionResult = connectionResult;
    }

    public ConnectionResult getConnectionResult() {
        return connectionResult;
    }
}
