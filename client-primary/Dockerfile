# dockerfile must always start by importing the base image.
# import the python image (from DockerHub).
FROM python:latest

# In order to launch our python code, we must import the 'client-primary.py'\
# The second parameter '/client-primary/' is the path where to put the file on the image.
ADD client-primary.py /client-primary/

# This command changes the base directory of your image.
# Here we define '/client-rimary/' as base directory.
WORKDIR /client-primary/