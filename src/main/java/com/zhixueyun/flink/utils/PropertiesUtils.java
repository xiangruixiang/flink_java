package com.zhixueyun.flink.utils;

import java.io.*;
import java.util.Properties;

public class PropertiesUtils {


    public static Properties properties(String filePath){
        Properties properties = new Properties();

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
        return properties;
    }


}
