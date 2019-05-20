package com.hzp.mobilesafe.bean;

import android.graphics.drawable.Drawable;

public class AppInfo {
    public String packageName;
    public String name;
    public Drawable icon;
    public long size;
    public boolean isSystem;
    public boolean isSD;

    public AppInfo(String packageName, String name, Drawable icon, long size,
                   boolean isSystem, boolean isSD) {
        super();
        this.packageName = packageName;
        this.name = name;
        this.icon = icon;
        this.size = size;
        this.isSystem = isSystem;
        this.isSD = isSD;
    }
}
