package com.alperez.demo.importimages.utils;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.Build;
import android.webkit.WebView;

import androidx.annotation.Nullable;

/**
 * Created by stanislav.perchenko on 22.09.2020 at 20:11.
 */
public final class ViewUtils {

    private ViewUtils() { }


    public static void clearWebView(WebView webView) {
        if (Build.VERSION.SDK_INT < 18) {
            webView.clearView();
        } else {
            webView.loadUrl("about:blank");
        }
    }

    @SuppressLint("NewApi")
    public static int getColorFromResourcesCompat(Resources res, int resId, @Nullable Resources.Theme theme) {
        return (Build.VERSION.SDK_INT >= 23) ? res.getColor(resId, theme) : res.getColor(resId);
    }
}