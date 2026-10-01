# FluDa Examples

Runnable examples for the fluent JDBC client.

## Overview

| Example              | Packaging | Modules + Database          | Demonstrates                                                                                                                    |
|----------------------|-----------|-----------------------------|---------------------------------------------------------------------------------------------------------------------------------|
| [`vanilla`](vanilla) | jar       | core, H2                    | The core `JdbcClient` with no CDI, backed by an in-memory H2 `DataSource`.                                                      |
| [`javase`](javase)   | jar       | core/cdi, H2                | The core + CDI modules, bootstrapped with Weld SE from a `main()` method.                                                       |
| [`servlet`](servlet) | war       | core/cdi, MariaDB           | The core + CDI modules in a Servlet container (Tomcat 11), with the `DataSource` provided via JNDI from `META-INF/context.xml`. |
| [`jakartaee`](jakartaee) | war       | core/cdi/config, PostgreSQL | The core + CDI modules behind a JAX-RS resource on GlassFish / WildFly, backed by PostgreSQL.                                   |
| [`sql-init`](sql-init) | jar       | sql-init, H2, S3            | SQL migration initialization with custom version strategy and S3 resource resolver.                                             |

## Prerequisites

- JDK 21+
- Maven 3.9+ (or the included Maven wrapper `./mvnw`)
- The `fluda-jdbc-client` library installed in your local Maven repository

Install the library first:

```bash
git clone https://github.com/fludakit/jdbc-client.git
cd jdbc-client
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

Start databases with Docker Compose:

```bash
docker compose up -d
```
