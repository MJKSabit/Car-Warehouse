package com.github.mjksabit.warehouse.client.network;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;

final public class ServerConnect implements Closeable {
    // Port used to connect to server
    public static int PORT = 26979;

    // Server host
    private static final String LOCALHOST = "127.0.0.1";

    public static final int TIMEOUT = 10000;

    // Singleton instance
    private static ServerConnect instance = null;
    private Socket socket;

    private InputStream inputStream = null;
    private OutputStream outputStream = null;
    private DataInputStream in = null;
    private DataOutputStream out = null;

    // Response Listener in a Parallel Thread
    ResponseListener responseListener = null;

    private ServerConnect() {
        try {
            // Create Connection
            socket = new Socket(LOCALHOST, PORT);

            socket.setSoTimeout(TIMEOUT);

            // Input Stream from Server
            inputStream = socket.getInputStream();
            // Output Stream to Server
            outputStream = socket.getOutputStream();

            in = new DataInputStream(inputStream);
            out = new DataOutputStream(outputStream);

            // ResponseListen from InputStream
            responseListener = new ResponseListener(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Singleton Instance Getter
    public static ServerConnect getInstance() {
        if (instance == null)
            instance = new ServerConnect();

        return instance;
    }

    // get Response Listener
    public ResponseListener getResponseListener() {
        return responseListener;
    }

    /**
     * Sends Request and Add Listener to handle the response that got back
     * @param request   Request that is sent to the server
     * @param handler   Listener that triggers an action on getting response
     */
    public void sendRequest(Data request, ResponseHandler handler) {
        try {
            request.write(out);
            responseListener.addHandler(request.getTYPE(), handler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            responseListener.stop();

            out.close();
            in.close();

            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
