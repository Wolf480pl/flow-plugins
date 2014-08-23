package com.flowpowered.plugins.util;

public class ResultOrThrowable<R, T extends Throwable> {
    private final R result;
    private final T throwable;

    protected ResultOrThrowable(R result, T throwable) {
        this.result = result;
        this.throwable = throwable;
    }

    public boolean isThrowable() {
        return throwable != null;
    }

    public T getThrowable() {
        return throwable;
    }

    public R getResultSafely() {
        return result;
    }

    public R get() throws T {
        if (isThrowable()) {
            throw getThrowable();
        }
        return getResultSafely();
    }

    public static <X, Y extends Throwable> ResultOrThrowable<X, Y> throwable(Y throwable) {
        return new ResultOrThrowable<X, Y>(null, throwable);
    }

    public static <X, Y extends Throwable> ResultOrThrowable<X, Y> result(X result) {
        return new ResultOrThrowable<X, Y>(result, null);
    }
}
