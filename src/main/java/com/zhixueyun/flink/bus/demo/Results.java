package com.zhixueyun.flink.bus.demo;

public class Results {

    private String createTime ;
    private int memberId;
    private int studyTotalTime;


    public Results(String createTime, int memberId, int studyTotalTime) {
        this.createTime = createTime;
        this.memberId = memberId;
        this.studyTotalTime = studyTotalTime;
    }

    @Override
    public String toString() {
        return "Results{" +
                "createTime='" + createTime + '\'' +
                ", memberId=" + memberId +
                ", studyTotalTime=" + studyTotalTime +
                '}';
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public int getStudyTotalTime() {
        return studyTotalTime;
    }

    public void setStudyTotalTime(int studyTotalTime) {
        this.studyTotalTime = studyTotalTime;
    }
}
