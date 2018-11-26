# Shielder
A small, simple, asynchronous utility for constraining arbitrary tasks within CPU and RAM limitations.
Additionally provides a wrapper for the JSR-223 specification to sandbox scripting functionality.

## Dependency
### Maven
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.neontech.shielder</groupId>
    <artifactId>shielder-core</artifactId>
    <!-- <artifactId>shielder-jsr223</artifactId> -->
    <version>${version}</version>
</dependency>
```

### Gradle
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.neontech.shielder:shielder-core:$version'
    // implementation `com.github.neontech.shielder:shielder-jsr223:$version`
}
```

## Getting Started
### shielder-core
```java
final var sandbox = Sandbox.create();
final var environment = SandboxEnvironment.builder()
    .setCpuLimit(Duration.ofSeconds(1))
    .setRamLimit(1000000)
    .build();

final Callable<T> taskToSandbox = ...
final CompletableFuture<T> future = sandbox.startTask(environment, taskToSandbox);
```

### shielder-jsr223
```java
final var manager = new ShielderScriptEngineManager();
```
`manager` can be treated exactly like a `ScriptEngineManager`. Everything created by the `manager` is executed in a
`Sandbox` which can be accessed by invoking `manager.getSandbox()`.

To change the `SandboxEnvironment` for a specific `ScriptContext`, use the `ShielderScriptEngine.SANDBOX_ENVIRONMENT`
attribute and simply provide a `SandboxEnvironment` instance as its value.
