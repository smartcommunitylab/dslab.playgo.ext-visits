# dslab.playandgo.ext-visits Project
Extension for POI visits

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw compile quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Packaging and running the application

The application can be packaged using:
```shell script
./mvnw package
```
It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:
```shell script
./mvnw package -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using: 
```shell script
./mvnw package -Pnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```shell script
./mvnw package -Pnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/dslab.playandgo.ext-visits-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.

## CONFIGURATION

Environment Variables:

- ``PLAYANDGO_ENGINE_URL`` URL of Play&Go engine (NO trailing slash)
- ``AUTH_SERVER_URL`` URL of the AAC server (WITH trailing slash)
- ``CLIENT_ID`` Client ID 
- ``CLIENT_SECRET`` Client secret
- ``CAMPAIGN_VISIT_SOURCE`` source for the ID-URL mapping for the visit configuration folders URLs 
- ``GE_ENDPOINT`` Endpoint of the GE Engine (NO trailing slash)
- ``GE_USERNAME`` username for GE (basic auth)
- ``GE_PASSWORD`` password for GE (basic auth)

