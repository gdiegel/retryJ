package io.github.gdiegel.retry.collaborators;

import java.util.concurrent.atomic.AtomicLong;

public class InvocationCounter {

    private final AtomicLong invocations = new AtomicLong(0L);

    public long invoke() {
        return invocations.incrementAndGet();
    }

    public AtomicLong getInvocations() {
        return invocations;
    }
}
