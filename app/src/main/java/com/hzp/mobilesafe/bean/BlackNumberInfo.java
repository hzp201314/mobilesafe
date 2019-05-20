package com.hzp.mobilesafe.bean;

/**
 * created by hzp on 2019/5/20 15:08
 * 作者：codehan
 * 描述：
 */
public class BlackNumberInfo {

    public String blacknumber;
    public int mode;

    public BlackNumberInfo(String blacknumber, int mode) {
        super();
        this.blacknumber = blacknumber;
        this.mode = mode;
    }

    @Override
    public String toString() {
        return "BlackNumberInfo [blacknumber=" + blacknumber + ", mode=" + mode
                + "]";
    }
}
