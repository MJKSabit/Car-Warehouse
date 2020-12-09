package com.github.mjksabit.warehouse.client.network;

import org.json.JSONObject;

@FunctionalInterface
public interface ResponseHandler {
    boolean handle(JSONObject response);
    default void postHandle() {}
}
