package com.github.mjksabit.warehouse.server.Network;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;

public class Client implements Runnable, Closeable {

    Logger logger = LogManager.getLogger(Client.class);

    Socket socket;

    InputStream inputStream = null;
    OutputStream outputStream = null;
    DataInputStream in = null;
    DataOutputStream out = null;

    ResponseSender sender;

    private boolean isManufacturer  = false;
    private boolean isAdmin         = false;

    private String name = null;

    public Client(Socket socket) throws IOException {
        this.socket = socket;

        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();

        in = new DataInputStream(inputStream);
        out = new DataOutputStream(outputStream);

        new Thread(this).start();
    }

    @Override
    public void run() {
        sender = new ResponseSender(this, out);

        logger.info("Client Connected");

        try {
            while (true) {
                logger.info("Reading Data");
                var data = new Data(in);
                logger.info("Data read, Creating Response");
                sender.addToQueue(data);
            }
        } catch (JSONException | IOException e) {
            logger.error(e.getMessage());
        }

    }


    @Override
    public void close() throws IOException {
        inputStream.close();
        outputStream.close();

        socket.close();
    }
}
