import logging, os, sys, threading, grpc
from concurrent import futures
from flask import Flask
from flask_restful import reqparse, Api, Resource

sys.path.append(os.path.abspath('../proto'))
import replicated_log_pb2 as pb2, replicated_log_pb2_grpc as pb2_grpc

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

""" gRCP server """
class ReplicatedLogServicer(pb2_grpc.ReplicatedLogServicer):
    def ReplicateMessages(self, message, context):
        print(f"got message from Primary: {message.message}")
        MESSAGES.append(message.message)
        return pb2.ReplicateMessagesAck(response="ACK")

def rcp_app():
    logging.basicConfig()
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    pb2_grpc.add_ReplicatedLogServicer_to_server(
        ReplicatedLogServicer(), server)
    server.add_insecure_port('[::]:50051')
    print("====== Start gRCP server ======")
    server.start()
    server.wait_for_termination()

""" List to store messages """
MESSAGES = []

if __name__ == '__main__':
    t1 = threading.Thread(target=http_app)
    t2 = threading.Thread(target=rcp_app)
    t1.start()
    t2.start()
