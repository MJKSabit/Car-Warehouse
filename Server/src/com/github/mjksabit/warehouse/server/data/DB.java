package com.github.mjksabit.warehouse.server.data;

import com.github.mjksabit.warehouse.server.Model.User;
import com.github.mjksabit.warehouse.server.Network.Data;
import org.apache.logging.log4j.CloseableThreadContext;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DB {

    private Map<String, String> users = new HashMap<>();
    private DB() {
        users.put("sabit", "1234");
    }

    private static DB instance = null;
    public static DB getInstance() {
        if (instance==null)
            instance = new DB();
        return instance;
    }


    public Data login(String username, String password) throws JSONException {
        username = username.toLowerCase();
        String type;
        JSONObject jsonObject = new JSONObject();

        if (!users.containsKey(username)) {
            jsonObject.put(Data.INFO, "No such user exists!");
            type = Data.ERROR;
        } else if (!users.get(username).equals(password)) {
            jsonObject.put(Data.INFO, "Password mismatch!");
            type = Data.ERROR;
        } else {
            type = Data.LOGIN;
            jsonObject.put(Data.INFO, "Welcome back "+username+"!");
        }

        return new Data(type, jsonObject, null);
    }
}
