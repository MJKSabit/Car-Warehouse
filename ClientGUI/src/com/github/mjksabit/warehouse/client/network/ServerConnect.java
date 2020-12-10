package com.github.mjksabit.warehouse.client.network;

import org.json.JSONObject;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;

public class ServerConnect implements Closeable {
    // Port used to connect to server
    public static int PORT = 26979;

    // Server host
    private static final String LOCALHOST = "127.0.0.1";

    private static final int TIMEOUT = 10000;

    // Singleton instance
    private static ServerConnect instance = null;
    private Socket socket = null;

    InputStream inputStream = null;
    OutputStream outputStream = null;
    BufferedReader in = null;
    BufferedWriter out = null;

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

            in = new BufferedReader(new InputStreamReader(inputStream));
            out = new BufferedWriter(new OutputStreamWriter(outputStream));

            // ResponseListen from InputStream
            responseListener = new ResponseListener(in);
        }
        catch (ConnectException e) {
            // Connection Error
//            Main.showError("Can not connect to Server", 2000);
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Singleton Instance Getter
    public static ServerConnect getInstance() {
        if (instance == null)
            instance = new ServerConnect();

        return instance;
    }

    public ResponseListener getResponseListener() {
        return responseListener;
    }

    /**
     * Send Request to Server
     * @param object    Request JSONObject, Must have a REQUEST_TYPE
     */
    public void sendRequest(JSONObject object) {
        try {
            // '\n' is a must need for Server to readLine
            out.write(object.toString()+"\n");

            // Send to Server
            out.flush();
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

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}