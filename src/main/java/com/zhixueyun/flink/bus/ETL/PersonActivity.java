package com.zhixueyun.flink.bus.ETL;

public class PersonActivity {

    private String f_member_id;
    private String f_business_id;
    private String f_business_type;
    private String f_bueinsess_name;
    private int f_business_satus;
    private int f_start_time;
    private int f_end_time;
    private int f_status;
    private String f_company_id;
    private int f_create_time;
    private int f_modify_date;

    public PersonActivity(String f_member_id, String f_business_id, String f_business_type, String f_bueinsess_name, int f_business_satus, int f_start_time, int f_end_time, int f_status, String f_company_id, int f_create_time, int f_modify_date) {
        this.f_member_id = f_member_id;
        this.f_business_id = f_business_id;
        this.f_business_type = f_business_type;
        this.f_bueinsess_name = f_bueinsess_name;
        this.f_business_satus = f_business_satus;
        this.f_start_time = f_start_time;
        this.f_end_time = f_end_time;
        this.f_status = f_status;
        this.f_company_id = f_company_id;
        this.f_create_time = f_create_time;
        this.f_modify_date = f_modify_date;
    }


    @Override
    public String toString() {
        return "PersonActivity{" +
                "f_member_id='" + f_member_id + '\'' +
                ", f_business_id='" + f_business_id + '\'' +
                ", f_business_type='" + f_business_type + '\'' +
                ", f_bueinsess_name='" + f_bueinsess_name + '\'' +
                ", f_business_satus=" + f_business_satus +
                ", f_start_time=" + f_start_time +
                ", f_end_time=" + f_end_time +
                ", f_status=" + f_status +
                ", f_company_id='" + f_company_id + '\'' +
                ", f_create_time=" + f_create_time +
                ", f_modify_date=" + f_modify_date +
                '}';
    }

    public String getF_member_id() {
        return f_member_id;
    }

    public void setF_member_id(String f_member_id) {
        this.f_member_id = f_member_id;
    }

    public String getF_business_id() {
        return f_business_id;
    }

    public void setF_business_id(String f_business_id) {
        this.f_business_id = f_business_id;
    }

    public String getF_business_type() {
        return f_business_type;
    }

    public void setF_business_type(String f_business_type) {
        this.f_business_type = f_business_type;
    }

    public String getF_bueinsess_name() {
        return f_bueinsess_name;
    }

    public void setF_bueinsess_name(String f_bueinsess_name) {
        this.f_bueinsess_name = f_bueinsess_name;
    }

    public int getF_business_satus() {
        return f_business_satus;
    }

    public void setF_business_satus(int f_business_satus) {
        this.f_business_satus = f_business_satus;
    }

    public int getF_start_time() {
        return f_start_time;
    }

    public void setF_start_time(int f_start_time) {
        this.f_start_time = f_start_time;
    }

    public int getF_end_time() {
        return f_end_time;
    }

    public void setF_end_time(int f_end_time) {
        this.f_end_time = f_end_time;
    }

    public int getF_status() {
        return f_status;
    }

    public void setF_status(int f_activity_status) {
        this.f_status = f_activity_status;
    }

    public String getF_company_id() {
        return f_company_id;
    }

    public void setF_company_id(String f_company_id) {
        this.f_company_id = f_company_id;
    }

    public int getF_create_time() {
        return f_create_time;
    }

    public void setF_create_time(int f_create_time) {
        this.f_create_time = f_create_time;
    }

    public int getF_modify_date() {
        return f_modify_date;
    }

    public void setF_modify_date(int f_modify_date) {
        this.f_modify_date = f_modify_date;
    }
}
