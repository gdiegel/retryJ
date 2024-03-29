# retryJ

[![Java CI](https://github.com/gdiegel/retryJ/actions/workflows/maven-verify.yml/badge.svg?branch=develop)](https://github.com/gdiegel/retryJ/actions/workflows/maven-verify.yml)
[![Built with Maven](http://maven.apache.org/images/logos/maven-feather.png)](http://maven.apache.org)

retryJ is a retry library for Java. It allows executing a computation wrapped in a Callable until the result matches a
desired condition while ignoring certain exceptions that might be thrown along the way. It supports global execution
limits and timeouts and allows configuring the interval between executions.

Some example use cases:

* Checking a mailbox repeatedly until an email arrives
* Calling an unstable web resource that may fail intermittently
* Polling a resource until it returns the desired result
* Executing a computation until the value matches a specified exit condition

## Requirements

Java 17+

## Install

retryJ is on Maven Central. To use it, include this dependency information in your `pom.xml`:

```xml
<dependency>
    <groupId>io.github.gdiegel</groupId>
    <artifactId>retryJ</artifactId>
    <version>${LATEST_VERSION}</version>
</dependency>
```

## Usage

### Execute once and return the result:

```java
final RetryPolicy<Double> retryPolicy = RetryPolicy.<Double>builder()
    .withMaximumExecutions(1L)
    .build();
final Optional<Double> result = Retry.with(retryPolicy).execute(Math::random);
// result.get() => 0.570372838968257
```

### Execute twice while ignoring `RuntimeException`:

```java
class ThrowOnceThenSucceed {
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
```

```java
final ThrowOnceThenSucceed tots = new ThrowOnceThenSucceed();
final RetryPolicy<String> retryPolicy = RetryPolicy.<String>builder()
    .withMaximumExecutions(2L)
    .ignoreWhen(exception -> exception.getClass() == RuntimeException.class)
    .build();
final Optional<String> result = Retry.with(retryPolicy).execute(tots::invoke);
// result.get() => "Yippie!"
```

### Execute every 100 nanoseconds for a maximum of one minute until the result is smaller than or equal to 0.01 while ignoring `NumberFormatException`:

```java
final RetryPolicy<Double> retryPolicy = RetryPolicy.<Double>builder()
    .withInterval(Duration.of(100,NANOS))
    .withTimeout(Duration.of(1,MINUTES))
    .ignoreWhen(exception -> exception.getClass() == NumberFormatException.class)
    .retryUntil(d -> d <= 0.01)
    .build();
final Optional<Double> result = Retry.with(retryPolicy).execute(Math::random);
// result.get() => 0.09588896186808349
```

## License

Released under the [Apache 2.0 license](LICENSE.md)
