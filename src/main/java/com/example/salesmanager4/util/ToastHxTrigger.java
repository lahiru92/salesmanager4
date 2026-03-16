package com.example.salesmanager4.util;

public class ToastHxTrigger {

    public static String create(String message, String type) {
        return "{\"showToast\":{\"message\":\"" + message + "\",\"type\":\"" + type + "\"}}";
    }
}
