package com.zlimbo.zcat.parse;

import com.zlimbo.zcat.config.ZCatConfig;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class CatParser {

    /**
     * 解析十六进制字符串，获得其中的交易统计数量
     *
     * @param countMessage 十六进制字符串
     * @return 交易数量
     */
    static public String[] parseCount(String countMessage) {
        String message = countMessage.substring(2);
        return new String[] { new BigInteger(message, 16).toString() };
    }

    /**
     * 解析十六进制字符串，获得其中最后三笔交易的日期
     *
     * @param timeMessage 十六进制字符串
     * @return 最后三笔交易的日期数组
     */
    static public String [] parseTime(String timeMessage) {

        String message = timeMessage.substring(2);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String[] result = new String[3];
        for (int i = 0; i < 3; ++i) {
            String hexStr = message.substring(ZCatConfig.STATE_SINGLE_LEN * i, ZCatConfig.STATE_SINGLE_LEN * (i + 1));
            BigInteger bigInteger = new BigInteger(hexStr, 16);
            long timestamp = bigInteger.longValue();
            if (timestamp != 0) {
                String date = dateFormat.format(timestamp);
                result[i] = date;
            } else {
                result[i] = "-";
            }
        }

        return result;
    }

    static public String[][] parseMix(String mixMessage) {
        String countMessage = mixMessage.substring(0, 66);
        String timeMessage = "0x" + mixMessage.substring(66);
        String[] countResult = parseCount(countMessage);
        String[] timeResult = parseTime(timeMessage);
        return new String[][]{countResult, timeResult};
    }


    public static void main(String[] args) {
        String countMessage = "0x0000000000000000000000000000000000000000000000000000000000000002";
        String timeMessage = "0x00000000000000000000000000000000000000000000000000000178259e951c00000000000000000000000000000000000000000000000000000178259eac7700000000000000000000000000000000000000000000000000000178259ecf8c";
        String mixMessage = "0x0000000000000000000000000000000000000000000000000000000000000003000000000000000000000000000000000000000000000000000001784419ed44000000000000000000000000000000000000000000000000000001784419f9180000000000000000000000000000000000000000000000000000000000000000";


        CatParser.parseCount(countMessage);
        CatParser.parseTime(timeMessage);
        String[][] result = CatParser.parseMix(mixMessage);

        System.out.println("count: " + result[0][0]);
        System.out.println("date: ");
        for (String date: result[1]) {
            System.out.println(date);
        }

    };


}
