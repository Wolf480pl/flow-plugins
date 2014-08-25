package com.flowpowered.plugins.util.callback;

import com.flowpowered.plugins.util.ResultOrThrowable;

public class Callbacks {

    private Callbacks() {
    }

    public static <I, C extends Exception, A, B extends Exception, O, T extends Exception>
    ThrowingCatchingFunction<I, C, O, T> chain(final ThrowingCatchingFunction<I, C, A, B> f1, final ThrowingCatchingFunction<A, B, O, T> f2) {
        return new ThrowingCatchingFunction<I,C,O,T>() {
            @SuppressWarnings("unchecked")
            @Override
            public O call(ResultOrThrowable<I, C> input) throws T {
                ResultOrThrowable<A,B> mid;
                try {
                    A result = f1.call(input);
                    mid = ResultOrThrowable.result(result);
                } catch (Exception e) {
                    mid = (ResultOrThrowable<A, B>) ResultOrThrowable.throwable(e);
                }
                return f2.call(mid);
            }
        };
    }

    public static <I, A, B extends Exception, O, T extends Exception> ThrowingFunction<I, O, T> chain(final ThrowingFunction<I, A, B> f1, final ThrowingCatchingFunction<A, B, O, T> f2) {
        return new ThrowingFunction<I, O, T>() {
            @SuppressWarnings("unchecked")
            @Override
            public O call(I input) throws T {
                ResultOrThrowable<A, B> mid;
                try {
                    A result = f1.call(input);
                    mid = ResultOrThrowable.result(result);
                } catch (Exception e) {
                    mid = (ResultOrThrowable<A, B>) ResultOrThrowable.throwable(e);
                }
                return f2.call(mid);
            }
        };
    }

    public static <I, C extends Exception, A, B extends Exception, O> CatchingFunction<I, C, O> chain(final ThrowingCatchingFunction<I, C, A, B> f1, final CatchingFunction<A, B, O> f2) {
        return new CatchingFunction<I, C, O>() {
            @SuppressWarnings("unchecked")
            @Override
            public O call(ResultOrThrowable<I, C> input) {
                ResultOrThrowable<A, B> mid;
                try {
                    A result = f1.call(input);
                    mid = ResultOrThrowable.result(result);
                } catch (Exception e) {
                    mid = (ResultOrThrowable<A, B>) ResultOrThrowable.throwable(e);
                }
                return f2.call(mid);
            }
        };
    }

    public static <I, A, B extends Exception, O> Function<I, O> chain(final ThrowingFunction<I, A, B> f1, final CatchingFunction<A, B, O> f2) {
        return new Function<I, O>() {
            @SuppressWarnings("unchecked")
            @Override
            public O call(I input) {
                ResultOrThrowable<A, B> mid;
                try {
                    A result = f1.call(input);
                    mid = ResultOrThrowable.result(result);
                } catch (Exception e) {
                    mid = (ResultOrThrowable<A, B>) ResultOrThrowable.throwable(e);
                }
                return f2.call(mid);
            }
        };
    }

    public static <I, C extends Exception, A, O, T extends Exception> ThrowingCatchingFunction<I, C, O, T> chain(final CatchingFunction<I, C, A> f1, final ThrowingFunction<A, O, T> f2) {
        return new ThrowingCatchingFunction<I, C, O, T>() {
            @Override
            public O call(ResultOrThrowable<I, C> input) throws T {
                A mid = f1.call(input);
                return f2.call(mid);
            }
        };
    }

    public static <I, A, O, T extends Exception> ThrowingFunction<I, O, T> chain(final Function<I, A> f1, final ThrowingFunction<A, O, T> f2) {
        return new ThrowingFunction<I, O, T>() {
            @Override
            public O call(I input) throws T {
                A mid = f1.call(input);
                return f2.call(mid);
            }
        };
    }

    public static <I, C extends Exception, A, O> CatchingFunction<I, C, O> chain(final CatchingFunction<I, C, A> f1, final Function<A, O> f2) {
        return new CatchingFunction<I, C, O>() {
            @Override
            public O call(ResultOrThrowable<I, C> input) {
                A mid = f1.call(input);
                return f2.call(mid);
            }
        };
    }

    public static <I, A, O> Function<I, O> chain(final Function<I, A> f1, final Function<A, O> f2) {
        return new Function<I, O>() {
            @Override
            public O call(I input) {
                A mid = f1.call(input);
                return f2.call(mid);
            }
        };
    }

    public static <I, C extends Exception, O, T extends Exception> ThrowingCatchingFunction<I, C, O, T> throwing(final CatchingFunction<I, C, O> f) {
        return new ThrowingCatchingFunction<I, C, O, T>() {
            @Override
            public O call(ResultOrThrowable<I, C> input) throws T {
                return f.call(input);
            }
        };
    }

    public static <I, O, T extends Exception> ThrowingFunction<I, O, T> throwing(final Function<I, O> f) {
        return new ThrowingFunction<I, O, T>() {
            @Override
            public O call(I input) throws T {
                return f.call(input);
            }
        };
    }

    public static <I, C extends Exception, O, T extends Exception> ThrowingFunction<I, O, T> notCatching(final ThrowingCatchingFunction<I, C, O, T> f) {
        return new ThrowingFunction<I, O, T>() {
            @Override
            public O call(I input) throws T {
                return f.call(ResultOrThrowable.<I, C> result(input));
            }
        };
    }

    public static <I, C extends Exception, O> Function<I, O> notCatching(final CatchingFunction<I, C, O> f) {
        return new Function<I, O>() {
            @Override
            public O call(I input) {
                return f.call(ResultOrThrowable.<I, C> result(input));
            }
        };
    }
}
