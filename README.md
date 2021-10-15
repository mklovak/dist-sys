# Distributed Systems

UCU Data Engineering "Distributed Systems"

## Replicated Log Task

The course task is to develop a **distributed replicated log application** and implement different synchronization
techniques.

> **Reference:** https://docs.google.com/document/d/13akys1yQKNGqV9dGzSEDCGbHPDiKmqsZFOxKhxz841U/edit

### Deployment Architecture & Assumptions

* **1** Primary node, **N** (any number) Secondary nodes
* Primary and Secondary nodes should be run as separate applications in Docker containers
* Any RPC framework can be used for inter-node (Primary - Secondary) communication
  (sockets, language-specific RPC, HTTP, REST, **gRPC**, etc.)
* Primary node should expose a simple HTTP server with:
    * `POST` method – appends a message into the in-memory list
    * `GET` method – returns all messages from the in-memory list
* Secondary node(s) should expose a simple HTTP server with:
    * `GET` method – returns all replicated messages from the in-memory list

### Iteration 1

![iteration-1](assets/iteration-1.png)

#### Properties & Assumptions

* After each `POST` request, the message should be replicated on every Secondary server
* Primary should ensure that Secondaries have received a message via ACK
* `POST` request should be finished only after receiving ACKs from all Secondaries (**blocking replication approach**)
* To test that the replication is blocking, a delay/sleep on Secondaries should be introduced
* The communication channel is a **perfect link** (no failures and messages lost)
