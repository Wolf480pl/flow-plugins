package com.flowpowered.plugins.util.callback;

import com.flowpowered.plugins.util.ResultOrThrowable;

public interface ThrowingCatchingFunction<I, C extends Exception, O, T extends Exception> {

    O call(ResultOrThrowable<I, C> input) throws T;
}
