# Replicated Log Secondary Node Test

> Dummy node to test gRPC communication without any message replication logic.

This project uses node.js.

## Development

> **Make sure to use node.js 14.x.x+**

Copy `replicated-log.proto` file to `proto` folder:

```shell script
$ cp "$(eval git rev-parse --show-toplevel)/proto/replicated-log.proto" proto/replicated-log.proto
```

Install dependencies:

```shell script
$ npm i
```

Run this application:

```shell script
$ node index.js
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
$ docker build -t replicated_log_secondary_test -f services/replicated_log_secondary_test/Dockerfile .
```

```shell script
$ docker run -i --rm -p 50053:50051 replicated_log_secondary_test
```
