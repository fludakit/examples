# FluDa Examples

[![Vanilla](https://github.com/fludakit/examples/actions/workflows/vanilla.yml/badge.svg)](https://github.com/fludakit/examples/actions/workflows/vanilla.yml)
[![Java SE](https://github.com/fludakit/examples/actions/workflows/javase.yml/badge.svg)](https://github.com/fludakit/examples/actions/workflows/javase.yml)
[![Servlet](https://github.com/fludakit/examples/actions/workflows/servlet.yml/badge.svg)](https://github.com/fludakit/examples/actions/workflows/servlet.yml)
[![Jakarta EE](https://github.com/fludakit/examples/actions/workflows/jakartaee.yml/badge.svg)](https://github.com/fludakit/examples/actions/workflows/jakartaee.yml)
[![SQL Init](https://github.com/fludakit/examples/actions/workflows/sql-init.yml/badge.svg)](https://github.com/fludakit/examples/actions/workflows/sql-init.yml)

Runnable examples for the FluDa clients.

## Overview

| Example              | Packaging | Modules + Database          | Demonstrates                                                                                                                    |
|----------------------|-----------|-----------------------------|---------------------------------------------------------------------------------------------------------------------------------|
| [`vanilla`](vanilla) | jar       | core, H2                    | The core `JdbcClient` with no CDI, backed by an in-memory H2 `DataSource`.                                                      |
| [`javase`](javase)   | jar       | core/cdi, H2                | The core + CDI modules, bootstrapped with Weld SE from a `main()` method.                                                       |
| [`servlet`](servlet) | war       | core/cdi, MariaDB           | The core + CDI modules in a Servlet container (Tomcat 11), with the `DataSource` provided via JNDI from `META-INF/context.xml`. |
| [`jakartaee`](jakartaee) | war       | core/cdi/config, PostgreSQL | The core + CDI modules behind a JAX-RS resource on GlassFish / WildFly, backed by PostgreSQL.                                   |
| [`sql-init`](sql-init) | jar       | sql-init, H2, S3            | SQL migration initialization with custom version strategy and S3 resource resolver.                                             |
| [`jms-client-vanilla`](jms-client-vanilla) | jar | jms core, Artemis     | The core `JmsClient` with no CDI, sending and receiving a serializable payload on ActiveMQ Artemis.                              |
| [`jms-client-artemis`](jms-client-artemis) | jar | jms core/cdi/config, Artemis | CDI + MicroProfile Config in Java SE: the `fluda.jms.artemis.*` section builds the factory, the client is injected.       |
| [`jms-client-qpid`](jms-client-qpid) | jar       | jms core/cdi/config, Qpid (AMQP 1.0) | The same application code over AMQP 1.0, configured with `fluda.jms.qpid.*` instead.                                  |

The three `jms-client-*` examples need a running broker: the tests start one with Testcontainers
(Docker required), and the `main()` classes default to `tcp://localhost:61616` (Artemis) or
`amqp://localhost:5672` (Qpid). See [Run](#run) for the matching Docker Compose service, and for
running them from an IDE.

## Prerequisites

- JDK 21+
- Maven 3.9+ (or the included Maven wrapper `./mvnw`)
- The `fluda-jdbc-client` library installed in your local Maven repository (JDBC examples)
- The `fluda-jms-client` library installed in your local Maven repository (JMS examples)

Install the libraries first:

```bash
git clone https://github.com/fludakit/jdbc-client.git
cd jdbc-client
./mvnw install -DskipTests

git clone https://github.com/fludakit/jms-client.git
cd jms-client
./mvnw install -DskipTests
```

## Build and test

```bash
# vanilla
./mvnw -pl vanilla verify

# javase
./mvnw -pl javase verify

# servlet (requires MariaDB)
./mvnw -pl servlet -Parq-tomcat-embedded verify

# jakartaee (requires PostgreSQL)
./mvnw -pl jakartaee -Parq-glassfish-managed verify

# sql-init (requires Docker for LocalStack tests)
./mvnw -pl sql-init verify

# JMS examples (require Docker for the Artemis broker container)
./mvnw -pl jms-client-vanilla,jms-client-artemis,jms-client-qpid verify
```

## Run

```bash
# servlet on embedded Tomcat 11
./mvnw -pl servlet -Ptomcat-embedded clean package cargo:run

# jakartaee on GlassFish 8
./mvnw -pl jakartaee -Pglassfish clean package cargo:run

# jakartaee on WildFly 41
./mvnw -pl jakartaee -Pwildfly clean package wildfly:run
```

The JMS examples run against a local broker - an Artemis container serves all three (61616 for the
core protocol, 5672 for AMQP 1.0). It is part of `docker-compose.yml`, so:

```bash
# start just the broker (add -d --wait to block until both ports are listening)
docker compose up -d artemis

# plain JmsClient
./mvnw -pl jms-client-vanilla exec:java -Dexec.mainClass=com.example.VanillaExample

# CDI + config, Artemis native client
./mvnw -pl jms-client-artemis exec:java -Dexec.mainClass=com.example.ArtemisExample

# CDI + config, Qpid JMS over AMQP 1.0
./mvnw -pl jms-client-qpid exec:java -Dexec.mainClass=com.example.QpidExample
```

The same broker works when the examples are launched from an IDE: run `VanillaExample`,
`ArtemisExample` or `QpidExample` directly - the defaults in the sources and in
`microprofile-config.properties` already point at `localhost:61616` / `localhost:5672` with
`artemis`/`artemis`.

Point them at another broker with `JMS_BROKER_URL`, `JMS_USERNAME` and `JMS_PASSWORD`.

Start every service (databases and broker) with Docker Compose:

```bash
docker compose up -d
```
