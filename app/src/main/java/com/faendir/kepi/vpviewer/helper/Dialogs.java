package com.faendir.kepi.vpviewer.helper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import com.faendir.kepi.vpviewer.R;
import com.faendir.kepi.vpviewer.contexts.SettingsActivity;

/**
 * Creates dialogs
 *
 * Created by Lukas on 16.04.2016.
 */
public final class Dialogs {
    private Dialogs() {
    }

    public static void showLoginFailed(final Context context) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.title_badLogin)
                .setMessage(R.string.Login_failed)
                .setPositiveButton(R.string.button_login, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        context.startActivity(new Intent(context, SettingsActivity.class));
                    }
                })
                .setNegativeButton(R.string.button_close, null)
                .show();
    }

    public static void showConnectionFailed(Context context) {
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.title_badConnection))
                .setMessage(context.getString(R.string.text_badConection))
                .setNegativeButton(R.string.button_close, null)
                .show();

    }

    public static void showCrash(Context context) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.title_crash)
                .setMessage(R.string.text_crash)
                .setNegativeButton(R.string.button_close, null)
                .show();
    }
}
