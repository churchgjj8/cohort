# Cohort

![main](https://github.com/sksamuel/cohort/workflows/main/badge.svg)
[<img src="https://img.shields.io/maven-central/v/com.sksamuel.cohort/cohort-core.svg?label=latest%20release"/>](http://search.maven.org/#search%7Cga%7C1%7Ccohort)
[<img src="https://img.shields.io/nexus/s/https/oss.sonatype.org/com.sksamuel.cohort/cohort-core.svg?label=latest%20snapshot&style=plastic"/>](https://oss.sonatype.org/content/repositories/snapshots/com/sksamuel/cohort/)

Cohort is a [Spring Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html) style
replacement for non Spring services. It provides HTTP endpoints to help monitor and manage apps in production. For
example health, logging, database and JVM metrics.

## How to use

Include the core dependency `com.sksamuel.cohort:cohort-core:<version>` and the ktor
dependency `com.sksamuel.cohort:cohort-ktor:<version>` in your build along with the additional modules for the features
you wish to activate.

Then to wire into Ktor, we would install the `Cohort` plugin, and enable whichever features we want to expose. **By
default all features are disabled**.

Here is an example with each feature enabled.

```kotlin
install(Cohort) {

  // enable an endpoint to display operating system name and version
  operatingSystem = true

  // enable runtime JVM information such as vm options and vendor name
  jvmInfo = true

  // configure the Logback log manager to show effective log levels and allow runtime adjustment
  logManager = LogbackManager

  // show connection pool information
  dataSourceManager = HikariDataSourceManager(writerDs, readerDs)

  // show current system properties
  sysprops = true

  // enable an endpoint to dump the heap
  heapdump = true

  // enable healthchecks for kubernetes
  healthcheck("/liveness", livechecks)
  healthcheck("/readiness", readychecks)
}
```

## Healthchecks

Cohort provides health checks for a variety of JVM metrics such as memory and thread deadlocks as well as connectivity
to services such as Kafka and Elasticsearch and databases.

The kubelet uses liveness probes to know when to restart a container. For example, liveness probes could catch a
deadlock, where an application is running, but unable to make progress. Restarting a container in such a state can help
to make the application more available despite bugs.

The kubelet uses readiness probes to know when a container is ready to start accepting traffic. A Pod is considered
ready when all of its containers are ready. One use of this signal is to control which Pods are used as backends for
Services. When a Pod is not ready, it is removed from Service load balancers.

The kubelet uses startup probes to know when a container application has started. If such a probe is configured, it
disables liveness and readiness checks until it succeeds, making sure those probes don't interfere with the application
startup. This can be used to adopt liveness checks on slow starting containers, avoiding them getting killed by the
kubelet before they are up and running.

Sometimes, applications are temporarily unable to serve traffic. For example, an application might need to load large
data or configuration files during startup, or depend on external services after startup. In such cases, you don't want
to kill the application, but you don't want to send it requests either. Kubernetes provides readiness probes to detect
and mitigate these situations. A pod with containers reporting that they are not ready does not receive traffic through
Kubernetes Services.

### Endpoints

Here is an example of output from a health check with a series of configured health checks.

```json
[
  {
    "name": "com.sksamuel.cohort.memory.FreememHealthCheck",
    "healthy": true,
    "lastCheck": "2022-03-15T03:01:09.445932Z",
    "message": "Freemem is above threshold [433441040 >= 67108864]",
    "cause": null,
    "consecutiveSuccesses": 75,
    "consecutiveFailures": 0
  },
  {
    "name": "com.sksamuel.cohort.system.OpenFileDescriptorsHealthCheck",
    "healthy": true,
    "lastCheck": "2022-03-15T03:01:09.429469Z",
    "message": "Open file descriptor count within threshold [209 <= 16000]",
    "cause": null,
    "consecutiveSuccesses": 25,
    "consecutiveFailures": 0
  },
  {
    "name": "com.sksamuel.cohort.memory.GarbageCollectionTimeCheck",
    "healthy": true,
    "lastCheck": "2022-03-15T03:00:54.422194Z",
    "message": "GC Collection time was 0% [Max is 25]",
    "cause": null,
    "consecutiveSuccesses": 6,
    "consecutiveFailures": 0
  },
  {
    "name": "writer connections",
    "healthy": true,
    "lastCheck": "2022-03-15T03:01:09.445868Z",
    "message": "Database connections is equal or above threshold [8 >= 8]",
    "cause": null,
    "consecutiveSuccesses": 75,
    "consecutiveFailures": 0
  },
  {
    "name": "reader connections",
    "healthy": true,
    "lastCheck": "2022-03-15T03:01:09.445841Z",
    "message": "Database connections is equal or above threshold [8 >= 8]",
    "cause": null,
    "consecutiveSuccesses": 75,
    "consecutiveFailures": 0
  },
  {
    "name": "com.sksamuel.cohort.system.SystemCpuHealthCheck",
    "healthy": true,
    "lastCheck": "2022-03-15T03:01:09.463421Z",
    "message": "System CPU is below threshold [0.12667261373773417 < 0.9]",
    "cause": null,
    "consecutiveSuccesses": 75,
    "consecutiveFailures": 0
  },
  {
    "name": "com.sksamuel.cohort.threads.ThreadDeadlockHealthCheck",
    "healthy": true,
    "lastCheck": "2022-03-15T03:00:54.419733Z",
    "message": "There are 0 deadlocked threads",
    "cause": null,
    "consecutiveSuccesses": 6,
    "consecutiveFailures": 0
  }
]
```

## Logging

Cohort allows you to view the current logging configuration and update log levels at runtime.

Here is the example output of `/cohort/logging` which shows the current log levels:

## Jvm Info

Displays information about the JVM state, including VM options, JVM version, and vendor name.

```json
{
  "name": "106637@sam-H310M-A-2-0",
  "pid": 106637,
  "vmOptions": [
    "-Dvisualvm.id=32227655111670",
    "-javaagent:/home/sam/development/idea-IU-213.5744.125/lib/idea_rt.jar=36667:/home/sam/development/idea-IU-213.5744.125/bin",
    "-Dfile.encoding=UTF-8"
  ],
  "classPath": "/home/sam/development/workspace/......",
  "specName": "Java Virtual Machine Specification",
  "specVendor": "Oracle Corporation",
  "specVersion": "11",
  "vmName": "OpenJDK 64-Bit Server VM",
  "vmVendor": "AdoptOpenJDK",
  "vmVersion": "11.0.10+9",
  "startTime": 1647315704746,
  "uptime": 405278
}
```

## Operating System

Displays the running os and version.

```json
{
  "arch": "amd64",
  "name": "Linux",
  "version": "5.13.0-35-generic"
}
```

## Datasources

By passing one or more datasource managers to Cohort, you can see at runtime the current state of the database pool(s).
Once enabled, a GET request to `/cohort/datasources` will return information such as idle connection count, pool size
and connection timeout levels.

Here is an example output for a Hikari datasource:

```json
[
  {
    "name": "HikariPool-2",
    "activeConnections": 0,
    "idleConnections": 8,
    "totalConnections": 8,
    "threadsAwaitingConnection": 0,
    "connectionTimeout": 30000,
    "idleTimeout": 600000,
    "maxLifetime": 1800000,
    "leakDetectionThreshold": 0,
    "maximumPoolSize": 16,
    "validationTimeout": 5000
  }
]
```

## System Properties

Send a GET request to `/cohort/sysprops` to return the current system properties.

Here is an example of the output:

```json
{
  "sun.jnu.encoding": "UTF-8",
  "java.vm.vendor": "AdoptOpenJDK",
  "java.vendor.url": "https://adoptopenjdk.net/",
  "user.timezone": "America/Chicago",
  "os.name": "Linux",
  "java.vm.specification.version": "11",
  "user.country": "US",
  "sun.boot.library.path": "/home/sam/.sdkman/candidates/java/11.0.10.hs-adpt/lib",
  "sun.java.command": "com.myapp.MainKt",
  "user.home": "/home/sam",
  "java.version.date": "2021-01-19",
  "java.home": "/home/sam/.sdkman/candidates/java/11.0.10.hs-adpt",
  "file.separator": "/",
  "java.vm.compressedOopsMode": "Zero based",
  "line.separator": "\n",
  "java.specification.name": "Java Platform API Specification",
  "java.vm.specification.vendor": "Oracle Corporation",
  "sun.management.compiler": "HotSpot 64-Bit Tiered Compilers",
  "java.runtime.version": "11.0.10+9",
  "user.name": "sam",
  "path.separator": ":",
  "file.encoding": "UTF-8",
  "java.vm.name": "OpenJDK 64-Bit Server VM",
  "user.dir": "/home/sam/development/workspace/myapp",
  "os.arch": "amd64",
  "java.vm.specification.name": "Java Virtual Machine Specification",
  "java.awt.printerjob": "sun.print.PSPrinterJob",
  "java.class.version": "55.0"
}
```
