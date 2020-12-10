package com.github.mjksabit.warehouse.server.Network;

import com.github.mjksabit.warehouse.server.Main;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Test {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("127.0.0.1", 26979);

            var in = new DataInputStream(socket.getInputStream());
            var out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                while (true) {
                    try {
                        Data data = new Data(in);
                        System.out.println(data.getTYPE());
                        System.out.println(data.getText().toString(2));
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            var object = new JSONObject();
            object.put(Data.LOGIN_USERNAME, "sabit");
            object.put(Data.LOGIN_PASSWORD, "12345");
            new Data(Data.LOGIN, object, null).write(out);

        } catch (Exception e) {}
    }
}
