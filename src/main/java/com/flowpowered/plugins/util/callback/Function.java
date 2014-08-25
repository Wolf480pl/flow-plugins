package com.flowpowered.plugins.util.callback;

public interface Function<I, O> {

    O call(I input);
}
