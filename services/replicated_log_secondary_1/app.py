import asyncio
import grpc
import time

from aiohttp import web
from grpc.experimental.aio import init_grpc_aio

from proto.replicated_log_pb2 import ReplicateMessageResponse
from proto.replicated_log_pb2_grpc import add_ReplicatedLogServicer_to_server
from proto.replicated_log_pb2_grpc import ReplicatedLogServicer

""" List to store messages """
MESSAGES = []

class MessagesView(web.View):
    async def get(self):
        return web.Response(text=str(MESSAGES))

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
        self.router.add_view('/messages', MessagesView)

    def run(self):
        return web.run_app(self, port=5000)


class LogServicer(ReplicatedLogServicer):
    def ReplicateMessage(self, request, context):
        print(f"got message from Primary: {request.message}")
        MESSAGES.append(request.message)
        # time.sleep(5)  # Delays for 5 seconds.
        return ReplicateMessageResponse(response=f"OK, {MESSAGES}")


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
