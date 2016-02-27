package com.faendir.kepi.vpviewer.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.faendir.kepi.vpviewer.R;

public class PrefsFragment extends PreferenceFragment {

    public PrefsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
    }

}