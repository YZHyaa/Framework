package com.xupt.yzh.mebatis;

import com.xupt.yzh.mapper.User;

import java.sql.*;

/**
 * 具体jdbc操作，执行传入的sql
 * 注：这里的操作是写死的，只能对于Blog且只根据Id查询
 */
public class MYExecutor {
    public <T> T query(String sql, Object paramater) {
        Connection conn = null;
        Statement stmt = null;
        User user = new User();

        try {
            // 注册 JDBC 驱动
            Class.forName("com.mysql.jdbc.Driver");

            // 打开连接
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/mybatis", "root", "123456");

            // 执行查询
            stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery(String.format(sql, paramater));

            // 获取结果集
            while (rs.next()) {
                long uid = rs.getLong("uid");
                String name = rs.getString("name");
                String phone = rs.getString("phone");
                String email = rs.getString("email");

                user.setUid(uid);
                user.setName(name);
                user.setPhone(phone);
                user.setEmail(email);
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException se2) {
            }
            try {
                if (conn != null) conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        return (T)user;
    }
}
