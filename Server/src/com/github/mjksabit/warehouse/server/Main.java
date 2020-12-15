package com.github.mjksabit.warehouse.server;

import com.github.mjksabit.warehouse.server.Network.Client;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.util.Scanner;

public final class Main {

    // PORT Used to connect to Server
    private static final int PORT = 26979;

    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        Logger logger = LogManager.getLogger(Main.class);

        // New Thread to close the server explicitly
        new Thread(() -> {
            System.out.println("Press [enter] to close this server...");
            scanner.next();
            System.out.println("Exiting Server...");
            System.exit(0);
        }).start();

        // Open PORT to connect client
        try (ServerSocket serverSocket = new ServerSocket(PORT)){
            while (true) {
                logger.info("main: Waiting for a client to connect");

                // Client automatically run into new Thread
                new Client(serverSocket.accept());
            }
        } catch (BindException e) {
            logger.error("PORT: "+PORT+" is already in use!");
        } catch (IOException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }
}
