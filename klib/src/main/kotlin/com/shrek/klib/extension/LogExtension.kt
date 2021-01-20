package com.shrek.klib.extension

import com.shrek.klib.BuildConfig
import com.shrek.klib.logger.ZLog

/**
 * @author shrek
 * @date:  2016-05-26
 */
enum class LogType {
    CONSOLE,LOCAL,BOTH
}

inline fun <reified T> T.d(info:String,type:LogType = LogType.CONSOLE) {
    when(type) {
        LogType.CONSOLE -> ZLog.d(T::class.simpleName,info)
        LogType.LOCAL -> ZLog.d2P(T::class.simpleName,info)
        LogType.BOTH -> ZLog.dAndP(T::class.simpleName,info)
    }
}

inline fun <reified T> T.i(info:String,type:LogType = LogType.CONSOLE) {
    when(type) {
        LogType.CONSOLE -> ZLog.i(T::class.simpleName,info)
        LogType.LOCAL -> ZLog.i2P(T::class.simpleName,info)
        LogType.BOTH -> ZLog.iAndP(T::class.simpleName,info)
    }
}


inline fun <reified T> T.w(info:String,type:LogType = LogType.CONSOLE) {
    when(type) {
        LogType.CONSOLE -> ZLog.w(T::class.simpleName,info)
        LogType.LOCAL -> ZLog.w2P(T::class.simpleName,info)
        LogType.BOTH -> ZLog.wAndP(T::class.simpleName,info)
    }
}

inline fun <reified T> T.e(info:String,type:LogType = LogType.CONSOLE) {
    if (BuildConfig.DEBUG) {
        when(type) {
        LogType.CONSOLE -> ZLog.e(T::class.simpleName,info)
        LogType.LOCAL -> ZLog.e2P(T::class.simpleName,info)
        LogType.BOTH -> ZLog.eAndP(T::class.simpleName,info)
        }
    }
}

operator fun String.dec():String{
    i(this)
    return ""
}

operator fun String.rangeTo(type: LogType){
    i(this,type)
}

