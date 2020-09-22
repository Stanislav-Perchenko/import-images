package com.alperez.common.executor;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.alperez.common.executor.model.Executable;

/**
 * Created by stanislav.perchenko on 22.09.2020 at 22:11.
 */
public class BackgroundExecutor {

    private HandlerThread workerThread;
    private Handler workerHandler;
    private ResultHandler resultHandler;

    private boolean released;


    public BackgroundExecutor(Looper resultLooper) {

        this.resultHandler = new ResultHandler(resultLooper);
        workerThread = new HandlerThread(getClass().getSimpleName()) {
            @Override
            public void run() {
                super.run();
            }
        };
        workerThread.start();
        workerHandler = new Handler(workerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                runExecutable((Executable) msg.obj);
            }
        };
    }

    private void runExecutable(Executable exec) {
        ExecutorLogger.logWithThread(ExecutorLogger.buildMessage(this, "runExecutable", "Worker Handler received message. N="+exec.getSequenceNumber()));
        if (!exec.isDisposed()) {
            try {
                exec.executeSynchronously();
            } catch (Exception e) {
                ExecutorLogger.logWithThread(ExecutorLogger.buildMessage(this, "runExecutable", "Worker Handler exception while execution N="+exec.getSequenceNumber())+".  "+e.getMessage());
                exec.onExceptionWhileExecuted(e);
            }
            ExecutorLogger.logWithThread(ExecutorLogger.buildMessage(this, "runExecutable", "Worker Handler completed execution. N="+exec.getSequenceNumber()));
            resultHandler.notifyExecuted(exec);
        } else {
            ExecutorLogger.logWithThread(ExecutorLogger.buildMessage(this, "runExecutable", "!!!!!  The Executable is unsubscribed -> skip it! N="+exec.getSequenceNumber()));
        }
    }


    public synchronized void execute(Executable exec) {
        if (!released) {
            ExecutorLogger.logWithThread(ExecutorLogger.buildMessage(this, "executeAsync", "BG executor start with the - "+exec.getClass().getSimpleName())+". N="+exec.getSequenceNumber());
            workerHandler.sendMessage(workerHandler.obtainMessage(0, exec));
        } else {
            throw new IllegalStateException("The executor already released");
        }
    }

    public synchronized void release() {
        if (!released) {
            workerThread.quit();
            released = true;
        }
    }


    /**********************************************************************************************/
    private class ResultHandler extends Handler {

        public ResultHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_EXECUTED) {
                Executable exec = (Executable) msg.obj;
                ExecutorLogger.logWithThread(ExecutorLogger.buildMessage(this, "handleMessage", "Result Handler received message. N="+exec.getSequenceNumber()));
                exec.onExecuted();
            } else {
                super.handleMessage(msg);
            }
        }

        public void notifyExecuted(Executable exec) {
            ExecutorLogger.logWithThread(ExecutorLogger.buildMessage(this, "notifyExecuted", "Notify Result Handler. N="+exec.getSequenceNumber()));
            sendMessage(obtainMessage(MSG_EXECUTED, exec));
        }

        private static final int MSG_EXECUTED = 1;
    }
}
