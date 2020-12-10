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

            Thread.sleep(2000);

            new Thread(() -> {
                while (true) {
                    try {
                        Data data = new Data(in);
                        System.out.println(data.getTYPE());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            new Data("UNKNOWN", new JSONObject("{\"a\":1}"), null).write(out);

        } catch (Exception e) {}
    }
}
