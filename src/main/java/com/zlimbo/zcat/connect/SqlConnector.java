package com.zlimbo.zcat.connect;

import com.zlimbo.zcat.config.ZCatConfig;
import com.zlimbo.zcat.parse.CatParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class SqlConnector {

    /**
     * 日志
     */
    final Logger logger = LoggerFactory.getLogger(getClass());


    /**
     * sql查询结果类
     */
    public static class SqlQueryResult {
        private final List<String> columns;
        private final List<List<String>> records;
        private String errorMessage;
        private long spendTime;

        public SqlQueryResult() {
            columns = new ArrayList<>();
            records = new ArrayList<>();
            errorMessage = null;
            spendTime = 0;
        }

        public SqlQueryResult(List<String> columns, List<List<String>> records, long spendTime, String errorMessage) {
            this.columns = columns;
            this.records = records;
            this.spendTime = spendTime;
            this.errorMessage = errorMessage;
        }

        public List<String> getColumns() {
            return columns;
        }

        public List<List<String>> getRecords() {
            return records;
        }

        public long getSpendTime() {
            return spendTime;
        }

        public void setSpendTime(long spendTime) {
            this.spendTime = spendTime;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }


    private final ConnectionParam connectionParam;
    private Connection connection;
    private boolean onConnect;
    private String errorMessage;

    private int testExecuteNum = 1;

    public void setTestExecuteNum(int testExecuteNum) {
        this.testExecuteNum = testExecuteNum < 0 ? 1 : testExecuteNum;
    }

    /**
     * 数据库连接
     *
     * @param connectionParam
     */
    public SqlConnector(ConnectionParam connectionParam) {
        logger.debug("[SqlControl] start");

        this.connectionParam = connectionParam;

        logger.debug("[SqlConnector] end");
    }

    public boolean openConnect() {
        String url = "jdbc:mysql://" +
                connectionParam.getHost() + ":" +
                connectionParam.getPort() + "/" +
                connectionParam.getDatabase() +
                "?useSSL=false" +
                "&useUnicode=true" +
                "&characterEncoding=UTF8" +
                "&serverTimezone=GMT" +
                "&allowPublicKeyRetrieval=true";
        logger.debug("database url: " + url);
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(
                    url, connectionParam.getUser(), connectionParam.getPassword());
            onConnect = true;
        } catch (Exception e) {
            logger.error("database connect fail: " + e.getMessage());
            errorMessage = e.getMessage();
            onConnect = false;
//            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void closeConnect() {
        try {
            if (connection != null) {
                connection.close();
                connection = null;
            }
        } catch (Exception e) {
            logger.error("closeConnect fail: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * sql语句: show tables
     *
     * @return
     */
    public List<String> sqlShowTables() {
        logger.debug("[sqlShowTables] start");
        String sql = "show tables";
        List<String> tables = new ArrayList<>();
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                logger.debug("table: " + resultSet.getString(1));
                tables.add(resultSet.getString(1));
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        } finally {
            //finally block used to close resources
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException se) {
                logger.error(se.getMessage());
                logger.error(se.getSQLState());
                se.printStackTrace();
            }
        }
        logger.debug("[sqlShowTables] end");
        return tables;
    }


    /**
     * sql语句：create table
     *
     * @param sql
     * @return
     */
    public SqlQueryResult sqlCreateTable(String sql) {
        logger.debug("[sqlCreateTable] start");
        logger.debug("sql:" + sql);
        SqlQueryResult sqlQueryResult = new SqlQueryResult();
        long start = System.currentTimeMillis();
        String errorMessage = null;
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.executeUpdate(sql);
        } catch (Exception e) {
            logger.error(e.getMessage());
            sqlQueryResult.setErrorMessage(e.getMessage());
            e.printStackTrace();
        } finally {
            //finally block used to close resources
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException se) {
                logger.error(se.getMessage());
                logger.error(se.getSQLState());
                se.printStackTrace();
            }
        }

        long end = System.currentTimeMillis();
        long spendTime = end - start;
        sqlQueryResult.setSpendTime(spendTime);
        logger.debug("[sqlCreateTable] end");
        return sqlQueryResult;
    }


    /**
     * sql 查询
     *
     * @param sql
     * @return
     */
    public SqlQueryResult sqlQuery(String sql) {
        logger.debug("[sqlQuery] start");
        logger.debug("sql:" + sql);

        long start = System.currentTimeMillis();

        String errorMessage = null;
        List<String> columns = new ArrayList<>();
        List<List<String>> records = new ArrayList<>();
        PreparedStatement preparedStatement = null;
        try {
//            Statement statement = connection.createStatement();
//            ResultSet resultSet = statement.executeQuery(sql);

            preparedStatement = connection.prepareStatement(sql);
            logger.debug(preparedStatement.toString());
            long start1 = System.currentTimeMillis();

            // for test
            if (ZCatConfig.FOR_TEST) {
                for (int i = 0; i < testExecuteNum - 1; ++i) {
                    long start2 = System.currentTimeMillis();
                    preparedStatement.executeQuery();
                    double spendTime = (System.currentTimeMillis() - start2) / 1000d;
                    double spendTime1 = (System.currentTimeMillis() - start) / 1000d;
                    System.out.println("循环了第 " + i + " 次， 本次耗时为 " + spendTime + " s， 总耗时为 " + spendTime1);
                }
            }
            ResultSet resultSet = preparedStatement.executeQuery();

            long end2 = System.currentTimeMillis();
            logger.debug("jdbc query spend: {}s", (double) (end2 - start1) / 1000);
            assert resultSet != null;
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            int columnCount = resultSetMetaData.getColumnCount();

            for (int i = 0; i < columnCount; ++i) {
                String columnName = resultSetMetaData.getColumnName(i + 1);
                columns.add(columnName);
            }

            while (resultSet.next()) {
                List<String> record = new ArrayList<String>();
                for (int i = 0; i < columnCount; ++i) {
                    record.add(resultSet.getString(i + 1));
                }
                records.add(record);
            }
            resultSet.close();
        } catch (Exception e) {
            logger.error(e.getMessage());
            errorMessage = e.getMessage();
            //Handle errors for Class.forName
            e.printStackTrace();
        } finally {
            //finally block used to close resources
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException se) {
                logger.error(se.getMessage());
                logger.error(se.getSQLState());
                se.printStackTrace();
            }
        }

        long end = System.currentTimeMillis();
        long spendTime = end - start;

        logger.debug("[sqlQuery] end");
        return new SqlQueryResult(columns, records, spendTime, errorMessage);
    }


    /**
     * 状态查询
     *
     * @param sql
     * @return
     */
    public SqlQueryResult sqlQueryForState(String sql) {
        logger.debug("[sqlQueryForState] start");
        logger.debug("sql:" + sql);

        long start = System.currentTimeMillis();

        String errorMessage = null;
        List<String> columns = new ArrayList<>();
        List<List<String>> records = new ArrayList<>();
        PreparedStatement preparedStatement = null;
        try {
            Statement statement = connection.createStatement();
            long start2 = System.currentTimeMillis();
            ResultSet resultSet = statement.executeQuery(sql);
            long end2 = System.currentTimeMillis();
            logger.debug("jdbc query spend: {}s", (double) (end2 - start2) / 1000);
            if (resultSet.next()) {
                String data = resultSet.getString(1);
                data = removeEndZero(data);
                data = removeEndZero(data);
//                System.out.println("data length: " + data.length());
                if (data.length() == ZCatConfig.STATE_COUNT_LEN) {
                    columns.add("TX_COUNT");
                    String[] result = CatParser.parseCount(data);
                    for (String element : result) {
                        List<String> record = Collections.singletonList(element);
                        records.add(record);
                    }
                } else if (data.length() == ZCatConfig.STATE_TIME_LEN) {
                    columns.add("TX_TIME");
                    String[] result = CatParser.parseTime(data);
                    for (String element : result) {
                        List<String> record = Collections.singletonList(handleTime(element));
                        records.add(record);
                    }
                } else if (data.length() == ZCatConfig.STATE_MIX_LEN) {
                    columns.add("TX_COUNT");
                    columns.add("TX_TIME");
                    String[][] result = CatParser.parseMix(data);
                    for (int i = 0; i < 3; ++i) {
                        List<String> record = new ArrayList<>();
                        record.add(result[0][0]);
                        logger.debug("result[][]: [{}]", result[1][i]);
                        record.add(handleTime(result[1][i]));
                        records.add(record);
                    }
                } else {
                    errorMessage = "the data(" + data + ") can't parse!";
                }
                logger.debug("records: " + records);
            }
            resultSet.close();
        } catch (Exception e) {
            logger.error(e.getMessage());
            errorMessage = e.getMessage();
            //Handle errors for Class.forName
            e.printStackTrace();
        } finally {
            //finally block used to close resources
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException se) {
                logger.error(se.getMessage());
                logger.error(se.getSQLState());
                se.printStackTrace();
            }
        }

        long end = System.currentTimeMillis();
        long spendTime = end - start;

        logger.debug("[sqlQuery] end");
        return new SqlQueryResult(columns, records, spendTime, errorMessage);
    }

    /**
     * sql语句：insert、update、alter, delete, drop 等相关update操作
     *
     * @param sql
     * @return
     */
    public SqlQueryResult sqlUpdate(String sql) {
        logger.debug("[sqlUpdate] start");
        logger.debug("sql:" + sql);
        SqlQueryResult sqlQueryResult = new SqlQueryResult();
        long start = System.currentTimeMillis();

        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(sql);
            // for test
            if (ZCatConfig.FOR_TEST) {
                for (int i = 0; i < testExecuteNum; ++i) {
                    long start1 = System.currentTimeMillis();
                    preparedStatement.executeUpdate();
                    double spendTime = (System.currentTimeMillis() - start1) / 1000d;
                    double spendTime1 = (System.currentTimeMillis() - start) / 1000d;
                    System.out.println("循环了第 " + i + " 次， 本次耗时为 " + spendTime + " s， 总耗时为 " + spendTime1 + "s");
                }
            } else {
                preparedStatement.executeUpdate();
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            sqlQueryResult.setErrorMessage(e.getMessage());
            e.printStackTrace();
        } finally {
            //finally block used to close resources
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException se) {
                logger.error(se.getMessage());
                logger.error(se.getSQLState());
                se.printStackTrace();
            }
        }
        long end = System.currentTimeMillis();
        long spendTime = end - start;
        sqlQueryResult.setSpendTime(spendTime);
        logger.debug("[sqlUpdate] end");
        return sqlQueryResult;
    }


    /**
     * 获取某一张表的列数据
     *
     * @param tableName
     * @return
     */
    public List<String> getColumns(String tableName) {
        logger.debug("[getColumns] start");

        List<String> columns = new ArrayList<>();
        try {
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            ResultSet columnSet = databaseMetaData.getColumns(null, "%", tableName, "%");
            while (columnSet.next()) {
                String columnName = columnSet.getString("COLUMN_NAME");
                columns.add(columnName);
                logger.debug("columnName: " + columnName);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        logger.debug("[getColumns] end");
        return columns;
    }


    public int selectCount(String tableName) {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        SqlQueryResult result = sqlQuery(sql);
        return Integer.parseInt(result.getRecords().get(0).get(0));
    }

    public ConnectionParam getConnectionParam() {
        return connectionParam;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getDatabase() {
        return connectionParam.getDatabase();
    }

    public String getHost() {
        return connectionParam.getHost();
    }

    public String getPort() {
        return connectionParam.getPort();
    }

    public String getUser() {
        return connectionParam.getUser();
    }

    public String getPassword() {
        return connectionParam.getPassword();
    }

    public boolean isOnConnect() {
        return onConnect;
    }

    Connection getConnection() {
        return connection;
    }

    private String removeEndZero(String s) {
        // 去除末尾终止0
        if (s.charAt(s.length() - 1) == 0) {
            s = s.substring(0, s.length() - 1);
        }
        logger.debug("s length: " + s.length());
        return s;
    }

    private String handleTime(String time) {
        return "1970-01-01 08:00:00".equals(time) ? "-" : time;
    }
}
