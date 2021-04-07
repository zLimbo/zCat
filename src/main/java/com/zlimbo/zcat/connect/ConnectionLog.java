package com.zlimbo.zcat.connect;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class ConnectionLog {

    static Logger logger = LoggerFactory.getLogger(ConnectionLog.class);

    static int magicNum = 42;

    static private final String SAVE_PATH = ".zcat_data";
    /**
     * 历史数据库连接信息
     */
    static private LinkedHashSet<ConnectionParam> connectionParams;

    static public List<ConnectionParam> getConnectionParams() {
        return new ArrayList<>(connectionParams);
    }

    static public void addConnectionParam(ConnectionParam connectionParam) {
        connectionParams.add(connectionParam);
    }

    public static void removeConnectionParam(ConnectionParam connectionParam) {
        connectionParams.remove(connectionParam);
    }

    public static boolean save() {
        logger.debug("save connectionParams...");
        try {
            JSONArray json = (JSONArray) JSONObject.toJSON(connectionParams);
            String jsonString = json.toJSONString();
            byte[] data = encrypt(jsonString.getBytes(StandardCharsets.UTF_8));
            try (OutputStream outputStream = new FileOutputStream(SAVE_PATH)) {
                outputStream.write(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean load() {
        byte[] data = new byte[8 * 1024];
        int n = 0;
        try {
            try (InputStream inputStream = new FileInputStream(SAVE_PATH)) {
                n = inputStream.read(data);
            }
            data = decrypt(data, n);
            String jsonString = new String(data, StandardCharsets.UTF_8);
            JSONArray jsonArray = JSONArray.parseObject(jsonString, JSONArray.class);
            connectionParams = new LinkedHashSet<>(jsonArray.toJavaList(ConnectionParam.class));
        } catch (Exception e) {
            e.printStackTrace();
            connectionParams = new LinkedHashSet<>();
            return false;
        }
        logger.debug("load connectionParams...");
        return true;
    }

    private static byte[] encrypt(byte[] bytes) {
        for (int i = 0; i < bytes.length; ++i) {
            bytes[i] ^= magicNum;
        }
        return bytes;
    }

    private static byte[] decrypt(byte[] bytes, int n) {
        for (int i = 0; i < n; ++i) {
            bytes[i] ^= magicNum;
        }
        return bytes;
    }
}
