package com.github.mjksabit.warehouse.client.network;


@FunctionalInterface
public interface ResponseHandler {
    /**
     * When A Type of Response is obtained from server, what to do then
     * Set what to do with lambda function or implemented Class
     * @param response  response that is obtained from server
     */
    void handle(Data response);
}
