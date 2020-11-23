package com.zhixueyun.flink.bus.ETL;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zhixueyun.flink.bus.ETL.SinkToMySQL_JsonArray;
import com.zhixueyun.flink.bus.ETL.PersonActivity;
import com.zhixueyun.flink.utils.KafkaConfigUtil;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.runtime.state.filesystem.FsStateBackend;
import org.apache.flink.shaded.guava18.com.google.common.collect.Lists;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.AllWindowFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.util.Collector;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author 向瑞祥
 * @Date 2020-09-04
 * 本示例用于插入json数组data
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
public class GetJsonArray {

    static String topic = "test";

    public static void main(String[] args) throws Exception{
        //构建流执行环境
        StreamExecutionEnvironment streamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();

        //控制延迟 10毫秒
        //streamExecutionEnvironment.setBufferTimeout(10);

        //设置并行度
        streamExecutionEnvironment.setParallelism(1);

        //设置检查点，防止数据丢失,10秒
        streamExecutionEnvironment.enableCheckpointing(10000);

        // 确保检查点之间有至少500 ms的间隔【checkpoint最小间隔】
        streamExecutionEnvironment.getCheckpointConfig().setMinPauseBetweenCheckpoints(500);

        // 检查点必须在一分钟内完成，或者被丢弃【checkpoint的超时时间】
        streamExecutionEnvironment.getCheckpointConfig().setCheckpointTimeout(60000);

        // 同一时间只允许进行一个检查点
        streamExecutionEnvironment.getCheckpointConfig().setMaxConcurrentCheckpoints(1);

        //设置statebackend
        streamExecutionEnvironment.setStateBackend(new FsStateBackend("file:///mnt/flink/checkpoint",false));

        //系统异常退出或人为 Cancel 掉，不删除checkpoint数据
        streamExecutionEnvironment.getCheckpointConfig().enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);

        // 设置精确消费一次
        streamExecutionEnvironment.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);

        //重启策略
        streamExecutionEnvironment.setRestartStrategy(RestartStrategies.noRestart());

        //定义处理时间
        streamExecutionEnvironment.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);

        //kafka参数配置
        Properties prop = KafkaConfigUtil.buildKafkaProps();//kafka参数配置

        //配置消费者
        FlinkKafkaConsumer<String> consumer = new FlinkKafkaConsumer<String>(topic, new SimpleStringSchema(), prop);

        //从最新处开始消费kafka数据
        consumer.setStartFromLatest();

        //Checkpoint成功后，还要向Kafka特殊的topic中写偏移量
        consumer.setCommitOffsetsOnCheckpoints(true);

        //watermark 20秒
        consumer.assignTimestampsAndWatermarks(WatermarkStrategy.forBoundedOutOfOrderness(Duration.ofSeconds(20)));

        //获取kafka数据
        DataStreamSource<String> kafkaStream= streamExecutionEnvironment.addSource(consumer);

        //数据过滤
        SingleOutputStreamOperator<PersonActivity> stream = kafkaStream
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

        stream.print();

        stream.addSink(new SinkToMySQL_JsonArray());

        streamExecutionEnvironment.execute("开始同步数据");
    }


/*

    static class CustomSchema implements KafkaDeserializationSchema<JsonObject> {
        public boolean isEndOfStream(JsonObject jsonObject) {
            return false;
        }

        public JsonObject deserialize(ConsumerRecord<byte[], byte[]> consumerRecord) throws Exception {
            // 方法1： 直接string的接收,转成json .
            String s1 = new String(consumerRecord.value());
            JsonParser parser=new JsonParser();
            JsonObject object = (JsonObject) parser.parse(s1);

            // 返回结果
            JsonArray arrays = object.get("data").getAsJsonArray();
            JsonObject arrayJson = new JsonObject();
            arrayJson.add("data", arrays);
            return arrayJson;
        }

        public TypeInformation<JsonObject> getProducedType() {
            return TypeInformation.of(JsonObject.class);         }
    }

    */

}