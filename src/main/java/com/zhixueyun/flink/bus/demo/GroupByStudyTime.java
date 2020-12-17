package com.zhixueyun.flink.bus.demo;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zhixueyun.flink.utils.KafkaConfigUtil;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.runtime.state.filesystem.FsStateBackend;
import org.apache.flink.shaded.guava18.com.google.common.collect.Lists;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.util.Collector;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Properties;


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
public class GroupByStudyTime {

    static String topic = "demo";

    public static void main(String[] args) throws Exception{

        //构建流执行环境
        StreamExecutionEnvironment streamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
       // EnvironmentSettings environmentSettings = EnvironmentSettings.newInstance().useBlinkPlanner().inStreamingMode().build();
       // StreamTableEnvironment streamTableEnvironment = StreamTableEnvironment.create(streamExecutionEnvironment, environmentSettings);

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
        Properties prop = KafkaConfigUtil.buildKafkaProps(args[0]);//kafka参数配置

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
        SingleOutputStreamOperator<StudyProgress> stream = kafkaStream
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
        }).map(new MapFunction<String, StudyProgress>() {
            @Override
            public StudyProgress map(String string) throws Exception {
                Gson gson = new Gson();
                return gson.fromJson(string, StudyProgress.class);
            }
        });



        //多个key分组排序
        SingleOutputStreamOperator<Results> groupResult = stream.keyBy(
                new KeySelector<StudyProgress, Tuple2<String, Integer>>() {
                    @Override
                    public Tuple2<String, Integer> getKey(StudyProgress value) throws Exception {
                        return Tuple2.of( String.valueOf(value.getF_modify_date().substring(0,10)), value.getF_member_id());
                    }
                })
                .window(TumblingProcessingTimeWindows.of(Time.seconds(30)))
                .apply(new WindowFunction<StudyProgress, Results, Tuple2<String,Integer>, TimeWindow>() {
                    @Override
                    public void apply(Tuple2<String,Integer> key, TimeWindow timeWindow, Iterable<StudyProgress> iterable, Collector<Results> collector) throws Exception {
                        ArrayList<StudyProgress> StudyProgresses = Lists.newArrayList(iterable);
                        if (StudyProgresses.size() > 0) {
                            System.out.println("30秒的数据条数是：" +key +":"+ StudyProgresses.size());
                            int sum =0;

                            for(int i = 0; i< StudyProgresses.size(); i++){
                                sum += StudyProgresses.get(i).f_study_total_time;
                            }
                            JsonObject returnJson = new JsonObject();


                            returnJson.addProperty("createTime", key.getField(0).toString());
                            returnJson.addProperty("memberId", key.getField(1).toString());
                            returnJson.addProperty("studyTotalTime", sum);
                            //returnJson.addProperty("create_time", String.valueOf(LocalDateTime.now()));
                            Gson gson = new Gson();
                            collector.collect(gson.fromJson(returnJson, Results.class));
                        }
                    }
                });
        groupResult.addSink(new SinkToMySQLDemo());

        streamExecutionEnvironment.execute("kafka 消费任务开始");
    }
}

