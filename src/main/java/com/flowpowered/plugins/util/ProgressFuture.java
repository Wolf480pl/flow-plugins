package com.flowpowered.plugins.util;

import java.util.concurrent.Future;

public interface ProgressFuture<T> extends Future<T> {

    boolean startProgress();

    boolean hasStarted();

}