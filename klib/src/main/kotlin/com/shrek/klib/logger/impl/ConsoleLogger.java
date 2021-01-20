package com.shrek.klib.logger.impl;

import android.util.Log;

import com.shrek.klib.logger.ILogger;
import com.shrek.klib.logger.LogLevel;
import com.shrek.klib.logger.LoggerBo;

import java.util.ArrayList;
import java.util.List;

/**
 * 控制台输出
 * @author shrek
 * @date: 2016-04-01
 */
public class ConsoleLogger implements ILogger{

    @Override
    public void d(String tag, String message) {
        printlog(LogLevel.DEBUG, tag, message);
    }

    @Override
    public void i(String tag, String message) {
        printlog(LogLevel.INFO, tag, message);
    }

    @Override
    public void w(String tag, String message) {
        printlog(LogLevel.WARNING, tag, message);
    }

    @Override
    public void e(String tag, String message) {
        printlog(LogLevel.ERROR, tag, message);
    }

    @Override
    public void d(Object obj, String message) {
        d(obj.getClass().getName(),message);
    }

    @Override
    public void i(Object obj, String message) {
        i(obj.getClass().getName(), message);
    }

    @Override
    public void w(Object obj, String message) {
        w(obj.getClass().getName(), message);
    }

    @Override
    public void e(Object obj, String message) {
        e(obj.getClass().getName(), message);
    }

    @Override
    public void printlog(LogLevel mLogLevel, String tag, String msg) {
        switch (mLogLevel) {
            case DEBUG:
                Log.d(tag, msg);
                break;
            case WARNING:
                Log.w(tag, msg);
                break;
            case ERROR:
                Log.e(tag, msg);
                break;
            case INFO:
                Log.i(tag, msg);
                break;
            default:

                break;
        }
    }

    @Override
    public List<LoggerBo> getHistoryLogs() {
        return new ArrayList<LoggerBo>();
    }
}
