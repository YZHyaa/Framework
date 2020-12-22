package com.xupt.yzh.mebatis;

public class MYSqlSessionFactory {

    public MYSqlSession getSqlSession() {
        return new MYSqlSession(new MYConfiguration());
    }

}
