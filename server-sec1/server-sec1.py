#!/usr/bin/env python3

import http.server
import socketserver

# This variable is going to handle the requests of our client on the server.
handler = http.server.SimpleHTTPRequestHandler

# Here we define that we want to start the server on port 1234.
with socketserver.TCPServer(("", 1234), handler) as httpd:
    # This instruction will keep the server running, waiting for requests from the client.
    httpd.serve_forever()