# Thread Pool Framework

A lightweight Java thread pool framework for submitting tasks to a managed pool of worker threads — built from scratch without any framework dependencies.

## Tech Stack

- Java 21
- Maven
- JUnit 5

## Features

- Submit `Callable<T>` or `Runnable` tasks and get back a `TaskFuture<T>` to track results
- **Fixed thread pool** — a set number of workers, always running
- **Cached thread pool** — scales up to a max size under load and shrinks back during idle periods
- Configurable **rejection policies** for when the task queue is full
- **Task lifecycle events** (submitted, started, completed, failed) delivered to registered listeners
- Graceful `shutdown()` and immediate `shutdownNow()` with `awaitTermination` support
- Fluent builder API for pool configuration

## Design Patterns

| Pattern | Where |
|---|---|
| **Command** | `Task<T>` wraps a `Callable<T>` as a self-contained unit of work |
| **Template Method** | `AbstractThreadPool` defines the lifecycle; `shouldWorkerExit` and `onTaskEnqueued` are overridden by subclasses |
| **Strategy** | `RejectionPolicy` — swap between `AbortPolicy`, `CallerRunsPolicy`, and `DiscardPolicy` |
| **Observer** | `TaskListener` receives `TaskEvent` notifications for every stage of task execution |
| **Factory** | `ThreadPoolFactory` creates pool instances without exposing constructors |
| **Builder** | `ThreadPoolBuilder` provides a fluent API to assemble a `ThreadPoolConfig` |

## How to Run

Build and run all tests:

```bash
mvn test
```

### Quick start

```java
// Fixed pool via factory shortcut
ThreadPool pool = ThreadPoolFactory.fixedPool("my-pool", 4);

// Cached pool via builder
ThreadPoolConfig config = new ThreadPoolBuilder()
        .name("workers")
        .corePoolSize(2)
        .maxPoolSize(8)
        .queueCapacity(50)
        .keepAlive(30, TimeUnit.SECONDS)
        .rejectionPolicy(new CallerRunsPolicy())
        .build();

ThreadPool pool = ThreadPoolFactory.cachedPool(config);

// Submit tasks
TaskFuture<String> future = pool.submit(() -> "hello");
String result = future.get(1, TimeUnit.SECONDS);

// Add a listener
pool.addListener(event -> System.out.println(event.getType() + " — " + event.getTaskName()));

// Shutdown
pool.shutdown();
pool.awaitTermination(5, TimeUnit.SECONDS);
```
