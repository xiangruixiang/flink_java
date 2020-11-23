package com.zhixueyun.flink.bus.join.DimAndWater;

/**
 * @author huangqingshi
 * @Date 2019-12-07
 */
public class Employ {

    private String name;
    private int id;
    private int age;
    private String area;

    public Employ(String name, int id, int age, String area) {
        this.name = name;
        this.id = id;
        this.age = age;
        this.area = area;
    }

    public Employ() {
    }

    @Override
    public String toString() {
        return "Employ{" +
                "name='" + name + '\'' +
                ", id=" + id +
                ", age=" + age +
                ", area='" + area + '\'' +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }
}