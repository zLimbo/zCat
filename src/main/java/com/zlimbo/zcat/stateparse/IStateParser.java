package com.zlimbo.zcat.stateparse;

import com.alibaba.fastjson.JSONObject;

public interface IStateParser {

    JSONObject parse(String message);
}
