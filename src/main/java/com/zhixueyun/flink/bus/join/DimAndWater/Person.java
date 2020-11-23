package com.zhixueyun.flink.bus.join.DimAndWater;

/**
 * @author huangqingshi
 * @Date 2019-12-07
 */
public class Person {

    private String name;
    private int id;
    private int areaid;
    private int createtime;

    public int getCreatetime() {
        return createtime;
    }

    public void setCreatetime(int createtime) {
        this.createtime = createtime;
    }


    public Person(String name, int id, int areaid) {
        this.name = name;
        this.id = id;
        this.areaid = areaid;
    }

    public Person() {
    }

    public Person(String name, int id, int areaid, int createtime) {
        this.name = name;
        this.id = id;
        this.areaid = areaid;
        this.createtime = createtime;
    }

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", id=" + id +
                ", areaid=" + areaid +
                ", createtime=" + createtime +
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

    public int getAreaid() {
        return areaid;
    }

    public void setAreaid(int areaid) {
        this.areaid = areaid;
    }
}