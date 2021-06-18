package com.zlimbo.zcat.test;


import com.alibaba.fastjson.JSONObject;
import com.zlimbo.zcat.connect.ConnectionParam;
import com.zlimbo.zcat.connect.SqlConnector;
import com.zlimbo.zcat.util.CommonUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;


public class TestInsert {

    private final SqlConnector sqlConnector;
    Random random = new Random();

    public TestInsert(ConnectionParam connectionParam) {
//        connectionParam = new ConnectionParam(
//                "10.60.178.75", "3308", "cita_sql", "citauser", "admin");
//        connectionParam = new ConnectionParam(
//                "192.168.6.107", "3100", "ouyeel_cita", "root", "admin");

        sqlConnector = new SqlConnector(connectionParam);

        if (!sqlConnector.openConnect()) {
            System.out.println(sqlConnector.getHost() + ":" + sqlConnector.getPort() + " connect failed!");
            System.exit(-1);
        } else {
            System.out.println(sqlConnector.getHost() + ":" + sqlConnector.getPort() + "connect success!");
        }
    }

    public void testInsert(String tableName, InfoGenerator dataInfoGt, InfoGenerator pubInfoGt) {

        int n = 1000;

        for (int i = 1; i <= n; ++i) {
//            try {
//                Thread.sleep(3000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }

            String systemId = String.format("%012d", Math.abs(random.nextInt()));
            String requestSn = UUID.randomUUID().toString();
            String invokeTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis());
            String businessId = String.format("%012d", Math.abs(random.nextInt()));
//            String callbackUrl = "https://127.0.0.1/callback";
            String callbackUrl = "gas1000";
            String keyId = String.format("%06d", Math.abs(random.nextInt() % (int)1e7));
            String accountId = String.format("%06d", Math.abs(random.nextInt() % (int)1e7));
            String sm4Key = "0123456789abcdef0123456789abcdef";
            String sm4Iv = "0123456789abcdef0123456789abcdef";
            String priKey = "d6c83aee4bfbeb135a2dcef8c803b186d0678a99002b09d3c60c22aca7105005";
            String pubKey = "2204404536ab867d9a964bfcc5e6fdaa7d77e509ce5891d38b3ebbb036e5c225994597ea6d0bdff3539fd3062b3943a1c7dd75d173f35101b71298e9f7f08d51";
            String dataInfo = dataInfoGt.genInfo();
            String pubInfo = pubInfoGt.genInfo();

            String sql = String.format("INSERT INTO " + tableName +
                            "(SYSTEM_ID, REQUEST_SN, INVOKE_TIME, BUSINESS_ID, CALLBACK_URL, KEY_ID, ACCOUNT_ID, DATA_INFO, SM4_KEY, SM4_IV, PRI_KEY, PUB_KEY, PUB_INFO)\n" +
                            "VALUES('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s')",
                    systemId, requestSn, invokeTime, businessId, callbackUrl, keyId, accountId, dataInfo, sm4Key, sm4Iv, priKey, pubKey, pubInfo);

//            System.out.println("table: " + tableName + ", count: " + ++count);
//            System.out.println("sql: " + sql);
            if (i % 200 == 0) {
                System.out.println(sqlConnector.getPort() + " " + Thread.currentThread().getId() + " " + i);
            }
            SqlConnector.SqlQueryResult result = sqlConnector.sqlUpdate(sql);
            if (result.getErrorMessage() != null) {
                System.out.println("insert error");
            }
        }
    }

    static public void test(ConnectionParam connectionParam) {
        TestInsert testInsert = new TestInsert(connectionParam);

        InvoiceInfo invoiceInfo = new InvoiceInfo();
        testInsert.testInsert("invoice", invoiceInfo::genDataInfoForInvoice, invoiceInfo::genPubInfoForInvoice);

//        int theadNum = 2;
//        List<Thread> list = new ArrayList<>();
//        for (int i = 0; i < theadNum; ++i) {
//            Thread thread = new Thread(() -> {
//                System.out.println("testInsert");
//                testInsert.testInsert("invoice", invoiceInfo::genDataInfoForInvoice, invoiceInfo::genPubInfoForInvoice);
//            });
//            thread.start();
//            list.add(thread);
//        }
//        for (Thread thread: list) {
//            try {
//                thread.join();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
    }


    static public void main(String[] args) throws InterruptedException {

        long start = System.currentTimeMillis();
        List<Thread> list = new ArrayList<>();
        for (int port = 3100; port <= 3103; ++port) {
            int finalPort = port;
            Thread thread = new Thread(() -> {
                System.out.println("port: " + finalPort);
                test(new ConnectionParam(
                        "192.168.6.111",
                        String.valueOf(finalPort),
                        "ouyeel_cita",
                        "root",
                        "admin"));
            });
            thread.start();
            list.add(thread);
        }
        for (int port = 3100; port <= 3103; ++port) {
            int finalPort = port;
            Thread thread = new Thread(() -> {
                System.out.println("port: " + finalPort);
                test(new ConnectionParam(
                        "192.168.6.114",
                        String.valueOf(finalPort),
                        "ouyeel_cita",
                        "root",
                        "admin"));
            });
            thread.start();
            list.add(thread);
        }
        for (Thread thread: list) {
            thread.join();
        }
        double spendTime = (System.currentTimeMillis()- start) / 1000.0;
        System.out.println("total time: " + spendTime + "s, tps: " + (1000 * 6 / spendTime));

        // 佟兴
//        new Thread(() -> {
//            test(new ConnectionParam(
//                    "192.168.6.114", "3100", "dasfaa", "root", "admin"));
//        }).start();
//
//
//        // 海波
//        new Thread(() -> {
//            test(new ConnectionParam(
//                    "192.168.6.107", "3100", "ouyeel_cita", "root", "admin"));
//        }).start();

//        new Thread(() -> {
//            test(new ConnectionParam(
//                    "192.168.192.133", "3100", "ouyeel_cita", "root", "admin"));
//        }).start();
//        TestInsert testInsert = new TestInsert(new ConnectionParam(
//                "192.168.6.111", "3100", "ouyeel_cita", "root", "admin"));
//        InvoiceInfo invoiceInfo = new InvoiceInfo();
//        testInsert.testInsert("invoice_s", invoiceInfo::genDataInfoForInvoice, invoiceInfo::genPubInfoForInvoice);
    }

    public String genDataInfoForDeliveryActual() {
        String deliveryActualId = String.format("%07d", Math.abs(random.nextInt() % (int)1e7));
        String feedBackList = String.format("%07d", Math.abs(random.nextInt() % (int)1e7));
        String commissionId = String.format("%07d", Math.abs(random.nextInt() % (int)1e7));
        String createPerson = String.format("%05d", Math.abs(random.nextInt() % (int)1e5));
        String createDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis());
        String dataType = String.valueOf(random.nextInt() % 2);

        JSONObject json = new JSONObject();
        json.put("deliveryActualId", deliveryActualId);
        json.put("feedBackList", feedBackList);
        json.put("commissionId", commissionId);
        json.put("createPerson", createPerson);
        json.put("createDate", createDate);
        json.put("dataType", dataType);

        json = CommonUtils.smallHumpToUpperUnderline(json);
        return json.toJSONString();
    }
}
