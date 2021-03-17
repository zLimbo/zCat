package com.zlimbo.zcat.chain;

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
     * @param data 十六进制字符串
     * @return 交易数量
     */
    public String[] getCount(String data) {
        Event evet = new Event("Count", Arrays.<TypeReference<?>>asList(new TypeReference<Int>() {}));
        List<Type> result = FunctionReturnDecoder.decode(data, evet.getParameters());

        return new String[] { result.get(0).getValue().toString() };
    }

    /**
     * 解析十六进制字符串，获得其中最后三笔交易的日期
     *
     * @param data 十六进制字符串
     * @return 最后三笔交易的日期数组
     */
    public String [] getTheDatesOfLastThreeDeals(String data) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Event evet = new Event("TheDatesOfLastThreeDeals",
                Arrays.<TypeReference<?>>asList(new TypeReference<Int>() {},
                        new TypeReference<Int>() {},
                        new TypeReference.StaticArrayTypeReference<StaticArray<Int>>(3) {}));
        List<Type> result = FunctionReturnDecoder.decode(data, evet.getParameters());


//        System.out.println(result.size());
//        System.out.println(result.get(0).getValue());
//        System.out.println(result.get(1).getValue());

        StaticArray<Int> tmp = (StaticArray<Int>) result.get(2);
//        System.out.println(df.format(tmp.getValue().get(0).getValue()));
//        System.out.println(df.format(tmp.getValue().get(1).getValue()));
//        System.out.println(df.format(tmp.getValue().get(2).getValue()));

        String [] ans = new String[] {
                df.format(tmp.getValue().get(0).getValue()),
                df.format(tmp.getValue().get(1).getValue()),
                df.format(tmp.getValue().get(2).getValue()) };

        return ans;
    }

    public static void main(String[] args) {
        Parse parse = new Parse();
        String countData = "0x0000000000000000000000000000000000000000000000000000000000000002";
        String threeDateData = "0x0000000000000000000000000000000000000000000000000000000000000020000000000000000000000000000000000000000000000000000000000000000300000000000000000000000000000000000000000000000000000178259e951c00000000000000000000000000000000000000000000000000000178259eac7700000000000000000000000000000000000000000000000000000178259ecf8c";

        parse.getCount(countData);
        parse.getTheDatesOfLastThreeDeals(threeDateData);
    };


}
