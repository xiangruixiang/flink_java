package com.zhixueyun.flink.bus.join.WaterAndWater;

import com.zhixueyun.flink.utils.DbUtils;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

/**
 * 批量数据存入Mysql
 */
public class SinkToMySQL extends RichSinkFunction<List<ActivecPerson>> {
    PreparedStatement ps;
    private Connection connection;
    BasicDataSource dataSource;

    /**
     * open() 方法中建立连接，这样不用每次 invoke 的时候都要建立连接和释放连接
     *
     * @param parameters
     * @throws Exception
     */
    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);

       // dataSource=new BasicDataSource();
       // connection = DriverManager.getConnection(url, username, password);
       // connection=getConnection(dataSource);
/*

        String driver = "com.mysql.jdbc.Driver";
        String url = "jdbc:mysql://121.43.235.134:3306/dev?characterEncoding=utf8&useSSL=false";
        String username = "root";
        String password = "Xiang987";
        Class.forName(driver);
        connection = DriverManager.getConnection(url, username, password);

*/
        connection = DbUtils.getConnection();
        String sql = "replace into active_c_person(id, name, areaid, createtime, f_company_id,  f_modify_date) values(?, ?, ?, ?, ?, ?);";
        ps = this.connection.prepareStatement(sql);
    }


    @Override
    public void close() throws Exception {
        super.close();
        //关闭连接和释放资源
        if (connection != null) {
            connection.close();
        }
        if (ps != null) {
            ps.close();
        }
    }


    public void invoke(List<ActivecPerson> value, Context context) throws Exception {
        //遍历数据集合
        for (ActivecPerson person : value) {
            ps.setInt(1, person.getId());
            ps.setString(2, person.getName());
            ps.setInt(3, person.getAreaid());
            ps.setLong(4, person.getCreatetime());
            ps.setString(5, person.getF_company_id());
            ps.setLong(6, person.getF_modify_date());
            ps.addBatch();
        }

        int[] count = ps.executeBatch();//批量后执行
        System.out.println("成功了插入了" + count.length + "行数据");
    }

/*


    private static  Connection getConnection(BasicDataSource dataSource) {
       // Properties prop=new Properties();
        */
/*  prop.load(new FileInputStream("D:\\flink\\src\\main\\resources\\database.properties"));
          String driver=prop.getProperty("driver");
          String url=prop.getProperty("url");
          String username=prop.getProperty("Username");
          String password=prop.getProperty("Password");

         *//*


        String driver = "com.mysql.jdbc.Driver";
        String url = "jdbc:mysql://121.43.235.134:3306/dev";
        String username = "root";
        String password = "Xiang987";

        dataSource.setDriverClassName(driver);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        //设置连接池的参数
        dataSource.setInitialSize(10);
        dataSource.setMaxTotal(50);
        dataSource.setMinIdle(2);

        Connection con=null;
        try{
            con=dataSource.getConnection();
            System.out.println("创建连接池："+con);
        } catch (Exception e) {
            System.out.println("-----------greenplum get connection has exception,msg=" +e.getMessage());
        }
        return con;
    }
*/

}