package com.github.mjksabit.warehouse.client.network;

import org.json.JSONObject;

public class ServerConnectTest {
    public static void main(String[] args) {
        ResponseListener listener = ServerConnect.getInstance().getResponseListener();

        listener.addHandler("OKAY", (response -> {
            System.out.println(response);
            return true;
        }));

        listener.setErrorHandler((response -> {
            System.out.println("ERROR, "+response);
            return false;
        }));

        listener.addHandler("BYE", new ResponseHandler() {
            @Override
            public boolean handle(JSONObject response) {
                System.out.println("BYE");
                return true;
            }

            @Override
            public void postHandle() {
                listener.removeHandler("BYE", this);
            }
        });
    }
}
