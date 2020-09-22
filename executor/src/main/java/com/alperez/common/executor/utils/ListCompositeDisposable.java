package com.alperez.common.executor.utils;

import com.alperez.common.executor.model.Disposable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * A disposable container that can hold onto multiple other disposables.
 */
public final class ListCompositeDisposable implements Disposable, DisposableContainer {

    List<Disposable> resources;

    volatile boolean disposed;

    public ListCompositeDisposable() {
    }

    public ListCompositeDisposable(Disposable... resources) {
        ObjectHelper.requireNonNull(resources, "resources is null");
        this.resources = new LinkedList<Disposable>();
        for (Disposable d : resources) {
            ObjectHelper.requireNonNull(d, "Disposable item is null");
            this.resources.add(d);
        }
    }

    public ListCompositeDisposable(Iterable<? extends Disposable> resources) {
        ObjectHelper.requireNonNull(resources, "resources is null");
        this.resources = new LinkedList<Disposable>();
        for (Disposable d : resources) {
            ObjectHelper.requireNonNull(d, "Disposable item is null");
            this.resources.add(d);
        }
    }

    @Override
    public void dispose() {
        if (disposed) {
            return;
        }
        List<Disposable> set;
        synchronized (this) {
            if (disposed) {
                return;
            }
            disposed = true;
            set = resources;
            resources = null;
        }

        dispose(set);
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    @Override
    public boolean add(Disposable d) {
        ObjectHelper.requireNonNull(d, "d is null");
        if (!disposed) {
            synchronized (this) {
                if (!disposed) {
                    List<Disposable> set = resources;
                    if (set == null) {
                        set = new LinkedList<Disposable>();
                        resources = set;
                    }
                    set.add(d);
                    return true;
                }
            }
        }
        d.dispose();
        return false;
    }

    public boolean addAll(Disposable... ds) {
        ObjectHelper.requireNonNull(ds, "ds is null");
        if (!disposed) {
            synchronized (this) {
                if (!disposed) {
                    List<Disposable> set = resources;
                    if (set == null) {
                        set = new LinkedList<Disposable>();
                        resources = set;
                    }
                    for (Disposable d : ds) {
                        ObjectHelper.requireNonNull(d, "d is null");
                        set.add(d);
                    }
                    return true;
                }
            }
        }
        for (Disposable d : ds) {
            d.dispose();
        }
        return false;
    }

    @Override
    public boolean remove(Disposable d) {
        if (delete(d)) {
            d.dispose();
            return true;
        }
        return false;
    }

    @Override
    public boolean delete(Disposable d) {
        ObjectHelper.requireNonNull(d, "Disposable item is null");
        if (disposed) {
            return false;
        }
        synchronized (this) {
            if (disposed) {
                return false;
            }

            List<Disposable> set = resources;
            if (set == null || !set.remove(d)) {
                return false;
            }
        }
        return true;
    }

    public void clear() {
        if (disposed) {
            return;
        }
        List<Disposable> set;
        synchronized (this) {
            if (disposed) {
                return;
            }

            set = resources;
            resources = null;
        }

        dispose(set);
    }

    void dispose(List<Disposable> set) {
        if (set == null) {
            return;
        }
        List<Throwable> errors = null;
        for (Disposable o : set) {
            try {
                o.dispose();
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                if (errors == null) {
                    errors = new ArrayList<Throwable>();
                }
                errors.add(ex);
            }
        }
        if (errors != null) {
            if (errors.size() == 1) {
                throw ExceptionHelper.wrapOrThrow(errors.get(0));
            }
            throw new CompositeException(errors);
        }
    }
}

