package com.github.mjksabit.warehouse.client.network;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

public class ResponseListener implements Runnable {

    private volatile boolean continueListening = true;

    private final DataInputStream in;
    private final Map<String, ResponseHandler> handlers = new HashMap<>();
    private ResponseHandler errorHandler = (response -> {
        System.out.println("Error Handler not initialized");
        System.out.println("ERROR:");
        System.out.println(response.toString());
    });

    private final Thread listenerThread;

    public ResponseListener(DataInputStream input) {
        this.in = input;

        listenerThread = new Thread(this, "ResponseListener");
        listenerThread.start();
    }

    public void addHandler(String RESPONSE_KEY, ResponseHandler handler) {

        handlers.put(RESPONSE_KEY, handler);
    }

    public void removeHandler(String RESPONSE_KEY) {
        handlers.remove(RESPONSE_KEY);
    }

    public void setErrorHandler(ResponseHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    private void handleResponse(Data response) {

        if (handlers.containsKey(response.getTYPE())) {
            handlers.get(response.getTYPE()).handle(response);
        } else
            errorHandler.handle(response);

        if (response.getText().optBoolean(Data.REMOVE_REQUESTER, false))
            removeHandler(response.getText().optString(Data.REQUEST_KEY, ""));
    }

    @Override
    public void run() {
        while (continueListening) {
            try {
                Data response = new Data(in);
                handleResponse(response);

            } catch (SocketTimeoutException e) {
                System.out.println("NO RESPONSE, RESPONSE QUEUE SIZE: "+handlers.size()+", WAITING...");
                for (String key : handlers.keySet()) System.out.printf("%s ", key);
                System.out.println();
            }
            catch (Exception e) {
                // Disconnected!!!
//                errorHandler.handle();
                continueListening = false;
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        continueListening = false;
    }
}
