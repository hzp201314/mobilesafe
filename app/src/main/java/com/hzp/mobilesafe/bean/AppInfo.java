package com.hzp.mobilesafe.bean;

import android.graphics.drawable.Drawable;

public class AppInfo {
    public String packageName;
    public String name;
    public Drawable icon;
    public long size;
    public boolean isSystem;
    public boolean isSD;

    public int uid;
    public String md5;//特征码
    public boolean isAntiVirus;//是否是病毒的标示

    public AppInfo(String packageName, String name, Drawable icon, long size,
                   boolean isSystem, boolean isSD, int uid, String md5) {
        super();
        this.packageName = packageName;
        this.name = name;
        this.icon = icon;
        this.size = size;
        this.isSystem = isSystem;
        this.isSD = isSD;
        this.uid = uid;
        this.md5 = md5;
    }
}
