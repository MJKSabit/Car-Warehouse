package com.github.mjksabit.warehouse.server.Network;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;

public class ResponseSender implements Runnable{

    DataOutputStream out;
    Closeable client;
    Thread thread;

    volatile Queue<Data> responseQueue;
    Semaphore waitUntilNew = new Semaphore(1);
    Logger logger = LogManager.getLogger(ResponseSender.class);

    public ResponseSender(Closeable client, DataOutputStream out) {
        this.out = out;
        this.client = client;
        this.responseQueue = new LinkedBlockingDeque<>();
        this.thread = new Thread(this, "ResponseSender");
        this.thread.start();
    }

    public void addToQueue(Data data) {
        responseQueue.add(data);
        waitUntilNew.release();
    }

    @Override
    public void run() {
        try {
            while (true) {
                waitUntilNew.acquire();

                while (!responseQueue.isEmpty()) {
                    logger.info("Sending Response");
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

    public void renameThread(String threadName) {
        this.thread.setName(threadName);
    }
}
