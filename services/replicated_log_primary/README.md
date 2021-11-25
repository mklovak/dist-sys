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

> Make sure to configure secondary nodes via such environment variables:
> - `SECONDARY_<number>_ID` - node ID (e.g. `secondary-1`, **required**)
> - `SECONDARY_<number>_ENABLED` - whether node is enabled (should be set to `true`, **required**)
> - `SECONDARY_<number>_PORT` - node port (e.g. `50051`, **required**)
> - `SECONDARY_<number>_HOST` - node host (e.g. `172.16.1.1`, `0.0.0.0` is set by default)

```shell script
$ SECONDARY_1_ID=secondary-1 SECONDARY_1_ENABLED=true SECONDARY_1_PORT=50051 ./gradlew quarkusDev
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
$ docker build -t replicated_log_primary -f services/replicated_log_primary/Dockerfile .
```

```shell script
$ docker run -i --rm -p 9090:9090 -p 9095:9095 replicated_log_primary
```
