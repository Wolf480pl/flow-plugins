package com.flowpowered.plugins.util;

import java.util.concurrent.atomic.AtomicBoolean;

import com.flowpowered.commons.SimpleFuture;

public class ProgressFutureImpl<T> extends SimpleFuture<T> implements ProgressFuture<T> {
    private final AtomicBoolean started = new AtomicBoolean(false);

    @Override
    public boolean startProgress() {
        return started.compareAndSet(false, true);
    }

    @Override
    public boolean hasStarted() {
        return started.get() || isDone();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (!mayInterruptIfRunning) {
            if (!started.compareAndSet(false, true)) {
                return false;
            }
        } else {
            started.set(true);
        }
        return super.cancel(mayInterruptIfRunning);
    }

}
