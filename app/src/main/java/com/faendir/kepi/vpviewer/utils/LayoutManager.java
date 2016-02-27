package com.faendir.kepi.vpviewer.utils;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.AnimatorRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.faendir.kepi.vpviewer.R;
import com.faendir.kepi.vpviewer.fragments.BlankFragment;
import com.faendir.kepi.vpviewer.fragments.MainFragment;
import com.faendir.kepi.vpviewer.fragments.VPFragment;
import com.faendir.kepi.vpviewer.fragments.WebViewFragment;

import java.util.Date;
import java.util.Set;

/**
 * Created by Lukas on 18.01.2016.
 * manages global layout
 */
public final class LayoutManager {
    private static final String TAG_MAIN = "main";
    private static final String TAG_SECONDARY = "secondary";

    private LayoutManager() {
    }

    public static void mainFragment(Activity context) {
        Fragment fragment = new MainFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(context.getString(R.string.key_lowSpace), !isTablet(context));
        fragment.setArguments(bundle);
        replace(context, R.id.mainLayout, fragment, TAG_MAIN, true);
        if (isTablet(context)) {
            replace(context, R.id.secondaryLayout, new BlankFragment(), TAG_SECONDARY);
        }
        setHomeAsUpEnabled(context, false);
        context.setTitle(context.getString(R.string.app_name));
    }

    public static void vpFragment(Activity context, @NonNull Date date) {
        Fragment fragment = new VPFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(context.getString(R.string.key_loadDay), date);
        fragment.setArguments(bundle);
        FragmentManager manager = context.getFragmentManager();
        Fragment secondary = manager.findFragmentByTag(TAG_SECONDARY);
        Date oldDate;
        boolean isDownNavigation = secondary instanceof WebViewFragment
                || (secondary instanceof VPFragment
                && (oldDate = ((VPFragment) secondary).getDate()) != null && oldDate.before(date));
        loadSecondaryIfTablet(context, fragment, isDownNavigation);
        context.setTitle(DateFactory.format(date, DateFormat.READABLE));
    }

    public static void webViewFragment(Activity context) {
        loadSecondaryIfTablet(context, new WebViewFragment(), false);
        context.setTitle(context.getString(R.string.manualView));
    }

    public static void back(Activity context) {
        if (context.getFragmentManager().findFragmentByTag(TAG_MAIN) instanceof MainFragment) {
            context.finish();
        } else {
            mainFragment(context);
        }
    }

    private static void loadSecondaryIfTablet(Activity context, Fragment fragment, boolean isDownNavigation) {
        if (isTablet(context)) {
            int in;
            int out;
            if (isLandscape(context)) {
                if (isDownNavigation) {
                    in = R.animator.slide_in_down;
                    out = R.animator.slide_out_down;
                } else {
                    in = R.animator.slide_in_up;
                    out = R.animator.slide_out_up;
                }
            } else if (isDownNavigation) {
                in = R.animator.slide_in_right;
                out = R.animator.slide_out_right;
            } else {
                in = R.animator.slide_in_left;
                out = R.animator.slide_out_left;
            }
            replace(context, R.id.secondaryLayout, fragment, TAG_SECONDARY, false, in, out);
        } else {
            replace(context, R.id.mainLayout, fragment, TAG_MAIN, false);
            setHomeAsUpEnabled(context, true);
        }
    }

    private static void replace(Activity context, @IdRes int id, Fragment fragment, String tag) {
        replace(context, id, fragment, tag, true);
    }

    private static void replace(Activity context, @IdRes int id, Fragment fragment, String tag, boolean isDownNavigation) {
        replace(context, id, fragment, tag, isDownNavigation, null, null);

    }

    private static void replace(Activity context, @IdRes int id, Fragment fragment, String tag, boolean isDownNavigation, @Nullable @AnimatorRes Integer in, @Nullable @AnimatorRes Integer out) {
        FragmentManager manager = context.getFragmentManager();
        Fragment f = manager.findFragmentByTag(tag);
        if (f != null) {
            if (f.getClass() == fragment.getClass() && equalBundles(f.getArguments(), fragment.getArguments()))
                return;
            if (f.isVisible()) {
                if (in == null) {
                    in = isDownNavigation ? R.animator.slide_in_right : R.animator.slide_in_left;
                }
                if (out == null) {
                    out = isDownNavigation ? R.animator.slide_out_right : R.animator.slide_out_left;
                }
            }
        }
        if (in == null) in = 0;
        if (out == null) out = 0;
        manager.beginTransaction().setCustomAnimations(in, out).replace(id, fragment, tag).commit();
    }

    private static boolean equalBundles(Bundle one, Bundle two) {
        if (one == null) return two == null;
        if (one.size() != two.size())
            return false;

        Set<String> setOne = one.keySet();
        Object valueOne;
        Object valueTwo;

        for (String key : setOne) {
            valueOne = one.get(key);
            valueTwo = two.get(key);
            if (valueOne instanceof Bundle && valueTwo instanceof Bundle &&
                    !equalBundles((Bundle) valueOne, (Bundle) valueTwo)) {
                return false;
            } else if (valueOne == null) {
                if (valueTwo != null || !two.containsKey(key))
                    return false;
            } else if (!valueOne.equals(valueTwo))
                return false;
        }
        return true;
    }


    public static boolean isTablet(Activity context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE && context.findViewById(R.id.secondaryLayout) != null;
    }

    private static boolean isLandscape(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    private static void setHomeAsUpEnabled(Activity context, boolean value) {
        ActionBar actionBar = context.getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(value);
        } else if (context instanceof AppCompatActivity) {
            android.support.v7.app.ActionBar supportActionBar = ((AppCompatActivity) context).getSupportActionBar();
            if (supportActionBar != null) {
                supportActionBar.setDisplayHomeAsUpEnabled(value);
            }
        }
    }
}
