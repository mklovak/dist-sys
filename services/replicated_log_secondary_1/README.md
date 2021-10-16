# Replicated Log Secondary Node 1

This project uses Flask and Python gRPC Tools.

## Development

Copy `replicated-log.proto` file to `proto` folder:

```shell script
$ cp "$(eval git rev-parse --show-toplevel)/proto/replicated-log.proto" proto/replicated-log.proto
```

Install `requirements.txt`:

```shell script
$ pip3 install -r requirements.txt
```

Generate gRPC Python code:

```shell script
$ python -m grpc_tools.protoc -I=. --python_out=. --grpc_python_out=. proto/replicated-log.proto
```

Run this application:

```shell script
$ python3 app.py
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
$ docker build -t replicated_log_secondary_1 -f services/replicated_log_secondary_1/Dockerfile .
```

```shell script
$ docker run -i --rm -p 9091:5000 -p 50052:50051 replicated_log_secondary_1
```

## Known issues with gRPC for Python
Problems with one generated module importing another generated module:
https://github.com/grpc/grpc/issues/9450

Fix would be to modify imoprt statement in pb2_grpc.py to `from . import replicated_log_pb2 as replicated__log__pb2` 
