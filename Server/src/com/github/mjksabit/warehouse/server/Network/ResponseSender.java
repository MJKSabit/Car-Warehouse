package com.github.mjksabit.warehouse.server.Network;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;

public final class ResponseSender implements Runnable{

    private static final Logger logger = LogManager.getLogger(ResponseSender.class);

    // Where to write Response
    private final DataOutputStream out;

    // Closing Client from this Thread
    private final Closeable client;
    private final Thread thread;

    // Managing a request queue to send response serially rather than parallel
    private final Queue<Data> responseQueue;

    // Semaphore that blocks Sending if Request Queue is Empty
    private final Semaphore waitUntilNew = new Semaphore(1);

    public ResponseSender(Closeable client, DataOutputStream out) {
        this.out = out;
        this.client = client;
        this.responseQueue = new LinkedBlockingDeque<>();
        this.thread = new Thread(this);
        this.thread.start();
    }

    public void addToQueue(Data data) {
        responseQueue.add(data);
        // New response is here, Start sending in by unblocking
        waitUntilNew.release();
    }

    @Override
    public void run() {
        try {
            while (true) {
                // Wait until the queue has something
                waitUntilNew.acquire();

                while (!responseQueue.isEmpty()) {
                    logger.info("Sending Response");
                    // Removing a response from the queue and write to output stream
                    responseQueue.poll().write(out);
                }
            }
        } catch (InterruptedException | IOException e) {
            logger.error(e.getMessage());
            logger.debug("Stopping Response Sender");
            try {
                client.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    // Rename this ResponseSender Thread to differentiate clients
    public void renameThread(String threadName) {
        this.thread.setName(threadName);
    }
}
