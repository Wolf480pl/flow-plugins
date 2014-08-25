package com.flowpowered.plugins.util.callback;

public interface ThrowingFunction<I, O, T extends Exception> {

    O call(I input) throws T;
}
