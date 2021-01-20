package com.shrek.klib

import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.DisplayMetrics
import androidx.multidex.MultiDexApplication
import com.shrek.klib.colligate.AndroidVersionCheckUtils
import com.shrek.klib.exception.ZAppException
import com.shrek.klib.logger.ZLog
import com.shrek.klib.net.ZNetChangeObserver
import com.shrek.klib.net.ZNetWorkUtil
import com.shrek.klib.net.ZNetworkStateReceiver
import com.shrek.klib.ormlite.DBOpt
import com.shrek.klib.ormlite.ZDBHelper
import java.io.PrintStream

/**
 * @author shrek
 * @date:  2016-05-27
 */
open class KApp : MultiDexApplication(){

    val uncaughtExceptionHandler: Thread.UncaughtExceptionHandler by lazy {
        ZAppException.getInstance(this, defSetting.isDebugMode)
    }

    lateinit var defSetting: ZSetting
        private set

    //数据库操作类 未初始化
    var dbOpt:ZDBHelper? = null

    val provideDisplay : DisplayMetrics by lazy{
        resources.displayMetrics
    }

    val actManager : ZActivityManager by lazy{
         ZActivityManager()
    }

    val observer : ZNetChangeObserver by lazy {
        object: ZNetChangeObserver {

            override fun onConnect(type: ZNetWorkUtil.NetType) {
                val currActivity = actManager.current
                if (currActivity is ZNetChangeObserver) {
                    currActivity.onConnect(type)
                }

            }

            override fun  onDisConnect() {
                val currActivity = actManager.current
                if (currActivity is ZNetChangeObserver) {
                    currActivity.onDisConnect()
                }
            }
        }
    }

    protected var zwNetChangeObserver: ZNetChangeObserver? = null

    companion object {
        lateinit var app:KApp
    }

    override fun onCreate() {
        super.onCreate()
        KApp.app = this

        val settingBuilder = ZSetting.Builder(this)
        initSetting(settingBuilder)
        defSetting = settingBuilder.builder()
        
        //初始化数据库
        if(defSetting.dbInitClasses != null && defSetting.dbInitClasses.size > 0) {
            dbOpt = DBOpt(this,defSetting,defSetting.dbInitClasses)
        }
        
        if (defSetting.isCaptureError) {
            // 注册App异常崩溃处理器
            Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler)
        }

        /** ------------------ debug mode ----------------------- */
        if (defSetting.isDebugMode) {
            val info = applicationInfo
            info.flags = ApplicationInfo.FLAG_DEBUGGABLE
        }

        /** ------------------ isOpenStrictMode 2.3以上支持----------------------- */
        if (defSetting.isDebugMode && defSetting.isOpenStrictMode
                && AndroidVersionCheckUtils.hasGingerbread()) {
            android.os.StrictMode.setThreadPolicy(android.os.StrictMode.ThreadPolicy.Builder() // 构造StrictMode
                    .detectDiskReads() // 当发生磁盘读操作时输出
                    .detectDiskWrites()// 当发生磁盘写操作时输出
                    .detectNetwork() // 访问网络时输出，这里可以替换为detectAll()
                    .penaltyLog()// 就包括了磁盘读写和网络I/O
                    // 以日志的方式输出
                    .build())
            android.os.StrictMode.setVmPolicy(android.os.StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects() // 探测SQLite数据库操作
                    .penaltyLog() // 以日志的方式输出
                    .penaltyDeath().build())

        }

        /** ------------------ Log initialize ----------------------- */
        ZLog.init(defSetting.context, defSetting.defaultTAG, defSetting.logPrintPath, *defSetting.levels)

        // 修改System.out输出流
        System.setOut(object : PrintStream(System.out) {
            @Synchronized override fun println(str: String) {
                ZLog.i(str)
            }
        })

        // 网络监听
        ZNetworkStateReceiver.registerObserver(zwNetChangeObserver)
        onAfterCreate()
    }

    open fun onAfterCreate() {

    }

    open protected fun initSetting(builder:ZSetting.Builder) {

    }

    override public fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
    }
    
    inline fun <reified T: Context> T.getApp(): KApp? {
        return this@KApp
    }

}