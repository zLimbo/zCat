package com.zlimbo.zcat.connect;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

public class ConnectionLog {

    static Logger logger = LoggerFactory.getLogger(ConnectionLog.class);

    /**
     * 历史数据库连接信息
     */
    static private LinkedHashSet<ConnectionParam> CONNECTION_PARAMS = new LinkedHashSet<>();

    static private final String SAVE_PATH = System.getenv("HOMEPATH") + "/.zcat";

    static public void addConnectionParam(ConnectionParam connectionParam) {
        CONNECTION_PARAMS.add(connectionParam);
    }

    static public List<ConnectionParam> getConnectionParams() {
        return new ArrayList<>(CONNECTION_PARAMS);
    }

    public static void save() {
        logger.debug("save CONNECTION_PARAMS: [{}]", CONNECTION_PARAMS);
        try {
            JSONArray json = (JSONArray) JSONObject.toJSON(CONNECTION_PARAMS);
            String jsonString = json.toJSONString();
            try (OutputStream outputStream = new FileOutputStream(SAVE_PATH)) {
                outputStream.write(jsonString.getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean load() {
        byte[] data = new byte[1024];
        try {
            try (InputStream inputStream = new FileInputStream(SAVE_PATH)) {
                inputStream.read(data);
            }
            String jsonString = new String(data, StandardCharsets.UTF_8);
            CONNECTION_PARAMS = JSONArray.parseObject(jsonString, LinkedHashSet.class);
        } catch (Exception e) {
            e.printStackTrace();
            CONNECTION_PARAMS = new LinkedHashSet<>();
            return false;
        }
        logger.debug("load CONNECTION_PARAMS: [{}]", CONNECTION_PARAMS);
        return true;
    }

}
