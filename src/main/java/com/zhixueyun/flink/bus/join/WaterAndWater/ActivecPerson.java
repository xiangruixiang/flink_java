package com.zhixueyun.flink.bus.join.WaterAndWater;

public class ActivecPerson {


    private int id;
    private String name;
    private int areaid;
    private long createtime;
    private String f_company_id;
    private long f_modify_date;

    public ActivecPerson() {
    }

    public ActivecPerson(int id, String name, int areaid, long createtime, String f_company_id, long f_modify_date) {
        this.id = id;
        this.name = name;
        this.areaid = areaid;
        this.createtime = createtime;
        this.f_company_id = f_company_id;
        this.f_modify_date = f_modify_date;
    }

    @Override
    public String toString() {
        return "ActivecPerson{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", areaid=" + areaid +
                ", createtime=" + createtime +
                ", f_company_id='" + f_company_id + '\'' +
                ", f_modify_date='" + f_modify_date + '\'' +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAreaid() {
        return areaid;
    }

    public void setAreaid(int areaid) {
        this.areaid = areaid;
    }

    public long getCreatetime() {
        return createtime;
    }

    public void setCreatetime(long createtime) {
        this.createtime = createtime;
    }

    public String getF_company_id() {
        return f_company_id;
    }

    public void setF_company_id(String f_company_id) {
        this.f_company_id = f_company_id;
    }

    public long getF_modify_date() {
        return f_modify_date;
    }

    public void setF_modify_date(long f_modify_date) {
        this.f_modify_date = f_modify_date;
    }
}
