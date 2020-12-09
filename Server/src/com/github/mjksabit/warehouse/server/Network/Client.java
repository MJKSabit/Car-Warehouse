package com.github.mjksabit.warehouse.server.Network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client implements Runnable {

    Socket socket;

    InputStream inputStream = null;
    OutputStream outputStream = null;
    BufferedReader in = null;
    BufferedWriter out = null;

    ResponseHandler manager;

    public Client(Socket socket) {
        this.socket = socket;
        manager = new ResponseHandler();
        new Thread(this).start();
    }

    @Override
    public void run() {

    }
}
