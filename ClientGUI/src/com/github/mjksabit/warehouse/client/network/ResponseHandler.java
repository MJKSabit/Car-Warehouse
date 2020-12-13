package com.github.mjksabit.warehouse.client.network;

@FunctionalInterface
public interface ResponseHandler {
    void handle(Data response);
}
