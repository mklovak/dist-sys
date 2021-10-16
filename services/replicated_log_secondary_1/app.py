import logging
import threading
from concurrent import futures

import grpc
from flask import Flask
from flask_restful import reqparse, Api, Resource

from proto import replicated_log_pb2 as pb2, replicated_log_pb2_grpc as pb2_grpc

""" HTTP server (Flask) with REST API endpoint """
app = Flask(__name__)
api = Api(app)
parser = reqparse.RequestParser()


class MessageList(Resource):
    def get(self):
        print(f"Current messages on this server: {MESSAGES}")
        return MESSAGES


api.add_resource(MessageList, '/messages')


def http_app():
    print("====== Start HTTP server  ======")
    app.run()


class ReplicatedLogServicer(pb2_grpc.ReplicatedLogServicer):
    """ gRCP server """

    def ReplicateMessage(self, request, context):
        print(f"got message from Primary: {request.message}")
        MESSAGES.append(request.message)
        return pb2.ReplicateMessageResponse(response="OK")


def rpc_app():
    logging.basicConfig()
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    pb2_grpc.add_ReplicatedLogServicer_to_server(ReplicatedLogServicer(), server)
    server.add_insecure_port('0.0.0.0:50051')
    print("====== Start gRPC server ======")
    server.start()
    server.wait_for_termination()


""" List to store messages """
MESSAGES = []

if __name__ == '__main__':
    t1 = threading.Thread(target=http_app)
    t2 = threading.Thread(target=rpc_app)
    t1.start()
    t2.start()
