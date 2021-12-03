package com.zlimbo.zcat.config;

import java.util.HashMap;
import java.util.Map;

public class ZCatConfig {

    public static final boolean FOR_TEST = true;
    public static final int DEFAULT_PAGE_NUM = 500;

    /**
     * 状态查询
     */
    final public static String STATE = "STATE";
    final public static String STATE_HASH = "STATE_HASH";
    final public static String COUNT = "COUNT";

    final public static String LOWERCASE_STATE = "state";
    final public static String QUERY_STATE_COUNT = "count_tx";
    final public static String QUERY_STATE_TIME = "time_tx";
    final public static String QUERY_STATE_MIX = "*";

    final public static int STATE_SINGLE_LEN = 64;
    final public static int STATE_COUNT_LEN = 2 + STATE_SINGLE_LEN;
    final public static int STATE_TIME_LEN = 2 + STATE_SINGLE_LEN * 3;
    final public static int STATE_MIX_LEN = 2 + STATE_SINGLE_LEN * 4;

//    public static final int STATE_COUNT_LENGTH = 66;
//    public static final int STATE_THREE_LATEST_DATA_LENGTH = 322;
//    public static final int MIX_DATA_LENGTH = 386;


    public static final int QUERY_STATE_WAIT_TIME = 7000;


//    public static final JSONObject languageJson = null;
//
//    static {
//        try {
//            Reader reader = new FileReader("language/english.json");
//            char[] chars = new char[1024];
//            StringBuilder stringBuilder = new StringBuilder();
//            while (reader.read(chars))
//
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//    }


    public static void main(String[] args) {
        String sql = "SELECT * FROM invoice USING STATE";
        String data = "0x3843394839578349343384343857";
        String secondSql = (sql.endsWith(";") ? sql.substring(0, sql.lastIndexOf(";")) : sql)
                + " where hash = \'" + data  + "\';";

        System.out.println("first sql: " + sql);
        System.out.println("second sql: " + secondSql);
    }

    final public static Map<String, String> LANGUAGE_ENGLISH_MAP = new HashMap() {
        {
            put("File", "File");
            put("Edit", "Edit");
            put("Option", "Option");
            put("Help", "Help");
            put("Connect Database", "Connect Database");
//            put("Connect Database", "Connect CITA");
            put("Connect CITA", "Connect CITA");
            put("New Query", "New Query");
            put("MySQL New Connect", "MySQL New Connect");
//            put("MySQL New Connect", "CITA New Connect");
            put("Connect Error", "Connect Error");
            put("Incorrect connection!", "Incorrect connection!");
            put("Please re-enter connection parameters", "Please input parameter again");
            put("Close", "Close");
            put("Add Record", "Add Record");
            put("Submit", "Submit");
            put("Cancel", "Cancel");
            put("Attribute", "Attribute");
            put("Value", "Value");
            put("SQL statement cannot be empty!", "SQL statement cannot be empty!");
            put("Time", "Time");
            put("Query", "Query");
            put("Run", "Run");
            put("Add", "Add");
            put("Connect", "Connect");
            put("CITA New Connect", "CITA New Connect");
            put("CITA URL: ", "CITA URL: ");
        }
    };

    final public static Map<String, String> LANGUAGE_CHINESE_MAP = new HashMap() {
        {
            put("File", "文件");
            put("Edit", "编辑");
            put("Option", "设置");
            put("Help", "帮助");
            put("Connect Database", "连接数据库");
            put("Connect Database", "连接SQL中间件");
            put("Connect CITA", "连接CITA");
            put("Connect CITA", "连接区块链");
            put("New Query", "新建查询");
            put("MySQL New Connect", "MySQL 新连接");
            put("MySQL New Connect", "SQL中间件 新连接");
            put("Connect Error", "连接错误");
            put("Incorrect connection!", "不正确的连接!");
            put("Please re-enter connection parameters", "请重新输入连接参数");
            put("Close", "关闭");
            put("Add Record", "添加记录");
            put("Submit", "提交");
            put("Cancel", "取消");
            put("Attribute", "属性");
            put("Value", "值");
            put("SQL statement cannot be empty!", "SQL语句不能为空！");
            put("Time", "时间");
            put("Query", "查询");
            put("Run", "运行");
            put("Add", "添加");
            put("Add", "Add");
            put("Connect", "连接");
            put("CITA New Connect", "CITA 新连接");
            put("CITA New Connect", "区块链 新连接");
            put("CITA URL: ", "CITA URL: ");
            put("CITA URL: ", "区块链 URL: ");

        }
    };
}
