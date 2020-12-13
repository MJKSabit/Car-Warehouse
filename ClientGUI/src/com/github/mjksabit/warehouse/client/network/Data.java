package com.github.mjksabit.warehouse.client.network;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Data {


    private final String TYPE;
    private final JSONObject text;
    private final byte[] binary;

    public static final String ERROR = "error";
    public static final String INFO = "info";

    public static final String REQUEST_KEY = "request";
    public static final String REMOVE_REQUESTER = "removeRequester";

    public static final String LOGIN = "login";
    public static final String LOGOUT = "logout";

    public static final String LOGIN_USERNAME = "username";
    public static final String LOGIN_PASSWORD = "password";

    public static final String UPDATE_CAR = "updateCar";

    public static final String VIEW_ALL = "viewAll";
    public static final String BUY_CAR = "buyCar";

    public static final String REMOVE_CAR = "removeCar";
    public static final String ADD_CAR = "addCar";
    public static final String EDIT_CAR = "editCar";

    public static final String ADMIN = "adminLogin";
    public static final String ADD_USER = "addUser";
    public static final String REMOVE_USER = "removeUser";

    public static final String CAR_ID = "carId";
    public static final String CAR = "car";

    public Data(String TYPE, JSONObject text, byte[] binary) {
        this.TYPE = TYPE;
        this.text = text;
        this.binary = binary;
    }

    public String getTYPE() {
        return TYPE;
    }

    public JSONObject getText() {
        return text;
    }

    public byte[] getBinary() {
        return binary;
    }

    public Data(DataInputStream in) throws IOException, JSONException {
        TYPE = in.readUTF();

        int textSize = Integer.parseInt(in.readUTF());
        if (textSize != 0) {
            var buff = in.readNBytes(textSize);
            var jsonText = new String(buff, StandardCharsets.UTF_8);
            text = new JSONObject(jsonText);
        } else
            text = null;

        int binarySize = Integer.parseInt(in.readUTF());
        if (binarySize != 0) {
            binary = in.readNBytes(binarySize);
        } else
            binary = null;
    }

    public void write(DataOutputStream out) throws IOException {
        out.writeUTF(TYPE);

        if (text != null) {
            String text = this.text.toString();
            out.writeUTF(text.length()+"");
            out.write(text.getBytes(StandardCharsets.UTF_8));
        } else {
            out.writeUTF("0");
        }

        if (binary != null) {
            out.writeUTF(binary.length+"");
            out.write(binary);
        } else {
            out.writeUTF("0");
        }

        out.flush();
    }
}

