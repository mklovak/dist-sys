FROM python:3.9.7

ENV APP_HOME=/app
WORKDIR $APP_HOME

# It is expected to run Docker build with the whole repository context.
# Folder paths are set according to the repository root folder.

COPY /proto/replicated-log.proto $APP_HOME/proto/replicated-log.proto
COPY /services/replicated_log_secondary_1/app.py $APP_HOME/app.py
COPY /services/replicated_log_secondary_1/requirements.txt $APP_HOME/requirements.txt

RUN pip install -r requirements.txt && \
    # Generate Protobuf and gRPC Python files.
    python -m grpc_tools.protoc -I=$APP_HOME --python_out=$APP_HOME --grpc_python_out=$APP_HOME $APP_HOME/proto/replicated-log.proto

CMD python $APP_HOME/app.py
