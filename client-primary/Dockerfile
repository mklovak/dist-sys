# dockerfile must always start by importing the base image.
# import the python image (from DockerHub).
FROM python:latest

# In order to launch our python code, we must import the 'client-primary.py'\
# The second parameter '/client-primary/' is the path where to put the file on the image.
COPY /client-primary/client-primary.py /client-primary/
COPY /client-primary/requirements.txt /client-primary/
COPY /proto/replicated-log.proto /client-primary/
COPY /proto/replicated_log_pb2.py /client-primary/
COPY /proto/replicated_log_pb2_grpc.py /client-primary/


# This command changes the base directory of your image.
# Here we define '/client-rimary/' as base directory.
WORKDIR /client-primary/

# Install the function's dependencies
RUN  pip3 install -r requirements.txt
