package com.samjakob.sockets_example;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A delegate is simply a class that performs something on behalf of another
 * object hence this class is called 'MyServerDelegate'. Feel free to call this
 * whatever you please.
 *
 * One of these classes is initialized for every socket that connects to the
 * server, and it is responsible for communicating with that socket in a new
 * thread (which is why it implements Runnable).
 */
class MyServerDelegate implements Runnable {

    /**
     * The client socket that connected to the server.
     */
    private final Socket socket;

    /**
     * The input stream allows us to read data incoming TO the *server*.
     */
    private final DataInputStream inputStream;
    /**
     * The output stream allows us to transmit data outgoing FROM the *server*.
     */
    private final DataOutputStream outputStream;

    /**
     * Our constructor forces the client socket to be set (as one of these is
     * initialized for every client socket) when this class is created.
     *
     * @param socket The client socket the delegate should be responsible for.
     * @throws IOException If we are unable to access the input and output
     *                     stream from the socket, we allow the IOException
     *                     that they will generate to be thrown.
     */
    MyServerDelegate(Socket socket) throws IOException {
        this.socket = socket;

        // We declare the input stream and output stream on the class for
        // convenience, and set them here when the delegate is initialized.
        //
        // We make these final fields on the class because they shouldn't be
        // updated – as they'll last the lifetime of the socket connection, and
        // we don't want to accidentally overwrite them.
        this.inputStream = new DataInputStream(socket.getInputStream());
        this.outputStream = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() {

        System.out.println(
            "Accepted connection from: " +
            socket.getRemoteSocketAddress().toString()
        );

        // While WE (server-side) haven't disconnected a client, continue to
        // attempt to read data from the socket.
        while (!socket.isClosed()) {

            try {

                // TODO: should you 'ping' the client occasionally to ensure
                //  the connection hasn't dropped unexpectedly? ...and if so:
                //  – What should this ping message look like?
                //  - How can you make sure the ping message isn't confused for
                //  a real message?

                // When there is data available from the input stream
                if (inputStream.available() > 0) {
                    // Read the incoming message from the server input stream.
                    var message = inputStream.readUTF();

                    // If the message is exit, the client is disconnecting, so
                    // close the connection and break out of the loop.

                    // TODO: if you just stop the client application, you'll
                    //  notice that 'Connection closed' is not printed. How
                    //  can you deal with the connection being dropped without
                    //  being properly closed?
                    if (message.equals("exit")) {
                        socket.close();
                        break;
                    }

                    // Write the outgoing message to the server output stream.
                    outputStream.writeUTF(message.toUpperCase());
                }

            } catch (IOException ex) {
                // If we have an exception, we'll log that we failed to
                // read data from the socket and allow the connection to
                // close.
                System.err.println("Failed to read from the socket.");
                ex.printStackTrace();
                break;
            }
        }

        System.out.println(
            "Connection closed: " +
            socket.getRemoteSocketAddress().toString()
        );

    }

}


public class MyServer {

    /**
     * The server socket that accepts incoming connections.
     */
    ServerSocket serverSocket;

    /**
     * Trivial main method that creates a new {@link MyServer} and calls
     * 'start' on it.
     */
    public static void main(String[] args) {
        var server = new MyServer();
        server.start();
    }

    public void start() {
        try {

            // Initialize the server socket and bind to the port for our
            // protocol.
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(
                    // Bind to 0.0.0.0 (which means any host)...
                    // If this doesn't work (e.g., on Windows), try changing
                    // this to "localhost".
                    "0.0.0.0",
                    // ...and our protocol's port.
                    ScuffedProtocol.PORT
            ));

            while (!serverSocket.isClosed()) {
                try {
                    // serverSocket.accept will block execution until a
                    // connection is made. We can use this to our advantage. By
                    // placing it in this loop, whilst the server is running,
                    // we will continuously wait for a socket connection.
                    //
                    // Once we get one, the code will continue to construct a
                    // MyServerDelegate class for that socket, which is then
                    // passed into a new Thread and started.
                    var delegate = new MyServerDelegate(serverSocket.accept());

                    // Because MyServerDelegate implements Runnable (and
                    // therefore has a run method) we can pass it into a Thread
                    // which will execute the run method on our delegate once
                    // we call start.
                    //
                    // Once execution of our runnable has stopped, the thread
                    // will automatically end and be cleaned up.
                    new Thread(delegate).start();
                } catch (IOException ex) {
                    System.err.println(
                        "Failed to accept a socket connection. " +
                        "Is there a problem with the client?"
                    );
                }
            }

        } catch (IOException ex) {
            System.err.println(
                "Failed to start the server. " +
                "Is the port already taken?"
            );
            ex.printStackTrace();
        }
    }

}
