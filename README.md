# retryJ - A micro retry library for Java

```java
final Retry<Double> retry = Retry.<Double>builder()
        .withInterval(Duration.of(100, NANOS))
        .withTimeout(Duration.of(1, MINUTES))
        .retryWhenException(e -> e.getClass().equals(NumberFormatException.class))
        .retryUntil(d -> d <= 0.01)
        .build();
final Optional<Double> call = retry.call(Math::random);
```
