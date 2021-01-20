package com.shrek.klib;

import android.content.Context;

import com.shrek.klib.colligate.StringUtils;
import com.shrek.klib.logger.LogLevel;
import com.shrek.klib.ormlite.bo.ZDb;

import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Interceptor;

/**
 * 全局的设置
 *
 * @author shrek
 * @date: 2016-04-01
 */
public class ZSetting {

    private Context context;

    private boolean isDebugMode;

    private boolean isPrintLog;

    private String defaultTAG;

    private String logPrintPath;

    //打印log的有效时间 sec
    private long printLogAvaidTime;

    private LogLevel[] levels;

    //是否开启Strict模式
    private boolean isOpenStrictMode;

    //是否捕捉异常
    private boolean isCaptureError;

    private String restCachePath;

    private String restBaseUrl;

    private Interceptor[] customInterceptors;

    private String gsonTimeFormat;

    private String dbName;

    //数据库操作的有效时间
    private int dbVersion,dbOPeratorAvailTime;
    //数据库初始化的类
    private Class<? extends ZDb>[] dbInitClasses;

    private String cachePath;

    private String preferencesName;

    private ZSetting() {
        super();
//        throw new AssertionError("No initialize");
    }

    public boolean isCaptureError() {
        return isCaptureError;
    }

    public boolean isOpenStrictMode() {
        return isOpenStrictMode;
    }

    public boolean isDebugMode() {
        return isDebugMode;
    }

    public Context getContext() {
        return context;
    }

    public String getDefaultTAG() {
        return defaultTAG;
    }

    public String getLogPrintPath() {
        return logPrintPath;
    }

    public LogLevel[] getLevels() {
        return levels;
    }

    public String getRestCachePath() {
        return restCachePath;
    }


    public String getGsonTimeFormat() {
        return gsonTimeFormat;
    }

    public String getRestBaseUrl() {
        return restBaseUrl;
    }

    public int getDbOPeratorAvailTime() {
        return dbOPeratorAvailTime;
    }

    public int getDbVersion() {
        return dbVersion;
    }

    public String getDbName() {
        return dbName;
    }

    public Class<? extends ZDb>[] getDbInitClasses() {
        return dbInitClasses;
    }

    public long getPrintLogAvaidTime() {
        return printLogAvaidTime;
    }

    public Interceptor[] getCustomInterceptors() {
        return customInterceptors;
    }

    public String getCachePath() {
        return cachePath;
    }

    public String getPreferencesName() {
        return preferencesName;
    }

    
    /**
     * 构造器
     */
    public static class Builder {
        private Context context;

        private boolean isPrintLog;

        private String defaultTAG;

        private String logPrintPath;

        private LogLevel[] levels;

        private long printLogAvaidTime;

        private boolean isOpenStrictMode;

        private boolean isCaptureError;

        private boolean isDebugMode;

        private String restCachePath;

        private String restBasrUrl;

        private Interceptor[] customInterceptors;

        private String gsonTimeFormat;

        private String dbName;

        private int dbVersion,dbOPeratorAvailTime;

        private Class<? extends ZDb>[] dbInitClasses;
        
        private String cachePath;

        private String preferencesName;

        public Builder(Context context) {
            super();
            Builder.this.context = context;
            Builder.this.isPrintLog = true;
            Builder.this.defaultTAG = "ZW_COMMON";

            String pixfString = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            logPrintPath = "/ZW_COMMON/log"+ pixfString +".txt";
            levels = new LogLevel[]{LogLevel.DEBUG, LogLevel.ERROR, LogLevel.WARNING, LogLevel.INFO};

            printLogAvaidTime = 60 * 60 * 24 * 7;

            Builder.this.isOpenStrictMode = true;
            Builder.this.isCaptureError = false;

            Builder.this.isDebugMode = true;

            restBasrUrl = "http://www.baidu.com";

            restCachePath = "/ZW_COMMON/RestCache/";

            gsonTimeFormat="yyyy-MM-dd";

            dbName = "Master";

            cachePath = "/ZW_COMMON/dataCache/";

            dbVersion = 1;
            dbOPeratorAvailTime = 5;

            preferencesName = "ZW_COMMON";
        }

        public ZSetting.Builder printLogFlag(boolean isOpen) {
            Builder.this.isPrintLog = isOpen;
            return ZSetting.Builder.this;
        }

        public ZSetting.Builder initLogParas(String defaultTAG, String logPrintPath, long printLogAvaidTime, LogLevel... levels) {
            if (!StringUtils.isEmpty(defaultTAG)) {
                Builder.this.defaultTAG = defaultTAG;
            }

            if (!StringUtils.isEmpty(logPrintPath)) {
                Builder.this.logPrintPath = logPrintPath;
            }

            if (printLogAvaidTime <= 0) {
                Builder.this.printLogAvaidTime = printLogAvaidTime;
            }

            if (levels != null) {
                Builder.this.levels = levels;
            }

            return ZSetting.Builder.this;
        }

        public ZSetting.Builder setIsDebugMode(boolean isDebugMode) {
            this.isDebugMode = isDebugMode;
            return ZSetting.Builder.this;
        }

        public ZSetting.Builder setIsOpenStrictMode(boolean isOpenStrictMode) {
            Builder.this.isOpenStrictMode = isOpenStrictMode;
            return ZSetting.Builder.this;
        }

        public ZSetting.Builder setIsCaptureError(boolean isCaptureError) {
            this.isCaptureError = isCaptureError;
            return ZSetting.Builder.this;
        }

        public ZSetting.Builder setRestCachePath(String restCachePath) {
            this.restCachePath = restCachePath;
            return ZSetting.Builder.this;
        }

        public ZSetting.Builder setRestBasrUrl(String restBasrUrl) {
            if (!restBasrUrl.startsWith("http://")) {
                restBasrUrl = "http://" + restBasrUrl;
            }

            if (!restBasrUrl.endsWith("/")) {
                restBasrUrl += "/";
            }
            this.restBasrUrl = restBasrUrl;
            return ZSetting.Builder.this;
        }

        public ZSetting.Builder setGsonTimeFormat(String gsonTimeFormat) {
            this.gsonTimeFormat = gsonTimeFormat;
            return ZSetting.Builder.this;
        }

        public ZSetting.Builder setDbName(String dbName) {
            this.dbName = dbName;
            return ZSetting.Builder.this;
        }

        public ZSetting.Builder setDbVersion(int dbVersion) {
            this.dbVersion = dbVersion;
            return ZSetting.Builder.this;
        }

        public ZSetting.Builder setDbOPeratorAvailTime(int dbOPeratorAvailTime) {
            this.dbOPeratorAvailTime = dbOPeratorAvailTime;
            return ZSetting.Builder.this;
        }

        public ZSetting.Builder initDBClasses(Class<? extends ZDb>... clazzs ){
            this.dbInitClasses = clazzs;
            return ZSetting.Builder.this;
        }
        
        
        public ZSetting.Builder addCustomInterceptors(Interceptor... addInterceptors) {
            this.customInterceptors = addInterceptors;
            return ZSetting.Builder.this;
        }

        public ZSetting.Builder setCachePath(String cachePath) {
            this.cachePath = cachePath;
            return ZSetting.Builder.this;
        }

        public ZSetting.Builder setPreferencesName(String preferencesName) {
            this.preferencesName = preferencesName;
            return ZSetting.Builder.this;
        }

        public ZSetting builder() {
            ZSetting setting = new ZSetting();
            setting.context = Builder.this.context;
            setting.isPrintLog = Builder.this.isPrintLog;
            setting.defaultTAG = Builder.this.defaultTAG;
            setting.logPrintPath = Builder.this.logPrintPath;
            setting.levels = Builder.this.levels;
            setting.printLogAvaidTime = Builder.this.printLogAvaidTime;
            setting.isOpenStrictMode = Builder.this.isOpenStrictMode;

            setting.isOpenStrictMode = Builder.this.isOpenStrictMode;
            setting.isCaptureError = Builder.this.isCaptureError;

            setting.isDebugMode = Builder.this.isDebugMode;
            setting.restCachePath = Builder.this.restCachePath;
            setting.restBaseUrl = Builder.this.restBasrUrl;
            setting.gsonTimeFormat = Builder.this.gsonTimeFormat;

            setting.dbName = Builder.this.dbName;
            setting.dbVersion = Builder.this.dbVersion;
            setting.dbOPeratorAvailTime = Builder.this.dbOPeratorAvailTime;
            setting.dbInitClasses = Builder.this.dbInitClasses;
            
            setting.customInterceptors = Builder.this.customInterceptors;
            setting.cachePath =  Builder.this.cachePath;
            setting.preferencesName =  Builder.this.preferencesName;
            return setting;
        }

    }
}
