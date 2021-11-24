from random import randint
from time import sleep
from datetime import datetime

import asyncio
import grpc
import random
import json
import uuid

from aiohttp import web
from grpc.experimental.aio import init_grpc_aio

from proto.replicated_log_pb2 import ReplicateMessageResponse
from proto.replicated_log_pb2_grpc import ReplicatedLogServicer
from proto.replicated_log_pb2_grpc import add_ReplicatedLogServicer_to_server

""" Storage for messages """
MESSAGES = {}
LOG = {}


class MessagesView(web.View):
    async def get(self):
        SORTED_MESSAGES = []

        sorted_message_ids = sorted(MESSAGES.keys())
        prev_message_id = None
        for message_id in sorted_message_ids:
            if (prev_message_id is None) and (message_id == 0):
                SORTED_MESSAGES.append(MESSAGES[message_id])
                prev_message_id = message_id
            elif (message_id - 1) == prev_message_id:
                SORTED_MESSAGES.append(MESSAGES[message_id])
                prev_message_id = message_id
            else:
                print(f"Message with id {message_id - 1} is missing")
                break
        return web.Response(text=str(SORTED_MESSAGES))


class LogView(web.View):
    async def get(self):
        return web.Response(text=(json.dumps(LOG)))


class Application(web.Application):
    def __init__(self):
        super().__init__()
        self.grpc_task = None
        self.grpc_server = GrpcServer()
        self.add_routes()
        self.on_startup.append(self.__on_startup())
        self.on_shutdown.append(self.__on_shutdown())

    def __on_startup(self):
        async def _on_startup(app):
            self.grpc_task = \
                asyncio.ensure_future(app.grpc_server.start())

        return _on_startup

    def __on_shutdown(self):
        async def _on_shutdown(app):
            await app.grpc_server.stop()
            app.grpc_task.cancel()
            await app.grpc_task

        return _on_shutdown

    def add_routes(self):
        self.router.add_view("/api/v1/messages", MessagesView)
        self.router.add_view("/api/v1/logs", LogView)

    def run(self):
        return web.run_app(self, port=5000)


class LogServicer(ReplicatedLogServicer):
    def ReplicateMessage(self, request, context):
        timestamp = datetime.now().strftime("%H:%M:%S")
        print(f"got message from Primary: {request.message} at {timestamp}")
        message_id = int(request.messageId)

        # random delay
        delay = randint(1, 5)
        sleep(delay)

        # random internal server error
        random_bit = random.getrandbits(1)
        random_boolean = bool(random_bit)
        if random_boolean is True:
            LOG[str(uuid.uuid1())] = [
                {"message_id": request.messageId},
                {"message": request.message},
                {"message_delay": f"{delay} sec"},
                {"received_at": timestamp},
                {"duplicated": "unknown"},
                {"random_error": "True"}
            ]
            return ReplicateMessageResponse(response=f"internal server error")
        else:
            # check for duplicates
            if message_id in MESSAGES:
                print(f"Duplicated message with ID {message_id} received")
                duplicated = "True"
            else:
                duplicated = "False"
                MESSAGES[message_id] = request.message

            LOG[str(uuid.uuid1())] = [
                {"message_id": request.messageId},
                {"message": request.message},
                {"message_delay": f"{delay} sec"},
                {"received_at": timestamp},
                {"duplicated": duplicated},
                {"random_error": "False"}
            ]
            return ReplicateMessageResponse(response=f"ok")


class GrpcServer:
    def __init__(self):
        init_grpc_aio()
        self.server = grpc.experimental.aio.server()
        self.servicer = LogServicer()
        add_ReplicatedLogServicer_to_server(
            self.servicer,
            self.server)
        self.server.add_insecure_port("[::]:50051")

    async def start(self):
        await self.server.start()
        await self.server.wait_for_termination()

    async def stop(self):
        await self.servicer.close()
        await self.server.wait_for_termination()


application = Application()
if __name__ == '__main__':
    application.run()
