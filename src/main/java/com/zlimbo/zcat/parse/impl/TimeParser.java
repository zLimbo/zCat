package com.zlimbo.zcat.parse.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zlimbo.zcat.parse.IStateParser;
import com.zlimbo.zcat.parse.ParserRegister;
import com.zlimbo.zcat.parse.StateConfig;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Int;
import org.web3j.abi.datatypes.StaticArray;
import org.web3j.abi.datatypes.Type;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

@ParserRegister(StateConfig.QUERY_STATE_TIME)
public class TimeParser implements IStateParser {
    @Override
    public JSONObject parse(String message) {

        Event event = new Event(StateConfig.QUERY_STATE_TIME,
                Arrays.<TypeReference<?>>asList(new TypeReference<Int>() {},
                        new TypeReference<Int>() {},
                        new TypeReference.StaticArrayTypeReference<StaticArray<Int>>(3) {}));
        List<Type> list = FunctionReturnDecoder.decode(message, event.getParameters());

        StaticArray<Int> staticArray = (StaticArray<Int>) list.get(2);

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String [] dates = new String[] {
                dateFormat.format(staticArray.getValue().get(0).getValue()),
                dateFormat.format(staticArray.getValue().get(1).getValue()),
                dateFormat.format(staticArray.getValue().get(2).getValue()) };

        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < 3; ++i) {
            String date = dateFormat.format(staticArray.getValue().get(0).getValue());
            jsonArray.add(date);
        }
        JSONObject json = new JSONObject();
        json.put(StateConfig.QUERY_STATE_TIME, jsonArray);

        return json;
    }

    private TimeParser() {

    }

    public static TimeParser getInstance() {
        return Inner.singleton;
    }

    private static class Inner {
        private static final TimeParser singleton = new TimeParser();
    }


}
