# retryJ - A micro retry library for Java

## Usage

### Execute once and return the result:

```java
final RetryPolicy<Double> retryPolicy = RetryPolicy.<Double>builder()
                .withMaxExecutions(1L)
                .build();
final Optional<Double> call = Retry.with(retryPolicy).call(Math::random);
```

### Execute every 100 nanoseconds for a maximum of one minute until the result is smaller than 0.01 while ignoring any NumberFormatExceptions:

```java
final RetryPolicy<Double> retryPolicy = RetryPolicy.<Double>builder()
        .withInterval(Duration.of(100, NANOS))
        .withTimeout(Duration.of(1, MINUTES))
        .retryWhenException(e -> e.getClass().equals(NumberFormatException.class))
        .retryUntil(d -> d <= 0.01)
        .build();
final Optional<Double> call = Retry.with(retryPolicy).call(Math::random);
```
