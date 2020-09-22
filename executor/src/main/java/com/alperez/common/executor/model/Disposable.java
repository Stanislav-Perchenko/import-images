package com.alperez.common.executor.model;

/**
 * Created by stanislav.perchenko on 22.09.2020 at 22:12.
 */
public interface Disposable {
    /**
     * Dispose the resource, the operation should be idempotent.
     */
    void dispose();

    /**
     * Returns true if this resource has been disposed.
     * @return true if this resource has been disposed
     */
    boolean isDisposed();
}
