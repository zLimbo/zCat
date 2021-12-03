package com.zlimbo.zcat.test;


import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.parser.ParserException;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;

import java.util.List;

import static com.alibaba.druid.sql.SQLUtils.toSQLString;

public class SqlFormatTest {

    public static void main(String[] args) {
        String str = "create table `tb_user` (\n" +
                "  `id` varchar(22) not null COMMENT '主键id',\n" +
                "  `usercode` varchar(11) DEFAULT null COMMENT '手机号',\n" +
                "  `name` varchar(10) DEFAULT nu";
        System.out.println("格式化之前：");
        System.out.println(str);
        System.out.println("格式化之后：");
        try {
            SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(str, "mysql");
            List<SQLStatement> statementList = parser.parseStatementList();
            System.out.println("statementList: " + statementList);
            str = toSQLString((SQLObject) statementList, "mysql");
            System.out.println(str);
        } catch (ParserException e) {
            System.out.println("SQL转换中发生了错误："+e.getMessage());
        }
    }

//    public static String format(String sql) {
////        sql.
//    }
}
