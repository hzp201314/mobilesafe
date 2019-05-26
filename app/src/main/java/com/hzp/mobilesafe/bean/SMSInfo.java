package com.hzp.mobilesafe.bean;

/**
 * created by hzp on 2019/5/25 09:35
 * 作者：codehan
 * 描述：
 */
public class SMSInfo {
    public String address;
    public String date;
    public String type;
    public String body;

    public SMSInfo(String address, String date, String type, String body) {
        super();
        this.address = address;
        this.date = date;
        this.type = type;
        this.body = body;
    };
}
