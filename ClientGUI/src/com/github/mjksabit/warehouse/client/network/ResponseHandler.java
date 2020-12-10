package com.github.mjksabit.warehouse.client.network;

import org.json.JSONObject;

@FunctionalInterface
public interface ResponseHandler {
    void handle(Data response);
}
