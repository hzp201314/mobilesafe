package com.hzp.mobilesafe.utils;

import java.io.UnsupportedEncodingException;

public class TextUtil {
    /**
     * 强转utf-8
     * @param str
     * @return
     */
    public static String toUtf8(String str) {
        String result = null;
        try {
            result = new String(str.getBytes("UTF-8"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }
}
