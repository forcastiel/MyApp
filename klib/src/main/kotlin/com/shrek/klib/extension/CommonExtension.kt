package com.shrek.klib.extension

import com.shrek.klib.colligate.cahce.ZCache
import com.shrek.klib.ormlite.ZDBHelper
import com.shrek.klib.ormlite.bo.ZDb
import com.shrek.klib.ormlite.dao.DBDao
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

/**
 * @author shrek
 * @date:  2016-05-25
 */

fun String.isEmpty():Boolean {
    return length == 0
}

/**
 * String转化为日期
 */
fun String.toDate(formatType:String):Date?{
    var date:Date? = null
    try {
        date = SimpleDateFormat(formatType).parse(this)
    }catch (e:Exception) {
        
    }
    return date
}

inline fun String.safeToDouble():Double{
    return this.toDoubleOrNull()?:0.0
}

fun String.safeToInt():Int{
    return this.toIntOrNull()?:0
}

/**
 * 验证手机格式 移动：134、135、136、137、138、139、150、151、157(TD)、158、159、187、188+++++147
 * 联通：130、131、132、152、155、156、185、186 电信：133、153、180、189,177
 * 第一位必定为1，第二位必定为3或5或7或8，其他位置的可以为0-9
 */
fun String.isMobile(): Boolean {
    // "[1]"代表第1位为数字1，"[3578]"代表第二位可以为3、5、7、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
    val telRegex = "[1][23456789]\\d{9}"
    if (isEmpty()) { return false }
    return matches(telRegex.toRegex())
}

/**
 * 验证邮箱
 */
fun String.isEmail(): Boolean {
    var flag = false
    try {
        val check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$"
        val regex = Pattern.compile(check)
        val matcher = regex.matcher(this)
        flag = matcher.matches()
    } catch (e: Exception) {
        flag = false
    }
    return flag
}

fun <T> T.weakSelf():WeakReference<T> {
    return WeakReference<T>(this)
}

/**
 * 主线程做什么
 */
fun Any.uiThread(delay:Long = 0,doing:()->Unit):Unit{
    if(delay == 0L) {
        kEnforer.enforceMainThread { doing() }
    } else {
        kEnforer.enforceMainThreadDelay({
            doing()
        },delay)
    }
}

/**
 * 其他线程做什么
 * 这里面用的是同一个线程的 消息队列
 */
fun Any.otherThread(delay:Long = 0,doing:()->Unit):Unit{
    if(delay == 0L) {
        kEnforer.enforceBackgroud { doing() }
    } else {
        kEnforer.enforceBackgroudDelay({
            doing()
        },delay)
    }
}


/**
 * 其他线程做什么
 * 每次都创建新的
 */
fun Any.bg(delay:Long = 0,doing:()->Unit):Unit{
    if(delay == 0L) {
        kEnforer.enforceBackgroud { doing() }
    } else {
        kEnforer.enforceBackgroudDelay({
            doing()
        },delay)
    }
}


/**
 * 缓存
 */
fun Any.cache(init: ZCache.() -> Unit):Unit {
    val cache = ZCache.get(kApplication,kAppSetting.cachePath)
    cache.apply {
        init()
    }
}

/**
 * 随机做什么
 */
fun Any.random(init:(Random) -> Unit) {
    init(Random())
}


inline fun <reified T: ZDb > Any.kDbOpt(dbOpt : ZDBHelper? = null ,dbDoing: DBDao<T>.() -> Unit ) {
    if (kApplication.dbOpt == null && dbOpt == null) {
        throw NullPointerException("ZDBHelper must init!")
    }
    if(kApplication.dbOpt == null) {
        kApplication.dbOpt = dbOpt
    }
    val dao = kApplication.dbOpt?.getDao(T::class.java)
    dbDoing(dao!!)
}

/**
 * 异步的数据库操作
 */
inline fun <reified T: ZDb,F> Any.kDbAsyncOpt(dbOpt : ZDBHelper? = null ,
        crossinline dbDoing: DBDao<T>.() -> F, crossinline nextDoing:(F)->Unit) {
    if (kApplication.dbOpt == null && dbOpt == null) {
        throw NullPointerException("ZDBHelper must init!")
    }

    if(kApplication.dbOpt == null) {
        kApplication.dbOpt = dbOpt
    }
    val dao = kApplication.dbOpt?.getDao(T::class.java)
    otherThread {
        val returnVal = dbDoing(dao!!)
        uiThread { nextDoing(returnVal) }
    }
}


/**
 * 得到代码运行的时间间隔
 */
fun Any.timeInterval(timeDoing : ()->Unit):Long {
    val start = System.currentTimeMillis()
    timeDoing.invoke()
    return System.currentTimeMillis() - start
}
