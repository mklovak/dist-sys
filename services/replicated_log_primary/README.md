# Replicated Log Primary Node

This project uses Quarkus framework – https://quarkus.io.

## OpenAPI Documentation

> **OpenAPI documentation** is available at http://localhost:9090/api/docs/

## Development

Copy `replicated-log.proto` file to `src/main/proto` folder:

```shell script
$ cp "$(eval git rev-parse --show-toplevel)/proto/replicated-log.proto" src/main/proto
```

Run this application in dev mode with live reload using:

```shell script
$ ./gradlew quarkusDev
```

> **Dev UI** is available in dev mode only at http://localhost:9090/q/dev/

## Testing

Run tests using:

```shell script
$ ./gradlew quarkusTest
```

## Packaging and Running the Application

> It is expected to run Docker build with the whole repository context.
> Folder paths are set according to the repository root folder.

Navigate to the repository root folder:

```shell script
$ cd $(eval git rev-parse --show-toplevel)
```

Package the application using Docker:

```shell script
$ docker build -t replicated_log_primary -f services/replicated_log_primary/Dockerfile --build-arg PORT=9090 .
```

```shell script
$ docker run -i --rm -p 9090:9090 replicated_log_primary
```
