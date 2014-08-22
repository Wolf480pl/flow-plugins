package com.flowpowered.plugins.util;

import java.util.concurrent.Future;

public interface SafeFuture<T> extends Future<T> {

    T getResult();

    boolean isThrowable();

    Throwable getThrowable();
}
