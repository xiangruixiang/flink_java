package com.zhixueyun.flink.bus.ETL;

import com.zhixueyun.flink.utils.DbUtils;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

/**
 * 批量数据存入Mysql
 */
public class SinkToMySQL_JsonArray extends RichSinkFunction<PersonActivity> {
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
        connection = DbUtils.getConnection();

        String sql = "replace into active(f_member_id, f_business_id, f_business_type, f_bueinsess_name,f_business_status," +
                "f_start_time, f_end_time, f_status, f_company_id, f_create_time, f_modify_date) values(?, ?, ?, ?,?, ?, ?, ?,?, ?, ?);";
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
    public void invoke(PersonActivity value, Context context) throws Exception {

       // for (PersonActivity employee : value) {
            ps.setString(1, value.getF_member_id());
            ps.setString(2, value.getF_business_id());
            ps.setString(3, value.getF_business_type());
            ps.setString(4, value.getF_bueinsess_name());
            ps.setInt(5, value.getF_business_satus());
            ps.setInt(6, value.getF_start_time());
            ps.setInt(7, value.getF_end_time());
            ps.setInt(8, value.getF_status());
            ps.setString(9, value.getF_company_id());
            ps.setInt(10, value.getF_create_time());
            ps.setInt(11, value.getF_modify_date());
            ps.addBatch();
       // }

        int[] count = ps.executeBatch();//批量后执行
        System.out.println("成功了插入了" + count.length + "行数据");
    }



}