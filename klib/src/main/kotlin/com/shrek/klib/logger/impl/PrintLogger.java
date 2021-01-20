package com.shrek.klib.logger.impl;

import com.shrek.klib.file.FileOperator;
import com.shrek.klib.logger.ILogger;
import com.shrek.klib.logger.LogLevel;
import com.shrek.klib.logger.LoggerBo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * @author shrek
 * @date: 2016-04-01
 */
public class PrintLogger implements ILogger {

    private SimpleDateFormat timesTemp;

    private final String GAP = "###";

    FileOperator operator;

    public PrintLogger(FileOperator operator) {
        super();
        this.operator = operator;

        timesTemp = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss] ");
    }

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
        d(obj.getClass().getName(), message);
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
    public void printlog(LogLevel mLogLevel, String tag, String message) {
        String printMessage = "";
        switch (mLogLevel) {
            case DEBUG:
                printMessage = getFormatMessage("[D]", tag, message);
                break;
            case WARNING:
                printMessage = getFormatMessage("[W]", tag, message);
                break;
            case ERROR:
                printMessage = getFormatMessage("[E]", tag, message);
                break;
            case INFO:
                printMessage = getFormatMessage("[I]", tag, message);
                break;
            default:

                break;
        }

        Observable.just(printMessage)
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<String>() {

                    @Override
                    public void call(String msg) {
//                        operator.writeMsg(timesTemp.format(new Date()));
                        operator.writeMsg(msg);
                        operator.stopWrite();
                    }

                });


        Observable.just("").delay(3, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<String>() {

                    @Override
                    public void call(String msg) {
                        operator.stopWrite();
                    }
                });
    }


    private String getFormatMessage(String level, String tag, String message) {
        String formatMessage = "%s %s %s %s %s %s %s";
        return String.format(formatMessage, timesTemp.format(new Date()),GAP, level, GAP, tag, GAP, message);
    }


    @Override
    public List<LoggerBo> getHistoryLogs() {
        final List<LoggerBo> bos = new ArrayList<LoggerBo>();

        operator.readMsg(new FileOperator.ReadCallback() {
            @Override
            public void readLine(String line) {
                String[] array = line.split(GAP);
                try {
                    Date logTime = timesTemp.parse(array[0]);
                    LoggerBo bo = new LoggerBo(logTime, array[1], array[2],
                            array[3]);
                    bos.add(0, bo);
                } catch (Exception e) {

                }

            }
        });

        return bos;
    }
}
