package com.xujinshan.rabbitmqutils;

/**
 * @Author: xujinshan361@163.com
 * 睡眠工具类
 */
public class SleepUtils {
    /**
     * 睡眠多少秒
     * @param second
     */
    public static void sleep(int second){
        try {
            Thread.sleep(1000*second);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
