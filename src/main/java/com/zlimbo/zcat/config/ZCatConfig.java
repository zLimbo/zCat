package com.zlimbo.zcat.config;

public class ZCatConfig {

    public static final int STATE_COUNT_LENGTH = 66;
    public static final int STATE_THREE_LATEST_DATA_LENGTH = 322;


    public static void main(String[] args) {
        String sql = "SELECT * FROM invoice USING STATE";
        String data = "0x3843394839578349343384343857";
        String secondSql = (sql.endsWith(";") ? sql.substring(0, sql.lastIndexOf(";")) : sql)
                + " where hash = \'" + data  + "\';";

        System.out.println("first sql: " + sql);
        System.out.println("second sql: " + secondSql);
    }
}
