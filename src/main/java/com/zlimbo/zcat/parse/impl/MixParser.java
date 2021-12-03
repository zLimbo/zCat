package com.zlimbo.zcat.parse.impl;

import com.alibaba.fastjson.JSONObject;
import com.zlimbo.zcat.parse.IStateParser;
import com.zlimbo.zcat.parse.ParserRegister;
import com.zlimbo.zcat.parse.StateConfig;

@ParserRegister(StateConfig.QUERY_STATE_MIX)
public class MixParser implements IStateParser {
    @Override
    public JSONObject parse(String message) {

        String countMessage = message.substring(0, 66);
        String timeMessage = "0x" + message.substring(66);

        JSONObject json = new JSONObject();
        json.putAll(CountParser.getInstance().parse(countMessage));
        json.putAll(TimeParser.getInstance().parse(timeMessage));

        return json;
    }

    private MixParser() {

    }

    public static MixParser getInstance() {
        return Inner.singleton;
    }

    private static class Inner {
        private static final MixParser singleton = new MixParser();
    }
}
