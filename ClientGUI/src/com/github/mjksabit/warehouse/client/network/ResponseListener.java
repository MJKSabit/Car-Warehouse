package com.github.mjksabit.warehouse.client.network;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResponseListener implements Runnable {

    public static final String RESPONSE_TYPE = "type";

    private volatile boolean continueListening = true;

    private final BufferedReader in;
    private final Map<String, List<ResponseHandler>> handlers = new HashMap<>();
    private ResponseHandler errorHandler = (response -> {
        System.out.println("Error Handler not initialized");
        System.out.println("ERROR:");
        System.out.println(response.toString());
        return true;
    });

    private final Thread listenerThread;

    public ResponseListener(BufferedReader input) {
        this.in = input;

        listenerThread = new Thread(this, "ResponseListener");
        listenerThread.start();
    }

    public void addHandler(String RESPONSE_KEY, ResponseHandler handler) {
        if (!handlers.containsKey(RESPONSE_KEY)) {
            handlers.put(RESPONSE_KEY,new ArrayList<>());
        }
        handlers.get(RESPONSE_KEY).add(handler);
    }

    public void removeHandler(String RESPONSE_KEY, ResponseHandler handler) {
        if (handlers.containsKey(RESPONSE_KEY)) {
            var list = handlers.get(RESPONSE_KEY);
            if (list != null)
                list.remove(handler);
        }
    }

    public void setErrorHandler(ResponseHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    private void handleRequest(String RESPONSE_KEY, JSONObject response) {
        boolean handled = false;

        if (handlers.containsKey(RESPONSE_KEY)) {
            var listeners = handlers.get(RESPONSE_KEY);

            for (var listener : listeners) {
                if (listener.handle(response)) {
                    listener.postHandle();
                    handled = true;
                    break;
                }
            }
        }

        if (!handled) {
            errorHandler.handle(response);
            errorHandler.postHandle();
        }
    }

    @Override
    public void run() {
        while (continueListening) {
            try {
                String response = in.readLine();

                JSONObject object = new JSONObject(response);
                String responseType = object.getString(RESPONSE_TYPE);

                handleRequest(responseType, object);

            } catch (SocketTimeoutException e) {
                System.out.println("NO RESPONSE, RESPONSE QUEUE SIZE: "+handlers.size()+", WAITING...");
            }
            catch (Exception e) {
                // Disconnected!!!
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        continueListening = false;
    }
}
