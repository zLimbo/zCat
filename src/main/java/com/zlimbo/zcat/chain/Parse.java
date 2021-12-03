package com.zlimbo.zcat.chain;

import com.zlimbo.zcat.config.ZCatConfig;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author jiangnanmax
 * @email jiangnanmax@gmail.com
 * @description Parse
 * @date 2021/3/11
 **/

/**
 * maven依赖：
 *
 * <dependency>
 *     <groupId>org.web3j</groupId>
 *     <artifactId>core</artifactId>
 *     <version>4.5.18</version>
 * </dependency>
 *
 */

public class Parse {

    // 以下的data参数均在函数里用常量给定了，后面就是调用函数时候通过data参数传入

    /**
     * 解析十六进制字符串，获得其中的交易统计数量
     *
     * @param countMessage 十六进制字符串
     * @return 交易数量
     */
    public String[] parseCount(String countMessage) {
        String message = countMessage.substring(2);
        return new String[] { new BigInteger(message, 16).toString() };
    }

    /**
     * 解析十六进制字符串，获得其中最后三笔交易的日期
     *
     * @param timeMessage 十六进制字符串
     * @return 最后三笔交易的日期数组
     */
    public String [] parseTime(String timeMessage) {

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

    public String[][] parseMix(String mixMessage) {
        String countMessage = mixMessage.substring(0, 66);
        String timeMessage = "0x" + mixMessage.substring(66);
        String[] countResult = parseCount(countMessage);
        String[] timeResult = parseTime(timeMessage);
        return new String[][]{countResult, timeResult};
    }


    public static void main(String[] args) {
        Parse parse = new Parse();
        String countData = "0x0000000000000000000000000000000000000000000000000000000000000002";
        String threeDateData = "0x0000000000000000000000000000000000000000000000000000000000000020000000000000000000000000000000000000000000000000000000000000000300000000000000000000000000000000000000000000000000000178259e951c00000000000000000000000000000000000000000000000000000178259eac7700000000000000000000000000000000000000000000000000000178259ecf8c";
        String mixData = "0x000000000000000000000000000000000000000000000000000000000000000600000000000000000000000000000000000000000000000000000000000000400000000000000000000000000000000000000000000000000000000000000003000000000000000000000000000000000000000000000000000001784419ed44000000000000000000000000000000000000000000000000000001784419f91800000000000000000000000000000000000000000000000000000178441ad974";


        parse.parseCount(countData);
        parse.parseTime(threeDateData);
        String[][] result = parse.parseMix(mixData);

        System.out.println("count: " + result[0][0]);
        System.out.println("date: ");
        for (String date: result[1]) {
            System.out.println(date);
        }

        test();
    };




    public static void test() {
        String mixData = "0x000000000000000000000000000000000000000000000000000000000000000600000000000000000000000000000000000000000000000000000000000000400000000000000000000000000000000000000000000000000000000000000003000000000000000000000000000000000000000000000000000001784419ed44000000000000000000000000000000000000000000000000000001784419f91800000000000000000000000000000000000000000000000000000178441ad974";

        for (int i = 0; i < 3; ++i) {
            String hexString = mixData.substring(2 + 64 * (3 + i));
            long timestamp = hexString2Long(hexString);
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateString = df.format(new Date(timestamp));
            System.out.println("timestamp: " + timestamp);
            System.out.println("date: " + dateString);
        }
    }


    public static long hexString2Long(String hexString) {
        long num = 0L;
        for (char c: hexString.toCharArray()) {
            if ('0' <= c && c <= '9') {
                num = num * 16 + c - '0';
            } else {
                num = num * 16 + 10 + c - 'a';
            }
        }
        return num;
    }

}
