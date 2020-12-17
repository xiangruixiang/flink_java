package com.zhixueyun;

import scala.util.parsing.json.JSONObject;

import java.io.*;
import java.util.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.ibm.icu.impl.ClassLoaderUtil.getClassLoader;

public class Data {

    public static void main(String[] args) {

        Properties properties = new Properties();
        String filePath = args[0];

        // 使用InPutStream流读取properties文件
        BufferedReader bufferedReader ;
        try {
            bufferedReader = new BufferedReader(new FileReader(filePath));
            properties.load(bufferedReader);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 获取key对应的value值
        System.out.println(properties.getProperty("aa"));


       // System.out.println(PropertiesCfg.get("aa"));

       /*
        try {
            //指定路径中的文件
            display("/usr/local/java.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
        */

        //创建Properties对象


    }


    public static class PropertiesCfg {

        //配置文件所在目录路径，相对项目根目录，如果是放在根目录下，直接写文件名称就行
        private final static String file = "aa.properties";
        private final static Properties properties = new Properties();

        static {
            try {
                properties.load(new InputStreamReader(ClassLoader.getSystemResourceAsStream(file), "utf-8"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //根据key获取值
        public static String get(String key) {
            return properties.getProperty(key).trim();
        }

        //根据key获取值，值为空则返回defaultValue
        public static String get(String key, String defaultValue) {
            return properties.getProperty(key, defaultValue);
        }
    }




    public static void display(String path)throws Exception{
            File file=new File(path);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = null;
            //定义一个map集合保存单词和单词出现的个数
            TreeMap<String,Integer> tm = new TreeMap<String,Integer>();
            //读取文件
            while((line=br.readLine())!=null){
                line = line.toLowerCase();
                String reg1 = "\\s+";
                String reg2 ="\\w+";
                //将读取的文本进行分割
                String str[] = line.split(reg1);
                for(String s: str){
                    if(s.matches(reg2)){
                        //判断集合中是否已经存在该单词，如果存在则个数加1，否则将单词添加到集合中，且个数置为1
                        if(!tm.containsKey(s)){
                            tm.put(s,1);
                        }else{
                            tm.put(s,tm.get(s)+1);
                        }
                    }
                }
            }
            br.close();
            System.out.println(tm);
            printResult(tm);
        }

    public static void printResult(Map<String,Integer> map) {
        List<Map.Entry<String,Integer>> list = new ArrayList<Map.Entry<String,Integer>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return (o2.getValue().compareTo(o1.getValue()) ); //降序排序,当o2小于、等于、大于o1时，返回-1,0,1
            }
        });

        for (int i = 0; i < 10; i++) {
            Map.Entry<String,Integer> entry = list.get(i);
            if (entry == null) {
                return;
            }
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
    }
}



