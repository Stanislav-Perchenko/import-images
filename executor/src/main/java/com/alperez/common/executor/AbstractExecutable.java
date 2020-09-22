package com.alperez.common.executor;

import androidx.annotation.Nullable;

import com.alperez.common.error.AppError;
import com.alperez.common.error.LocalError;
import com.alperez.common.executor.model.Callback;
import com.alperez.common.executor.model.Executable;

/**
 * Created by stanislav.perchenko on 22.09.2020 at 22:10.
 */
public abstract class AbstractExecutable<T> implements Executable {
    private static int nextSequenceNumberHolder = 1;

    private int sequenceNumber;
    private Callback<T> callback;
    private volatile boolean subscribed;

    private T result;
    private AppError error;
    private boolean processed;

    private BackgroundExecutor executor;


    protected AbstractExecutable() {
        sequenceNumber = nextSequenceNumberHolder++;
    }

    public AbstractExecutable<T> on(@Nullable BackgroundExecutor executor) {
        this.executor = executor;
        return this;
    }


    public AbstractExecutable<T> executeAsync(@Nullable Callback<T> callback) {
        this.callback = callback;
        subscribed = true;
        if (callback != null) callback.onSubscribe(this);
        if (executor != null) {
            ExecutorLogger.logWithThread(ExecutorLogger.buildMessage(this, "executeAsync", "start execution on Executor instance"));
            // Run on the Executor's thread
            executor.execute(this);
        } else {
            // Execute on the same thread
            ExecutorLogger.logWithThread(ExecutorLogger.buildMessage(this, "executeAsync", "start execution in the same Thread"));
            executeInThisThread();
        }
        return this;
    }

    private void executeInThisThread() {
        try {
            executeSynchronously();
        } catch (Exception e) {
            onExceptionWhileExecuted(e);
        }
        onExecuted();
    }

    @Override
    public int getSequenceNumber() {
        return sequenceNumber;
    }

    @Override
    public void dispose() {
        ExecutorLogger.logWithThread(ExecutorLogger.buildMessage(this, "unsubscribe", "Cancel request. N="+getSequenceNumber()));
        subscribed = false;
    }

    @Override
    public boolean isDisposed() {
        return !subscribed;
    }

    /**
     * This method sets result of a processing action. Must be called
     * from the executeAsync() before return, in case of successfull processing
     * @param result
     */
    protected void setResult(T result) {
        ExecutorLogger.logWithThread(ExecutorLogger.buildMessage(this, "setResult", "Got a result - "+result+",  N="+getSequenceNumber()));
        this.result = result;
        processed = true;
    }

    protected void setError(AppError error) {
        ExecutorLogger.logWithThread(ExecutorLogger.buildMessage(this, "setError", "Got an error - " + error.getInternalMessage()+",  N="+getSequenceNumber()));
        this.error = error;
        processed = true;
    }

    public boolean isProcessed() {
        return processed;
    }

    public T getResult() {
        return result;
    }

    public AppError getError() {
        return error;
    }

    @Override
    public void onExceptionWhileExecuted(Exception e) {
        e.printStackTrace();
        ExecutorLogger.logWithThread(ExecutorLogger.buildMessage(this, "onExceptionWhileExecuted", e.toString()+",  N="+getSequenceNumber()));
        error = LocalError.Companion.create(null, e);
        result = null;
        processed = true;
    }

    /**
     * This method is called from a processor to inform a client
     * of the result of processing. This method fires the Callback's methods
     * depending on the result of processing.
     * The right way is to call this method from the UI thread, so the client can safely
     * operate with results.
     */
    @Override
    public void onExecuted() {
        ExecutorLogger.logWithThread(ExecutorLogger.buildMessage(this, "onExecuted", String.format("Ready to notify a client. processed=%b, subscribed=%b. N=%d", processed, !isDisposed(), getSequenceNumber())));
        if (!processed) {
            throw new IllegalStateException("Result was not set in the executeAsync() call");
        } else {

            if (callback != null && !isDisposed()) {
                if (error != null) {
                    callback.onError( error);
                } else {
                    callback.onSuccess(result);
                }
                callback.onComplete(this);
            } else if (isDisposed()) {
                ExecutorLogger.logWithThread(ExecutorLogger.buildMessage(this, "onExecuted", "!!!!!  The Executable is unsubscribed -> skip callback!  N="+getSequenceNumber()));
            }

        }
    }
}
