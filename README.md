# retryJ
[![Java CI](https://github.com/gdiegel/retryJ/actions/workflows/maven-verify.yml/badge.svg?branch=develop)](https://github.com/gdiegel/retryJ/actions/workflows/maven-verify.yml)
[![Downloads](https://img.shields.io/github/downloads/gdiegel/retryJ/total?style=for-the-badge)](https://img.shields.io/github/downloads/gdiegel/retryJ/total?style=for-the-badge)

retryJ is a retry library for Java. It allows executing a computation wrapped in a Callable until the result
matches a desired condition while ignoring certain exceptions that might be thrown along the way. It supports global
execution limits and timeouts and allows configuring the interval between executions.

Some example use cases:
* Checking a mailbox repeatedly until an email arrives
* Calling an unstable web resource that may fail intermittently
* Polling a resource until it returns the desired result
* Executing a computation until the value matches a specified exit condition

## Requirements
Java 16+

## Maven
Include this dependency information in your `pom.xml`:

```xml
<dependency>
  <groupId>io.github.gdiegel</groupId>
  <artifactId>retryJ</artifactId>
  <version>1.0.0</version>
</dependency>
```

## Usage

### Execute once and return the result:

```java
final var retryPolicy = RetryPolicy.<Double>builder()
                .withMaximumExecutions(1L)
                .build();
final var result = Retry.with(retryPolicy).execute(Math::random);
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
final var tots = new ThrowOnceThenSucceed();
final var retryPolicy = RetryPolicy.<String>builder()
                .withMaximumExecutions(2L)
                .ignoreWhen(exception -> exception.getClass() == RuntimeException.class)
                .build();
final var result = Retry.with(retryPolicy).execute(tots::invoke);
// result.get() => "Yippie!"
```

### Execute every 100 nanoseconds for a maximum of one minute until the result is smaller than or equal to 0.01 while ignoring `NumberFormatException`:

```java
final var retryPolicy = RetryPolicy.<Double>builder()
        .withInterval(Duration.of(100, NANOS))
        .withTimeout(Duration.of(1, MINUTES))
        .ignoreWhen(exception -> exception.getClass() == NumberFormatException.class)
        .retryUntil(d -> d <= 0.01)
        .build();
final var result = Retry.with(retryPolicy).execute(Math::random);
// result.get() => 0.09588896186808349
```

## License
Released under the [Apache 2.0 license](LICENSE.md)
