package com.alperez.importimages.util;

import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.NonNull;


import java.lang.ref.WeakReference;

/**
 * Created by stanislav.perchenko on 22.09.2020 at 21:50.
 */
public class BasePresenter<TView> {

    public static void assertMainThread() {
        final String t_name = Thread.currentThread().getName();
        if (!TextUtils.equals(t_name, Looper.getMainLooper().getThread().getName())) {
            throw new RuntimeException("Not the Main thread - " + t_name);
        }
    }

    private final WeakReference<TView> viewRef;
    private volatile boolean released;

    protected BasePresenter(@NonNull TView view) {
        if (view == null) {
            throw new IllegalArgumentException("A valid instance of the View interface must be provided");
        }
        viewRef = new WeakReference<>(view);
    }

    protected TView getView() {
        return viewRef.get();
    }

    /**
     * This method starts initialization process of prepared UI.
     * It must be called from the Activity's onPostCreate()
     */
    public void initialize() {
    }

    /**
     * This method must be called from Activity's onDestroy()
     */
    public synchronized void release() {
        released = true;
        viewRef.clear();
    }

    public synchronized boolean isReleased() {
        return released;
    }

    /**
     * This method checks if the presentor has been released already
     * and throws IllegalSztateException if so.
     * subclasses must call this method before start execution of any operation.
     */
    protected synchronized void checkReleased() {
        if (released) {
            throw new IllegalStateException("Already released!");
        }
    }


}