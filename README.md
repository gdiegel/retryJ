# retryJ - A micro retry library for Java

```java
final Retry<Double> retry = Retry.<Double>builder()
    .withInterval(100, ChronoUnit.NANOS)
    .withTimeout(10, ChronoUnit.SECONDS)
    .retryOnException(e -> e.getClass().equals(NumberFormatException.class))
    .retryUntil(d -> d <= 0.01).build();
final Double result = retry.call(Math::random);
```
