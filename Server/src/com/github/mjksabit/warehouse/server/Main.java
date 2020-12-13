package com.github.mjksabit.warehouse.server;

import com.github.mjksabit.warehouse.server.Network.Client;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;

public class Main {

    private static final int PORT = 26979;

    public static void main(String[] args) {

        Logger logger = LogManager.getLogger(Main.class);

        try (ServerSocket serverSocket = new ServerSocket(PORT)){
            while (true) {
                logger.info("Waiting for client to connect");
                new Client(serverSocket.accept());
            }
        } catch (BindException e) {
            logger.error("PORT: "+PORT+" already in use!");
        } catch (IOException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }



    }
}
