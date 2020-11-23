package com.zhixueyun.flink.bus.compute;

public class Results {

    private String myname;
    private int salary;
    private String create_time;
    private int id;


    public Results(String myname, int salary, String create_time, int id) {
        this.myname = myname;
        this.salary = salary;
        this.create_time = create_time;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMyname() {
        return myname;
    }

    public void setMyname(String myname) {
        this.myname = myname;
    }

    public int getSalary() {
        return salary;
    }

    public void setSalary(int salary) {
        this.salary = salary;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    @Override
    public String toString() {
        return "Results{" +
                "myname='" + myname + '\'' +
                ", salary=" + salary +
                ", create_time='" + create_time + '\'' +
                ", id=" + id +
                '}';
    }
}
