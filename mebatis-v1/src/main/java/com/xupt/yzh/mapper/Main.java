package com.xupt.yzh.mapper;

import com.xupt.yzh.mebatis.MYSqlSession;
import com.xupt.yzh.mebatis.MYSqlSessionFactory;

public class Main {
    public static void main(String[] args) {
        MYSqlSessionFactory sqlSessionFactory = new MYSqlSessionFactory();
        MYSqlSession sqlSession = sqlSessionFactory.getSqlSession();

        // User user = sqlSession.selectOne("com.xupt.yzh.mapper.UserMapper.selectUserById", 1);

        UserMapper UserMapper = sqlSession.getMapper(UserMapper.class);
        User user = UserMapper.selectUserById(1);

        System.out.println(user);
    }
}
