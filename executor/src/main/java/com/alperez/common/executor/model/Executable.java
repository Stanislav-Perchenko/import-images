package com.alperez.common.executor.model;

/**
 * Created by stanislav.perchenko on 22.09.2020 at 22:13.
 */
public interface Executable extends Disposable {

    int getSequenceNumber();

    void executeSynchronously() throws Exception;

    void onExceptionWhileExecuted(Exception e);

    void onExecuted();
}
