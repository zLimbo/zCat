package com.zlimbo.zcat.parse.impl;

import com.alibaba.fastjson.JSONObject;
import com.zlimbo.zcat.parse.IStateParser;
import com.zlimbo.zcat.parse.ParserRegister;
import com.zlimbo.zcat.parse.StateConfig;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Int;
import org.web3j.abi.datatypes.Type;

import java.util.Arrays;
import java.util.List;

@ParserRegister(StateConfig.QUERY_STATE_COUNT)
public class CountParser implements IStateParser {

    @Override
    public JSONObject parse(String message) {

        Event event = new Event(StateConfig.QUERY_STATE_COUNT, Arrays.<TypeReference<?>>asList(new TypeReference<Int>() {}));
        List<Type> result = FunctionReturnDecoder.decode(message, event.getParameters());

        String count = result.get(0).getValue().toString();

        JSONObject json = new JSONObject();
        json.put(StateConfig.QUERY_STATE_COUNT, count);

        return json;
    }

    private CountParser() {

    }

    public static CountParser getInstance() {
        return Inner.singleton;
    }

    private static class Inner {
        private static final CountParser singleton = new CountParser();
    }
}
