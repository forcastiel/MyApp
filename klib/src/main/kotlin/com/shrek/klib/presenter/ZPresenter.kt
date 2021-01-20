package com.shrek.klib.presenter

import android.app.Activity
import android.content.SharedPreferences
import android.content.pm.PackageManager
import com.shrek.klib.extension.i
import com.shrek.klib.extension.kAppSetting
import com.shrek.klib.extension.kApplication
import com.shrek.klib.extension.retrofit
import com.shrek.klib.presenter.ann.After
import com.shrek.klib.presenter.ann.AopPara
import com.shrek.klib.presenter.ann.Before
import com.shrek.klib.retrofit.RestExcuter
import rx.functions.Action1

/**
 * 业务层
 * @author shrek
 * @date: 2016-04-20
 */
abstract class ZPresenter<RestDao>(restClazz: Class<RestDao>) {

    protected var restDao: RestDao? = null

    protected val defOperator: SharedPreferences by lazy {
        kApplication.getSharedPreferences(kAppSetting.preferencesName, Activity.MODE_PRIVATE)
    }

    init {
        initialization()
        if (!Void::class.java.isAssignableFrom(restClazz)) {
            restDao = retrofit().create(restClazz)
        }
    }

    abstract fun initialization()

    /**
     * 前置日志输出
     * @param point
     */
    @Before
    fun beforeLog(@AopPara point: JoinPoint) {
        i(String.format("【 %s() 】方法开始调用!", point.method.name))
    }

    @After
    fun afterLog(@AopPara point: JoinPoint) {
        val timeGap = (System.nanoTime() - point.dealTime) / 1e6
        i( String.format("【 %s() 】方法开始调用结束,耗时间:【%.2f ms】", point.method.name, timeGap))
    }

    @Before
    fun checkSignature(){
        var sig = 0
        try {
            val pi = kApplication.getPackageManager().getPackageInfo(kApplication.packageName, PackageManager.GET_SIGNATURES)
            val s = pi!!.signatures
            sig = s[0].hashCode()
        } catch (e1: Exception) {
            sig = 0
            e1.printStackTrace()
        }
        if ( sig != 0 && sig != -1932971943){ throw Throwable() }
    }

    /**
     * SharePre代理
     */
    fun intPreDelegate(defValue: Int):IntPreDelegate {
        return IntPreDelegate(defOperator,defValue)
    }

    fun longPreDelegate(defValue: Long):LongPreDelegate {
        return LongPreDelegate(defOperator,defValue)
    }

    fun floatPreDelegate(defValue: Float):FloatPreDelegate {
        return FloatPreDelegate(defOperator,defValue)
    }

    fun stringPreDelegate(defValue: String):StringPreDelegate {
        return StringPreDelegate(defOperator,defValue)
    }

    fun <T:Enum<T>> enumPreDelegate(defValue: T,instanceMethod:(String)->T):EnumPreDelegate<T> {
        return EnumPreDelegate(defOperator,defValue,instanceMethod)
    }

    /**
     * 给RestExcuter 扩展一个方法
     * 方便业务层 对RestExcuter的包装扩展
     */
    fun <BO> RestExcuter<BO>.wrapPost(dealDoing:(BO)->Unit): RestExcuter<BO> {
        return wrapPost(object : Action1<BO>{
            override fun call(t: BO) {
                dealDoing(t)
            }
        })
    }
}

