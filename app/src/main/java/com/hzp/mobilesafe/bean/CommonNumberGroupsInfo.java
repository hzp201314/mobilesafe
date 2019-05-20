package com.hzp.mobilesafe.bean;

import java.util.List;

public class CommonNumberGroupsInfo {
    public String name;
    public String idx;

    public List<CommonNumberChildInfo> child;//孩子的数据

    public CommonNumberGroupsInfo(String name, String idx,List<CommonNumberChildInfo> child) {
        super();
        this.name = name;
        this.idx = idx;
        this.child = child;
    }
}
