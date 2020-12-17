package com.zhixueyun.flink.bus.demo;

public class StudyProgress {
    public int f_id;
    public int f_member_id;
    public int f_course_id;
    public int f_begin_time;
    public int f_study_total_time;
    public int f_study_app_total_time;
    public int f_study_pc_total_time;
    public String f_modify_date;


    public StudyProgress(int f_id, int f_member_id, int f_course_id, int f_begin_time, int f_study_total_time, int f_study_app_total_time, int f_study_pc_total_time, String f_modify_date) {
        this.f_id = f_id;
        this.f_member_id = f_member_id;
        this.f_course_id = f_course_id;
        this.f_begin_time = f_begin_time;
        this.f_study_total_time = f_study_total_time;
        this.f_study_app_total_time = f_study_app_total_time;
        this.f_study_pc_total_time = f_study_pc_total_time;
        this.f_modify_date = f_modify_date;
    }

    public StudyProgress() {
    }

    @Override
    public String toString() {
        return "StudyProgress{" +
                "f_id=" + f_id +
                ", f_member_id=" + f_member_id +
                ", f_course_id=" + f_course_id +
                ", f_begin_time=" + f_begin_time +
                ", f_study_total_time=" + f_study_total_time +
                ", f_study_app_total_time=" + f_study_app_total_time +
                ", f_study_pc_total_time=" + f_study_pc_total_time +
                ", f_modify_date='" + f_modify_date + '\'' +
                '}';
    }

    public int getF_id() {
        return f_id;
    }

    public void setF_id(int f_id) {
        this.f_id = f_id;
    }

    public int getF_member_id() {
        return f_member_id;
    }

    public void setF_member_id(int f_member_id) {
        this.f_member_id = f_member_id;
    }

    public int getF_course_id() {
        return f_course_id;
    }

    public void setF_course_id(int f_course_id) {
        this.f_course_id = f_course_id;
    }

    public int getF_begin_time() {
        return f_begin_time;
    }

    public void setF_begin_time(int f_begin_time) {
        this.f_begin_time = f_begin_time;
    }

    public int getF_study_total_time() {
        return f_study_total_time;
    }

    public void setF_study_total_time(int f_study_total_time) {
        this.f_study_total_time = f_study_total_time;
    }

    public int getF_study_app_total_time() {
        return f_study_app_total_time;
    }

    public void setF_study_app_total_time(int f_study_app_total_time) {
        this.f_study_app_total_time = f_study_app_total_time;
    }

    public int getF_study_pc_total_time() {
        return f_study_pc_total_time;
    }

    public void setF_study_pc_total_time(int f_study_pc_total_time) {
        this.f_study_pc_total_time = f_study_pc_total_time;
    }

    public String getF_modify_date() {
        return f_modify_date;
    }

    public void setF_modify_date(String f_modify_date) {
        this.f_modify_date = f_modify_date;
    }
}