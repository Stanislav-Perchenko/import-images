package com.alperez.common.executor.utils;

import com.alperez.common.executor.model.Disposable;

/**
 * Common interface to add and remove disposables from a container.
 */
public interface DisposableContainer {

    /**
     * Adds a disposable to this container or disposes it if the
     * container has been disposed.
     * @param d the disposable to add, not null
     * @return true if successful, false if this container has been disposed
     */
    boolean add(Disposable d);

    /**
     * Removes and disposes the given disposable if it is part of this
     * container.
     * @param d the disposable to remove and dispose, not null
     * @return true if the operation was successful
     */
    boolean remove(Disposable d);

    /**
     * Removes (but does not dispose) the given disposable if it is part of this
     * container.
     * @param d the disposable to remove, not null
     * @return true if the operation was successful
     */
    boolean delete(Disposable d);
}