package com.github.mjksabit.warehouse.client.network;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class Data {

    private final String TYPE;
    private final JSONObject text;
    private final byte[] binary;

    // ERROR TYPE
    public static final String ERROR = "error";
    // TYPE==ERROR has additional INFO in text
    public static final String INFO = "info";

    // Added in Response Data to remove listeners from client
    public static final String REQUEST_KEY = "request";
    // Control Removal of Client Listener from Server
    public static final String REMOVE_REQUESTER = "removeRequester";

    // Server-Client Communication Language CONSTANTS
    public static final String LOGIN = "login";
    public static final String LOGIN_USERNAME = "username";
    public static final String LOGIN_PASSWORD = "password";
    public static final String LOGOUT = "logout";

    public static final String VIEW_ALL = "viewAll";
    public static final String BUY_CAR = "buyCar";

    public static final String UPDATE_CAR = "updateCar";

    public static final String REMOVE_CAR = "removeCar";
    public static final String ADD_CAR = "addCar";
    public static final String EDIT_CAR = "editCar";

    public static final String ADMIN = "adminLogin";
    public static final String GET_USERS = "getUsers";
    public static final String ADD_USER = "addUser";
    public static final String REMOVE_USER = "removeUser";

    public static final String CAR_ID = "carId";
    public static final String CAR = "car";
    public static final String USER = "user";

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

    /**
     * Reads Data from DataInputStream and returns the new Data, used
     * to read Response and Request from InputStream
     * @param in    InputStream Wrapped in DataInputStream
     * @throws IOException  Data I/O Stream Exception
     * @throws JSONException    JSON Exceptions
     */
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

    /**
     * Writes Data to DataOutputStream, used
     * to write Response and Request from OutStream
     * @param out    OutputStream Wrapped in DataOutputStream
     * @throws IOException  Data I/O Stream Exception
     */
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

    /**
     * Builder Class for Data
     * Can create Data with multiple fields in root
     */
    public static class SimpleBuilder {
        private final String TYPE;
        private final JSONObject object = new JSONObject();

        public SimpleBuilder(String TYPE) {
            this.TYPE = TYPE;
        }

        public SimpleBuilder add(String key, String content) {
            try { object.put(key, content); }
            catch (JSONException e) { e.printStackTrace(); }
            return this;
        }

        public SimpleBuilder add(String key, int content) {
            try { object.put(key, content); }
            catch (JSONException e) { e.printStackTrace(); }
            return this;
        }


        public Data build() {
            return new Data(TYPE, object, null);
        }
    }
}
