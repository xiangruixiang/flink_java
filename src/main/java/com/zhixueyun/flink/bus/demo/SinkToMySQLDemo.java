package com.zhixueyun.flink.bus.demo;

import com.zhixueyun.flink.utils.DbUtils;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * 批量数据存入Mysql
 */
public class SinkToMySQLDemo extends RichSinkFunction<Results> {
    PreparedStatement ps;

    private Connection connection;

    /**
     * open() 方法中建立连接，这样不用每次 invoke 的时候都要建立连接和释放连接
     *
     * @param parameters
     * @throws Exception
     */
    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
/*
        String driver = "com.mysql.jdbc.Driver";
        String url = "jdbc:mysql://121.43.235.134:3306/dev?characterEncoding=utf8&useSSL=false";
        String username = "root";
        String password = "Xiang987";
        Class.forName(driver);
        connection = DriverManager.getConnection(url, username, password);
        */
        connection = DbUtils.getConnection();

        String sql = "replace into demo(create_time, member_id, study_time) values(?, ?, ?);";
        ps = this.connection.prepareStatement(sql);
    }


    @Override
    public void close() throws Exception {
        super.close();
        //关闭并释放资源
        if(connection != null) {
            connection.close();
        }

        if(ps != null) {
            ps.close();
        }
    }

    /**
     * 每条数据的插入都要调用一次 invoke() 方法
     *
     * @param value
     * @param context
     * @throws Exception
     */
    @Override
    public void invoke(Results value, Context context) throws Exception {
        //遍历数据集合
       // for (Results employee : value) {
            ps.setString(1, value.getCreateTime());
            ps.setInt(2, value.getMemberId());
            ps.setInt(3, value.getStudyTotalTime());
            ps.addBatch();
      //  }

     /*
        for (Employee employee : value) {
            ps.setString(1, employee.getMemberId());
            ps.setString(2, employee.getBueinsessName());
            ps.setString(3, employee.getBusinessType());
            ps.setString(4, employee.getBueinsessName());
            ps.setInt(5, employee.getBusinessStatus());
            ps.setInt(6, employee.getStartTime());
            ps.setInt(7, employee.getEndTime());
            ps.setInt(8, employee.getActivityStatus());
            ps.setString(9, employee.getCompanyId());
            ps.setInt(10, employee.getCreateTime());
            ps.setInt(11, employee.getModifyDate());
            ps.addBatch();
        }
        */

        int[] count = ps.executeBatch();//批量后执行
        System.out.println("成功了插入了" + count.length + "行数据");
    }



}