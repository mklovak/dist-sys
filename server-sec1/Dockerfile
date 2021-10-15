# dockerfile must always start by importing the base image.
# import the python image (from DockerHub).
FROM python:latest

# In order to launch our python code, we must import the 'server-sec1.py' and 'index.html' file.
# The second parameter '/server-sec1/' is the path where to put the file on the image.
COPY /server-sec1/server-sec1.py /server-sec1/
COPY /server-sec1/requirements.txt /server-sec1/
COPY /proto/replicated-log.proto /server-sec1/
COPY /proto/replicated_log_pb2.py /server-sec1/
COPY /proto/replicated_log_pb2_grpc.py /server-sec1/


# This command changes the base directory of your image.
# Here we define '/server/' as base directory (where all commands will be executed).
WORKDIR /server-sec1/

# Install the function's dependencies
RUN  pip3 install -r requirements.txt
