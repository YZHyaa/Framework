package com.xupt.yzh.mebatis;

/**
 * 最核心对象，对外基础
 * 1.在每次请求时创建（因为SqlSession线程不安全）
 * 2.SqlSession中包含了配置（Configuration）与执行器（Excutor）
 *      Excutor：具体执行sql（Jdbc操作），被SqlSession提供的默认方法解析sql后调用
 *      Configuration：定制化配置，返回Mapper代理对象（MapperProxy）
 */
public class MYSqlSession {

    private MYConfiguration configuration;

    private MYExecutor excutor;

    public MYSqlSession(MYConfiguration configuration) {
        this.configuration = configuration;
        this.excutor = new MYExecutor();
    }

    /**
     * 根据statementId解析sql，调用Excutor提供的方法执行查询
     * @param statementId
     * @param parameter
     * @param <T>
     * @return
     */
    public <T> T selectOne(String statementId, Object parameter) {
        // 根据statementId拿到SQL
        String sql = MYConfiguration.sqlMappings.getString(statementId);
        return excutor.query(sql, parameter);
    }

    /**
     * 通过Configuration获取提过给客户端的代理对象
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T getMapper(Class clazz) {
        return configuration.getMapper(clazz, this);
    }
}
