package com.zhixueyun.flink.bus.join.WaterAndWater;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zhixueyun.flink.utils.KafkaConfigUtil;
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
public class TableGetJsonArrayWaterWater {

    static String testTopic = "test";
    static String devTopic = "dev";

    public static void main(String[] args) throws Exception{

        //构建流执行环境
        StreamExecutionEnvironment streamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        EnvironmentSettings environmentSettings = EnvironmentSettings.newInstance().useBlinkPlanner().inStreamingMode().build();
        StreamTableEnvironment streamTableEnvironment = StreamTableEnvironment.create(streamExecutionEnvironment, environmentSettings);

        //控制延迟 10毫秒
        streamExecutionEnvironment.setBufferTimeout(10);

        //定义处理时间
        streamExecutionEnvironment.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        //kafka参数配置
        Properties prop = KafkaConfigUtil.buildKafkaProps(args[0]);//kafka参数配置


        ///////////////////////////////////////////////
        //配置消费者
        FlinkKafkaConsumer<String> testConsumer = new FlinkKafkaConsumer<String>(testTopic, new SimpleStringSchema(), prop);

        //配置水印
        testConsumer.assignTimestampsAndWatermarks(WatermarkStrategy.forBoundedOutOfOrderness(Duration.ofSeconds(20)));

        //获取kafka数据
        DataStreamSource<String> testKafkaStream= streamExecutionEnvironment.addSource(testConsumer).setParallelism(1);

        //数据过滤
        SingleOutputStreamOperator<Person> testStream = testKafkaStream
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

        testStream.print();


       //将流数据转换成虚拟表
        streamTableEnvironment.createTemporaryView("person",testStream , $("id"), $("name"), $("areaid"), $("createtime"));
        ///////////////////////////////////////////////



        ///////////////////////////////////////////////
        //配置消费者
        FlinkKafkaConsumer<String> devConsumer = new FlinkKafkaConsumer<String>(devTopic, new SimpleStringSchema(), prop);

        //配置水印
        devConsumer.assignTimestampsAndWatermarks(WatermarkStrategy.forBoundedOutOfOrderness(Duration.ofSeconds(20)));

        //获取kafka数据
        DataStreamSource<String> devKafkaStream = streamExecutionEnvironment.addSource(devConsumer).setParallelism(1);

        //数据过滤
        SingleOutputStreamOperator<PersonActivity> devStream = devKafkaStream
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
                }).map(new MapFunction<String, PersonActivity>() {
                    @Override
                    public PersonActivity map(String string) throws Exception {
                        Gson gson = new Gson();
                        return gson.fromJson(string, PersonActivity.class);
                    }
                });

         devStream.print();

        //将流数据转换成虚拟表
        streamTableEnvironment.createTemporaryView("active",devStream
                , $("f_business_name"), $("f_business_id"), $("f_business_type")
                , $("f_business_status"), $("f_company_id"), $("f_create_time")
                , $("f_end_time"), $("f_member_id"), $("f_modify_date")
                , $("f_start_time"), $("f_status"));
        ///////////////////////////////////////////////



        //维度表与流数据合并
        Table result = streamTableEnvironment.sqlQuery("SELECT person.*, active.f_company_id, active.f_modify_date " +
                "FROM person " +
                "INNER JOIN active ON person.areaid = active.f_business_status");
        result.printSchema();


        //输出结果到控制台
        streamTableEnvironment.toAppendStream(result, ActivecPerson.class).print();

        //将关联查询结果转换为POJO
        DataStream<ActivecPerson> dsRow = streamTableEnvironment.toAppendStream(result, ActivecPerson.class);
        dsRow.timeWindowAll(Time.seconds(5))
                .apply(new AllWindowFunction<ActivecPerson, List<ActivecPerson>, TimeWindow>() {
                    @Override
                    public void apply(TimeWindow window, Iterable<ActivecPerson> values, Collector<List<ActivecPerson>> out) throws Exception {
                        ArrayList<ActivecPerson> employees = Lists.newArrayList(values);
                        if (employees.size() > 0) {
                            System.out.println("total分钟内收集到 employee 的数据条数是：" + employees.size());
                            out.collect(employees);
                        }
                    }
                }).addSink(new SinkToMySQL());

        streamExecutionEnvironment.execute("kafka 消费任务开始");
    }

}

