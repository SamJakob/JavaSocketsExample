package com.samjakob.sockets_example;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

public class MyClient {

    /** Trivial main method that creates a new {@link MyClient} and calls
     * 'start' on it. */
    public static void main(String[] args) {
        var client = new MyClient();
        client.start();
    }

    /**
     * The client socket that gets connected to the server.
     */
    Socket clientSocket;

    public void start() {
        Scanner scanner = new Scanner(System.in);

        try {
            // Initialize the client socket and connect to the server.
            clientSocket = new Socket();
            clientSocket.connect(new InetSocketAddress(ScuffedProtocol.PORT));

            // The input stream allows us to read data incoming TO the
            // *client*.
            var inputStream = new DataInputStream(clientSocket.getInputStream());
            // The output stream allows us to transmit data outgoing FROM the
            // *client*.
            var outputStream = new DataOutputStream(clientSocket.getOutputStream());

            // We print the prompt for the user to write a message.
            System.out.print("> ");

            // While we're connected, read a new line and if it's not 'exit',
            // send it to the server and print the result.
            while (!clientSocket.isClosed()) {

                try {
                    // TODO: when should a user be able to send messages?
                    //  Should every outgoing message expect a response before
                    //  another message can be sent?

                    // If our System.in has some bytes ready, which would imply
                    // that our Scanner has a next line (i.e., someone has
                    // typed something into the console) then read the input.
                    //
                    // We can't just use Scanner.hasNext here because hasNext
                    // is blocking and doing that would stop us from being able
                    // to check for incoming messages from the server whilst we
                    // wait for input.
                    if (System.in.available() > 0) {
                        String message = scanner.nextLine();

                        // If the command is exit, close the socket and break
                        // out of the loop.
                        if (message.equalsIgnoreCase("exit")) {
                            // Send exit to the server to tell it that the
                            // client connection is closing.
                            outputStream.writeUTF("exit");

                            // TODO: how should you tell the server that your
                            //  client is disconnecting? Should you introduce
                            //  a mechanism for reconnecting automatically if
                            //  the connection drops?

                            clientSocket.close();
                            break;
                        }

                        // Otherwise, send the message.
                        //
                        // We use writeUTF (UTF = Unicode) to write a string.
                        // This writes the number of bytes in the string before
                        // sending the string which allows the server to check
                        // the length and that it received the whole message =
                        // before attempting to process it.
                        //
                        // Which aside from convenience, is why writeUTF is
                        // better than simply writing all the bytes and is why
                        // writeLine is deprecated and shouldn't be used.
                        outputStream.writeUTF(message);
                    }

                    // If our socket input stream has some number of bytes
                    // available (i.e., the server has sent something to us),
                    // then simply print it the line.
                    if (inputStream.available() > 0) {

                        // TODO: this will break if we receive something other
                        //  than a UTF string. How can we deal with this?
                        //  Deciding upon this is an important part of your
                        //  protocol.

                        // readUTF is the counterpart to writeUTF. It checks
                        // how many bytes it is receiving by checking the
                        // integer sent before the string and if it is of
                        // appropriate length, reads that many bytes.
                        System.out.println(inputStream.readUTF());

                        // Now re-print the input prompt.
                        System.out.print("> ");

                    }
                } catch (IOException ex) {
                    // If we encounter an IOException, it means there was a
                    // problem communicating (IO = Input/Output) so we'll log
                    // the error.
                    System.err.println(
                        "A communication error occurred with the server."
                    );
                    ex.printStackTrace();
                    break;
                }

            }

            System.out.println("\nConnection closed.");
        } catch (IOException ex) {
            System.err.println(
                "Failed to connect to the server. Is it running?"
            );
            ex.printStackTrace();
        }
    }

}
