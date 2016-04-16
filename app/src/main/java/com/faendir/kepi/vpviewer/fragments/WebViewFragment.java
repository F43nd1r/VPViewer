package com.faendir.kepi.vpviewer.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.faendir.kepi.vpviewer.R;
import com.faendir.kepi.vpviewer.contexts.UpdateService;
import com.faendir.kepi.vpviewer.utils.Logger;
import com.faendir.kepi.vpviewer.utils.PersistManager;

public class WebViewFragment extends Fragment {

    private WebView webView;
    private final Logger logger = new Logger(this);

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        logger.log(getString(R.string.log_onCreateView));
        View v = inflater.inflate(R.layout.fragment_web, container, false);
        webView = (WebView) v.findViewById(R.id.webView);
        webView.getSettings().setLoadWithOverviewMode(true);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        logger.log(getString(R.string.log_onResume));
        webView.loadDataWithBaseURL(UpdateService.HOST + UpdateService.PAGE, new PersistManager(getActivity()).getRaw(), "plain/html", "utf-8", null);
    }
}
