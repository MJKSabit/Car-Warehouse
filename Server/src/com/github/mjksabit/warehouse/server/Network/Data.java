package com.github.mjksabit.warehouse.server.Network;

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
