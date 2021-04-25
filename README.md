# retryJ
retryJ is a micro retry library for Java that allows executing a computation wrapped in a Callable until the result
matches a desired condition while ignoring any exception that might be thrown along the way. It supports global
execution limits and timeouts and allows configuring the interval between executions.

## Usage

### Execute once and return the result:

```java
final RetryPolicy<Double> retryPolicy = RetryPolicy.<Double>builder()
                .withMaxExecutions(1L)
                .build();
final Optional<Double> result = Retry.with(retryPolicy).call(Math::random);
// result.get() => 0.570372838968257
```

### Execute twice while ignoring any RuntimeExceptions:

```java
private static class ThrowOnceThenSucceed {
        private boolean thrown = false;

        String invoke() {
            if (thrown) {
                return "Yippie!";
            } else {
                thrown = true;
                throw new RuntimeException("Pow!");
            }
        }
    }

final var retryPolicy = RetryPolicy.<String>builder()
                .withMaxExecutions(2)
                .retryWhenException(e -> Objects.equals(e.getClass(), RuntimeException.class)).build();
Optional<String> result = Retry.with(retryPolicy).call(tots::invoke);
// result.get() => "Yippie!"
```

### Execute every 100 nanoseconds for a maximum of one minute until the result is smaller than or equal to 0.01 while ignoring any NumberFormatExceptions:

```java
final RetryPolicy<Double> retryPolicy = RetryPolicy.<Double>builder()
        .withInterval(Duration.of(100, NANOS))
        .withTimeout(Duration.of(1, MINUTES))
        .retryWhenException(e -> e.getClass().equals(NumberFormatException.class))
        .retryUntil(d -> d <= 0.01)
        .build();
final Optional<Double> result = Retry.with(retryPolicy).call(Math::random);
// result.get() => 0.09588896186808349
```

## License
Released under the [Apache 2.0 license](LICENSE.md)
