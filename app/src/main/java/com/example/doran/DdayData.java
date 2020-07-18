package com.example.doran;

public class DdayData {
    private String Ddaydata_DateName; //100일, 200일, 등 기념일 이름
    private String Ddaydata_Date; // 그 기념일의 날짜
    private String Ddaydata_Dday; // 그 기념일까지 몇일 남았는지
    private String Ddaydata_Firstday; // 현재우리 사귄날짜
    private String Ourdate;// 현재우리 사귄일수

    public DdayData() {
    }

    public String getDdaydata_DateName() {
        return Ddaydata_DateName;
    }

    public void setDdaydata_DateName(String ddaydata_DateName) {
        Ddaydata_DateName = ddaydata_DateName;
    }

    public String getDdaydata_Date() {
        return Ddaydata_Date;
    }

    public void setDdaydata_Date(String ddaydata_Date) {
        Ddaydata_Date = ddaydata_Date;
    }

    public String getDdaydata_Dday() {
        return Ddaydata_Dday;
    }

    public void setDdaydata_Dday(String ddaydata_Dday) {
        Ddaydata_Dday = ddaydata_Dday;
    }

    public String getDdaydata_Firstday() {
        return Ddaydata_Firstday;
    }

    public void setDdaydata_Firstday(String ddaydata_Firstday) {
        Ddaydata_Firstday = ddaydata_Firstday;
    }

    public String getOurdate() {
        return Ourdate;
    }

    public void setOurdate(String ourdate) {
        Ourdate = ourdate;
    }
}
