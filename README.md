# Java TCP Sockets Example
This is an example project demonstrating how to communicate between a Java
server and client with a TCP socket.

This example is compatible with my [PythonTCPSocketsExample](https://github.com/SamJakob/PythonTCPSocketsExample).

This is written and tested with Java 16, however if you're using an older Java
version the only necessary change should be replacing use of `var` in the code
with the data type (i.e., the class name).

- [`ScuffedProtocol.java`](./src/com/samjakob/sockets_example/ScuffedProtocol.java):
    is a stub class that simply has a constant for the
    port number of the protocol.
- [`MyClient.java`](./src/com/samjakob/sockets_example/MyClient.java):
    is a runnable Java file that contains a simple client
    implementation that allows a user to enter messages to send to a server
    and prints any received messages from the server.
- [`MyServer.java`](./src/com/samjakob/sockets_example/MyServer.java):
    is a runnable Java file that contains a simple server
    implementation that converts any received messages to CAPITALS and sends
    the updated message back to the client.
