package com.zhixueyun.flink.bus.join.DimAndWater;

/**
 * @author huangqingshi
 * @Date 2019-12-07
 */
public class PersonInfo {

    private int age;
    private int id;

    public PersonInfo() {}

    public PersonInfo(int id,int age) {
        this.age = age;
        this.id = id;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "PersonInfo{" +
                "age=" + age +
                ", id=" + id +
                '}';
    }
}