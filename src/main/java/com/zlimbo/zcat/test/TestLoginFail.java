package com.zlimbo.zcat.test;

import com.zlimbo.zcat.connect.ConnectionParam;
import com.zlimbo.zcat.connect.SqlConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.DriverManager;
import java.text.SimpleDateFormat;

public class TestLoginFail {

    private static final Logger logger = LoggerFactory.getLogger(TestLoginFail.class);

    static public void main(String[] args) {
        long count = 0L;
        ConnectionParam connectionParam = new ConnectionParam(
                "10.60.178.75", "3308", "cita_sql", "citauser", "admin0");
        SqlConnector sqlConnector = new SqlConnector(connectionParam);
        while (true) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ++count;
            logger.debug(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis()) +
                    " connect count: " + count);
            String url = "jdbc:mysql://" +
                    connectionParam.getHost() + ":" +
                    connectionParam.getPort() + "/" +
                    connectionParam.getDatabase() +
                    "?useSSL=false" +
                    "&useUnicode=true" +
                    "&characterEncoding=UTF8" +
                    "&serverTimezone=GMT" +
                    "&allowPublicKeyRetrieval=true";
            try {
                Class.forName("com.mysql.jdbc.Driver");
                DriverManager.getConnection(
                        url, connectionParam.getUser(), connectionParam.getPassword());
            } catch (Exception e) {
            }

            logger.debug(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis()) +
                    " connect result failed!");
        }
    }
}