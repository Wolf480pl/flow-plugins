package com.flowpowered.plugins.artifact;

public interface SchedulingProvider {

    /**
     * Makes some thread call {@link #pulse(String)} for the given artifact soon.
     * The definition of "soon" is implementation-specific.
     */
    void enqueuePulse(String artifactName);

    /**
     * Makes some thread run the {@code runnable}. The thread should be chosen in a way that won't block processing of other artifacts.
     */
    void runAsync(Runnable runnable);
}
