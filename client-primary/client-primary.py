#!/usr/bin/env python3

# This library is used to download the 'index.html' from server.
import urllib.request

# 1234: Remember we define 1234 as the server port.
fp = urllib.request.urlopen("http://localhost:1234/")

# 'encodedContent' correspond to the server response encoded ('index.html').
# 'decodedContent' correspond to the server response decoded (what we want to display).
encodedContent = fp.read()
decodedContent = encodedContent.decode("utf8")

# Display the server file: 'index.html'.
print(decodedContent)

# Close the server connection.
fp.close()