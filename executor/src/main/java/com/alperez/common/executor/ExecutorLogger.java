package com.alperez.common.executor;

import android.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by stanislav.perchenko on 22.09.2020 at 22:11.
 */
public final class ExecutorLogger {
    private ExecutorLogger() {
        throw new IllegalStateException("No instances");
    }

    private static final AtomicBoolean isDebuggable = new AtomicBoolean(false);

    public static void serDebuggable(boolean isDebuggable) {
        ExecutorLogger.isDebuggable.set(isDebuggable);
    }

    static String buildMessage(Object obj, String methodName, String message) {
        return isDebuggable.get() ? String.format("%s@%s(): %s", obj.getClass().getSimpleName(), methodName, message) : "";
    }

    static void logWithThread(String message) {
        String thName = Thread.currentThread().getName();
        log(String.format("(%s) -> %s", thName, message));
    }

    private static void log(String message) {
        if(isDebuggable.get()) Log.d("CoreTest", message);
    }
}
