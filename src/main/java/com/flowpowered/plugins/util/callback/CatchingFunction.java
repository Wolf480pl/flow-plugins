package com.flowpowered.plugins.util.callback;

import com.flowpowered.plugins.util.ResultOrThrowable;

public interface CatchingFunction<I, C extends Exception, O> {

    O call(ResultOrThrowable<I, C> input);
}
