package com.shrek.klib.ormlite

import android.content.Context
import com.shrek.klib.ZSetting
import com.shrek.klib.extension.kApplication
import com.shrek.klib.extension.otherThread
import com.shrek.klib.extension.uiThread
import com.shrek.klib.ormlite.bo.ZDb
import com.shrek.klib.ormlite.dao.DBDao

/**
 * @author Shrek
 * @date:  2017-05-08
 */
class DBOpt(context: Context, setting: ZSetting,var clazzs:Array<Class<out ZDb>>) : ZDBHelper(context, setting) {

    override fun loadDatabaseClazz(): Array<Class<out ZDb>> {
        return clazzs
    }
}

/**
 * 数据库操作
 */
inline fun <reified T: ZDb> Any.kDbOpt(dbDoing: DBDao<T>.() -> Unit ) {
    if(kApplication.dbOpt == null) { throw NullPointerException("KApp的数据库操作类未初始化!") }
    val dao = kApplication.dbOpt?.getDao(T::class.java)
    dbDoing(dao!!)
}

/**
 * 异步的数据库操作
 */
inline fun <reified T: ZDb,F> Any.kDbAsyncOpt(
        crossinline dbDoing: DBDao<T>.() -> F, crossinline nextDoing:(F)->Unit) {
    if(kApplication.dbOpt == null) { throw NullPointerException("KApp的数据库操作类未初始化!") }
    val dao = kApplication.dbOpt?.getDao(T::class.java)
    otherThread {
        val returnVal = dbDoing(dao!!)
        uiThread { nextDoing(returnVal) }
    }
}