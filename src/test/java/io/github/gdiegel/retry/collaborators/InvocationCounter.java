package io.github.gdiegel.retry.collaborators;

import java.util.concurrent.atomic.LongAdder;

public class InvocationCounter {

    private final LongAdder invocations = new LongAdder();

    public long invoke() {
        invocations.increment();
        return getInvocations();
    }

    public long getInvocations() {
        return invocations.sum();
    }
}
