package com.shrek.klib.logger;

import android.content.Context;

import com.shrek.klib.bo.Constructor;
import com.shrek.klib.bo.Lazy;
import com.shrek.klib.file.FileOperator;
import com.shrek.klib.logger.impl.ConsoleLogger;
import com.shrek.klib.logger.impl.PrintLogger;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * 日志
 * @author shrek
 */
public class ZLog {
    private static final int CHUNK_SIZE = 200;

    private static List<Lazy<ILogger>> engines;

    private static LogLevel[] logLevels;

    private static String defaultTAG;

    private ZLog() {
        super();
        throw new AssertionError();
    }


    public static void init(final Context ctx, String defaultTAG, final String printPath, LogLevel... levels) {
        engines = new ArrayList<Lazy<ILogger>>();
        engines.add(new Lazy<ILogger>(new Constructor<ILogger>() {
            @Override
            public ILogger instance() {
                return new ConsoleLogger();
            }
        }));

        engines.add(new Lazy<ILogger>(new Constructor<ILogger>() {
            @Override
            public ILogger instance() {

                FileOperator.OptCallBack callBack = new FileOperator.OptCallBack() {

                    @Override
                    public void createFileError(Exception ex) {
                        ex.printStackTrace();
                    }

                    @Override
                    public void writeError(Exception ex) {
                        ex.printStackTrace();
                    }

                    @Override
                    public void readError(Exception ex) {
                        ex.printStackTrace();
                    }
                };
                return new PrintLogger(new FileOperator(ctx, printPath, callBack));
            }
        }));

        ZLog.defaultTAG = defaultTAG;
        if (levels == null) {
            logLevels = new LogLevel[0];
        } else {
            logLevels = levels;
        }

    }

    /**
     * debug
     *
     * @param message
     */
    public static void d(String message) {
        printLog(LogLevel.DEBUG, defaultTAG, message, true, false);
    }

    public static void d2P(String message) {
        printLog(LogLevel.DEBUG, defaultTAG, message, false, true);
    }

    public static void dAndP(String message) {
        printLog(LogLevel.DEBUG, defaultTAG, message, true, true);
    }


    public static void d(String tag, String message) {
        printLog(LogLevel.DEBUG, tag, message, true, false);
    }

    public static void d2P(String tag, String message) {
        printLog(LogLevel.DEBUG, tag, message, false, true);
    }

    public static void dAndP(String tag, String message) {
        printLog(LogLevel.DEBUG, tag, message, true, true);
    }


    public static void d(Object obj, String message) {
        printLog(LogLevel.DEBUG, obj.getClass().getName(), message, true, false);
    }

    public static void d2P(Object obj, String message) {
        printLog(LogLevel.DEBUG, obj.getClass().getName(), message, false, true);
    }

    public static void dAndP(Object obj, String message) {
        printLog(LogLevel.DEBUG, obj.getClass().getName(), message, true, true);
    }

    /**
     * info
     *
     * @param message
     */
    public static void i(String message) {
        printLog(LogLevel.INFO, defaultTAG, message, true, false);
    }

    public static void i2P(String message) {
        printLog(LogLevel.INFO, defaultTAG, message, false, true);
    }

    public static void iAndP(String message) {
        printLog(LogLevel.INFO, defaultTAG, message, true, true);
    }


    public static void i(String tag, String message) {
        printLog(LogLevel.INFO, tag, message, true, false);
    }

    public static void i2P(String tag, String message) {
        printLog(LogLevel.INFO, tag, message, false, true);
    }

    public static void iAndP(String tag, String message) {
        printLog(LogLevel.INFO, tag, message, true, true);
    }


    public static void i(Object obj, String message) {
        printLog(LogLevel.INFO, obj.getClass().getName(), message, true, false);
    }

    public static void i2P(Object obj, String message) {
        printLog(LogLevel.INFO, obj.getClass().getName(), message, false, true);
    }

    public static void iAndP(Object obj, String message) {
        printLog(LogLevel.INFO, obj.getClass().getName(), message, true, true);
    }

    /**
     * Warning
     *
     * @param message
     */
    public static void w(String message) {
        printLog(LogLevel.WARNING, defaultTAG, message, true, false);
    }

    public static void w2P(String message) {
        printLog(LogLevel.WARNING, defaultTAG, message, false, true);
    }

    public static void wAndP(String message) {
        printLog(LogLevel.WARNING, defaultTAG, message, true, true);
    }

    public static void w(String tag, String message) {
        printLog(LogLevel.WARNING, tag, message, true, false);
    }

    public static void w2P(String tag, String message) {
        printLog(LogLevel.WARNING, tag, message, false, true);
    }

    public static void wAndP(String tag, String message) {
        printLog(LogLevel.WARNING, tag, message, true, true);
    }

    public static void w(Object obj, String message) {
        printLog(LogLevel.WARNING, obj.getClass().getName(), message, true, false);
    }

    public static void w2P(Object obj, String message) {
        printLog(LogLevel.WARNING, obj.getClass().getName(), message, false, true);
    }

    public static void wAndP(Object obj, String message) {
        printLog(LogLevel.WARNING, obj.getClass().getName(), message, true, true);
    }


    /**
     * error
     *
     * @param message
     */
    public static void e(String message) {
        printLog(LogLevel.ERROR, defaultTAG, message, true, false);
    }

    public static void e2P(String message) {
        printLog(LogLevel.ERROR, defaultTAG, message, false, true);
    }

    public static void eAndP(String message) {
        printLog(LogLevel.ERROR, defaultTAG, message, true, true);
    }

    public static void e(String tag, String message) {
        printLog(LogLevel.ERROR, tag, message, true, false);
    }

    public static void e2P(String tag, String message) {
        printLog(LogLevel.ERROR, tag, message, false, true);
    }

    public static void eAndP(String tag, String message) {
        printLog(LogLevel.ERROR, tag, message, true, true);
    }

    public static void e(Object obj, String message) {
        printLog(LogLevel.ERROR, obj.getClass().getName(), message, true, false);
    }

    public static void e2P(Object obj, String message) {
        printLog(LogLevel.ERROR, obj.getClass().getName(), message, false, true);
    }

    public static void eAndP(Object obj, String message) {
        printLog(LogLevel.ERROR, obj.getClass().getName(), message, true, true);
    }


    /**
     * 打印log
     *
     * @param logLevel
     * @param tag
     * @param message
     * @param isConsole
     * @param isPrint
     */
    private static void printLog(LogLevel logLevel, String tag, String message, boolean isConsole, boolean isPrint) {
        if (engines == null) {
            throw new ExceptionInInitializerError("Log engine must initialize");
        }

        boolean isExist = false;
        for (LogLevel level : logLevels) {
            if (isExist = (level == logLevel)) {
                break;
            }
        }

        if (!isExist) return;

        if (tag == null) tag = defaultTAG;

        if (isConsole) {

            if (message.length() <= CHUNK_SIZE) {
                engines.get(0).get().printlog(logLevel, tag, "# " + message);
            } else {
                for (int i = 0; i < message.length(); i += CHUNK_SIZE) {
                    int count = Math.min(message.length() - i, CHUNK_SIZE);
                    engines.get(0).get().printlog(logLevel, tag, "# " + new String(message.toCharArray(), i, count));
                }
            }
        }

        if (isPrint) {
            engines.get(1).get().printlog(logLevel, tag, message);
        }

    }

    public static void setLogLevels(LogLevel... logLevels) {
        ZLog.logLevels = logLevels;
    }

    /**
     * 得到历史纪录<主线程 >
     * @param readDoing
     */
    public static void readHistoryBos(Action1<List<LoggerBo>> readDoing){
        Observable.just(engines.get(1).get().getHistoryLogs())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(readDoing);
    }

}
