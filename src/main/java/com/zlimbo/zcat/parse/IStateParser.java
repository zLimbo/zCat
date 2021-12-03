package com.zlimbo.zcat.parse;

import com.alibaba.fastjson.JSONObject;

public interface IStateParser {

    JSONObject parse(String message);
}
