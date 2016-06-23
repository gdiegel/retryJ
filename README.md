# retryJ - A micro retry library for Java8

```
final Retrier<Double> retrier = Retrier.<Double>builder()
    .withInterval(100, ChronoUnit.NANOS)
    .withTimeout(10, ChronoUnit.SECONDS)
    .retryOnException(e -> e.getClass().equals(NumberFormatException.class))
    .retryUntil(d -> d <= 0.01).build();
final Double result = retrier.call(Math::random);
```
