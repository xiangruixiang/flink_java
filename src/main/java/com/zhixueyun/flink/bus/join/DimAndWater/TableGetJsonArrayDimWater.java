package com.zhixueyun.flink.bus.join.DimAndWater;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.shaded.guava18.com.google.common.collect.Lists;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.AllWindowFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.util.Collector;
import com.zhixueyun.flink.utils.KafkaConfigUtil;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.apache.flink.table.api.Expressions.$;


/**
 * @author 向瑞祥
 * @Date 2020-09-04
 * 本示例用于插入json数组data, 维度表与流水表统计，维度表实时变化,2张维度表，1个kafka实时流，实时3表关联
 *
 * {
 *     "data":[
 *         {
 *             "id":111,
 *             "name":"aa"
 *         },
 *         {
 *             "id":111,
 *             "name":"bbb"
 *         }
 *     ],
 *     "database":"dev",
 *     "es":1599043486000,
 *     "id":2,
 *     "isDdl":false,
 *     "table":"users1",
 *     "ts":1599043487861,
 *     "type":"INSERT"
 * }
 */
public class TableGetJsonArrayDimWater {

    static String topic = "test";

    public static void main(String[] args) throws Exception{

        //构建流执行环境
        StreamExecutionEnvironment streamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        EnvironmentSettings environmentSettings = EnvironmentSettings.newInstance().useBlinkPlanner().inStreamingMode().build();
        StreamTableEnvironment streamTableEnvironment = StreamTableEnvironment.create(streamExecutionEnvironment, environmentSettings);

        //控制延迟 10毫秒
        streamExecutionEnvironment.setBufferTimeout(10);

        //定义处理时间
        streamExecutionEnvironment.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);

        //kafka参数配置
        Properties prop = KafkaConfigUtil.buildKafkaProps();//kafka参数配置

        //配置消费者
        FlinkKafkaConsumer<String> consumer = new FlinkKafkaConsumer<String>(topic, new SimpleStringSchema(), prop);

        //配置水印
        consumer.assignTimestampsAndWatermarks(WatermarkStrategy.forBoundedOutOfOrderness(Duration.ofSeconds(20)));

        //获取kafka数据
        DataStreamSource<String> kafkaStream= streamExecutionEnvironment.addSource(consumer).setParallelism(1);

        //数据过滤
        SingleOutputStreamOperator<Person> stream = kafkaStream
                .filter(new FilterFunction<String>() {
            @Override
            public boolean filter(String s) throws Exception {
                JsonParser parser=new JsonParser();
                JsonObject value = (JsonObject) parser.parse(s);

                String type = value.get("type").getAsString();
                return type.equalsIgnoreCase("INSERT")
                        || type.equalsIgnoreCase("UPDATE") ;
            }})
                .flatMap(new FlatMapFunction<String, String>() {
            @Override
            public void flatMap(String jsonObject, Collector<String> collector) throws Exception {
                JsonParser parser=new JsonParser();
                JsonObject value = (JsonObject) parser.parse(jsonObject);
                JsonArray result= (JsonArray) value.get("data");

                for(int i=0;i<result.size(); i++){
                    collector.collect(result.get(i).toString());
                }
            }
        }).map(new MapFunction<String, Person>() {
            @Override
            public Person map(String string) throws Exception {
                Gson gson = new Gson();
                return gson.fromJson(string, Person.class);
            }
        });

       // stream.print();

       //将流数据转换成虚拟表
        streamTableEnvironment.createTemporaryView("ratesHistory",stream , $("id"), $("name"), $("areaid"), $("proctime").proctime());

        //读取实时维度表
        String dimDDL = "CREATE TABLE dim_mysql (" +
                "  id int," +
                "  age int," +
                "  PRIMARY KEY (id) NOT ENFORCED" +
                ") WITH (" +
                "   'connector' = 'jdbc'," +
                "   'url' = 'jdbc:mysql://192.168.0.53:3306/dev'," +
                "   'table-name' = 'personInfo'," +
                "   'username' = 'root'," +
                "   'password' = 'Xiang987'" +
                ")";
        streamTableEnvironment.executeSql(dimDDL);


        //读取实时维度表
        String dimDDL1 = "CREATE TABLE area (" +
                "  id int," +
                "  area string," +
                "  PRIMARY KEY (id) NOT ENFORCED" +
                ") WITH (" +
                "   'connector' = 'jdbc'," +
                "   'driver' = 'com.mysql.jdbc.Driver'," +
                "   'url' = 'jdbc:mysql://192.168.0.53:3306/dev'," +
                "   'table-name' = 'areaname'," +
                "   'username' = 'root'," +
                "   'password' = 'Xiang987'" +
                ")";
        streamTableEnvironment.executeSql(dimDDL1);

        //维度表与流数据合并
        Table result = streamTableEnvironment.sqlQuery(
                "SELECT kafka.id, kafka.name, area.area, mysql.age " +
                        "FROM ratesHistory AS kafka " +
                        "INNER JOIN area FOR SYSTEM_TIME AS OF kafka.proctime AS area ON kafka.areaid=area.id " +
                        "INNER JOIN dim_mysql FOR SYSTEM_TIME AS OF kafka.proctime AS mysql ON area.id=mysql.id");

        //输出结果到控制台
        streamTableEnvironment.toAppendStream(result, Employ.class).print();

        //将关联查询结果转换为POJO
        DataStream<Employ> dsRow = streamTableEnvironment.toAppendStream(result, Employ.class);
        dsRow.timeWindowAll(Time.seconds(5))
                .apply(new AllWindowFunction<Employ, List<Employ>, TimeWindow>() {
                    @Override
                    public void apply(TimeWindow window, Iterable<Employ> values, Collector<List<Employ>> out) throws Exception {
                        ArrayList<Employ> employees = Lists.newArrayList(values);
                        if (employees.size() > 0) {
                            System.out.println("total分钟内收集到 employee 的数据条数是：" + employees.size());
                            out.collect(employees);
                        }
                    }
                }).addSink(new SinkToMySQL());

        streamExecutionEnvironment.execute("kafka 消费任务开始");
    }



}

