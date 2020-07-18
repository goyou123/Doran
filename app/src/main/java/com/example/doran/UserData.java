package com.example.doran;



public class UserData {

    String MyEmail;
    String MyName;
    String MyPhonenum;

    String ProfileImage;
    String Other;
    String MyUid;
    String CoupleKey;
    String fcmToken;

    int ask;

    // 사귄날 변수 추가?


    public UserData() {
    }

    public String getMyEmail() {
        return MyEmail;
    }

    public void setMyEmail(String myEmail) {
        MyEmail = myEmail;
    }

    public String getMyName() {
        return MyName;
    }

    public void setMyName(String myName) {
        MyName = myName;
    }

    public String getMyPhonenum() {
        return MyPhonenum;
    }

    public void setMyPhonenum(String myPhonenum) {
        MyPhonenum = myPhonenum;
    }

    public String getProfileImage() {
        return ProfileImage;
    }

    public void setProfileImage(String profileImage) {
        ProfileImage = profileImage;
    }

    public String getOther() {
        return Other;
    }

    public void setOther(String other) {
        Other = other;
    }

    public String getMyUid() {
        return MyUid;
    }

    public void setMyUid(String myUid) {
        MyUid = myUid;
    }

    public int getAsk() {
        return ask;
    }

    public void setAsk(int ask) {
        this.ask = ask;
    }

    public String getCoupleKey() {
        return CoupleKey;
    }

    public void setCoupleKey(String coupleKey) {
        CoupleKey = coupleKey;
    }
    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

}
