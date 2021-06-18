package com.zlimbo.zcat.test;

import com.zlimbo.zcat.connect.ConnectionParam;
import com.zlimbo.zcat.connect.SqlConnector;

import java.text.SimpleDateFormat;
import java.util.Random;
import java.util.UUID;

public class InsertAndSelectForState {

    public static void main(String[] args) {
        ConnectionParam connectionParam = new ConnectionParam(
                "192.168.6.112",
                "3100",
                "ouyeel",
                "root",
                "admin"
        );

        SqlConnector sqlConnector = new SqlConnector(connectionParam);
        if (!sqlConnector.openConnect()) {
            System.out.println(sqlConnector.getHost() + ":" + sqlConnector.getPort() + " connect failed!");
            System.exit(-1);
        } else {
            System.out.println(sqlConnector.getHost() + ":" + sqlConnector.getPort() + " connect success!");
        }

        Random random = new Random();
        InvoiceInfo invoiceInfo = new InvoiceInfo();
        InfoGenerator dataInfoGt = invoiceInfo::genDataInfoForInvoice;
        InfoGenerator pubInfoGt = invoiceInfo::genPubInfoForInvoice;
        String tableName = "invoice";
        String[] stateQuerys = {
                "SELECT * FROM " + tableName + " using state",
                "SELECT time_tx FROM " + tableName + " using state",
                "SELECT count_tx FROM " + tableName + " using state",
        };

        long preTime = System.currentTimeMillis();

        while (true) {
            String systemId = String.format("%012d", Math.abs(random.nextInt()));
            String requestSn = UUID.randomUUID().toString();
            String invokeTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis());
            String businessId = String.format("%012d", Math.abs(random.nextInt()));
            String callbackUrl = "https://127.0.0.1/callback";
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

            SqlConnector.SqlQueryResult result = sqlConnector.sqlUpdate(sql);
            if (result.getErrorMessage() != null) {
                System.out.println("insert error");
            }

            long curTime = System.currentTimeMillis();
            if (curTime - preTime >= 3000) {
                preTime = curTime;
                System.out.println();
                for (String query: stateQuerys) {
                    System.out.println(">>> sql: " + query);
                    SqlConnector.SqlQueryResult result1 = sqlConnector.sqlQueryForState(query);
                    if (result1.getErrorMessage() == null) {
                        System.out.println(result1.getRecords());
                    } else {
                        System.out.println("query state error!");
                    }
                }
            }
        }
    }
}
