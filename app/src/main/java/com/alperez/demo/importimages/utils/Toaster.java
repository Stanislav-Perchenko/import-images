package com.alperez.demo.importimages.utils;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.alperez.common.error.AppError;
import com.alperez.demo.importimages.BuildConfig;
import com.google.android.material.snackbar.Snackbar;

/**
 * Created by stanislav.perchenko on 12.08.2020 at 18:50.
 */
public final class Toaster {

    public static void showSnackbar(View container, final String text) {
        if (container != null) {
            Snackbar.make(container, text, Snackbar.LENGTH_LONG).show();
        }
    }

    public static Snackbar showSnackbar(View container, final int mainTextStringId, final int actionStringId, View.OnClickListener listener) {
        Context ctx = container.getContext();
        Snackbar sb = Snackbar.make(container, ctx.getString(mainTextStringId), Snackbar.LENGTH_INDEFINITE)
                .setAction(ctx.getString(actionStringId), listener);
        sb.show();
        return sb;
    }

    public static Snackbar showSnackbar(View container, final String mainText, final int actionStringId, View.OnClickListener listener) {
        Context ctx = container.getContext();
        Snackbar sb = Snackbar.make(container, mainText, Snackbar.LENGTH_INDEFINITE)
                .setAction(ctx.getString(actionStringId), listener);
        sb.show();
        return sb;
    }

    public static void toastToUser(Context c, int textResId) {
        Toast.makeText(c, c.getString(textResId), Toast.LENGTH_SHORT).show();
    }

    public static void toastErrorLong(Context c, int textResId, AppError err) {
        toastError(c, textResId, err, Toast.LENGTH_LONG);
    }

    private static void toastError(Context c, int textResId, AppError err, int lenght) {
        String textError = composeErrorText(err);
        Toast.makeText(c, c.getString(textResId, textError), lenght).show();
    }

    private static void toastError(Context c, String template, AppError err, int lenght) {
        String textError = composeErrorText(err);
        Toast.makeText(c, String.format(template, textError), lenght).show();
    }

    private static void toastError(Context c, AppError err, int lenght) {
        String textError = composeErrorText(err);
        Toast.makeText(c, textError, lenght).show();
    }

    private static String composeErrorText(AppError err) {
        return BuildConfig.DEBUG
                ? TextUtils.isEmpty(err.getInternalMessage()) ? err.getUserMessage() : String.format("%s (%s)", err.getUserMessage(), err.getInternalMessage())
                : err.getUserMessage();
    }

    private Toaster() { }

}
