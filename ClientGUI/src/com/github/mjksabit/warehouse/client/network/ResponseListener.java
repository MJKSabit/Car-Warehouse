package com.github.mjksabit.warehouse.client.network;

import java.io.DataInputStream;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

final public class ResponseListener implements Runnable {

    // Flag to stop this thread and stop listening for response from server
    private volatile boolean continueListening = true;

    // InputStream to read Response
    private final DataInputStream in;

    // Which Response to handle
    private final Map<String, ResponseHandler> handlers = new HashMap<>();

    // Default Action if No suitable Response Handler is found
    private ResponseHandler unknownResponseHandler = response ->
            System.out.println("ResponseHandler: UnknownResponse! ["+response.getTYPE()+"]");

    /**
     * Reads response from server,
     * runs in new thread, non-blocking
     * @param input     InputStream from Server
     */
    public ResponseListener(DataInputStream input) {
        this.in = input;

        // Start New Thread to Listen to Response
        new Thread(this, "ResponseListener")
                .start();
    }

    /**
     * Adds new handler to classify response and do its actions
     * @param RESPONSE_KEY  Key which is used to classify
     * @param handler       Action for that response Key
     */
    public void addHandler(String RESPONSE_KEY, ResponseHandler handler) {
        handlers.put(RESPONSE_KEY, handler);
    }

    // Removes that Action & Key from handler
    public void removeHandler(String RESPONSE_KEY) {
        handlers.remove(RESPONSE_KEY);
    }

    // Set Custom Error Handler to handle Unknown Response
    public void setErrorHandler(ResponseHandler unknownResponseHandler) {
        this.unknownResponseHandler = unknownResponseHandler;
    }

    private void handleResponse(Data response) {
        // If any registered ResponseHandler is there, let it handle it
        if (handlers.containsKey(response.getTYPE()))
            handlers.get(response.getTYPE()).handle(response);
        // Else show UnknownResponse and call its handler
        else
            unknownResponseHandler.handle(response);

        // ServerSide controls if ResponseHandler will be removed from client side
        if (response.getText().optBoolean(Data.REMOVE_REQUESTER, false))
            removeHandler(response.getText().optString(Data.REQUEST_KEY, ""));
    }

    @Override
    public void run() {
        // This thread can be stopped with the FLAG
        while (continueListening) {
            try {
                Data response = new Data(in);
                // Handle Response
                handleResponse(response);
            } catch (SocketTimeoutException ignored) {
//                System.out.println("No Response for "+ServerConnect.TIMEOUT+" milliseconds. Waiting...");
            } catch (Exception e) {
                continueListening = false;
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        continueListening = false;
    }
}
