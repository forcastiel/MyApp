package com.shrek.klib.extension

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.WindowManager
import com.shrek.klib.KApp
import com.shrek.klib.ZActivityManager
import com.shrek.klib.ZSetting
import com.shrek.klib.colligate.StringUtils
import com.shrek.klib.event.ZEventBus
import com.shrek.klib.file.FileOperator
import com.shrek.klib.net.ZNetChangeObserver
import com.shrek.klib.retrofit.LogInterceptor
import com.shrek.klib.thread.HandlerEnforcer
import com.shrek.klib.thread.ZThreadEnforcer
import com.squareup.picasso.Picasso
import okhttp3.OkHttpClient
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.fastjson.FastJsonConverterFactory
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 * 注入
 * @author shrek
 * @date:  2016-05-30
 */
val Any.kLayoutInflater: LayoutInflater by lazy {
    LayoutInflater.from(KApp.app)
}

val Any.kDisplay: DisplayMetrics by lazy {
    KApp.app.provideDisplay
}


@Application
val Any.kApplication: KApp
    get() = KApp.app

val Any.picasso: Picasso by lazy {
    Picasso.with(KApp.app)
}

@Application
val Any.actManager: ZActivityManager
    get() = KApp.app.actManager

@Application
val Activity.kObserver: ZNetChangeObserver
    get() = KApp.app.observer

val Any.eventBus: ZEventBus
    get() = ZEventBus()

val Any.kEnforer: ZThreadEnforcer
    get() = HandlerEnforcer.newInstance()

@Application
val Any.kAppSetting: ZSetting
    get() = KApp.app.defSetting


fun Any.getOkHttpBuilder(): OkHttpClient.Builder {
    return OkHttpClient.Builder().readTimeout(30, TimeUnit.SECONDS).connectTimeout(30, TimeUnit.SECONDS)
}

//fun gson(): GsonBuilder {
//    val setting = KApp.app.appSetting
//    return GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().setDateFormat(setting.getGsonTimeFormat())
//}

//fun Any.jsonBuilder(): GsonBuilder {
//    return gson()
//}

/**
 * 相对屏幕的宽度
 */
fun Any.kWidth(rate:Float = 1.0f):Float {
    return kDisplay.widthPixels*rate
}

fun Any.kIntWidth(rate:Float = 1.0f):Int {
    return kWidth(rate).toInt()
}

/**
 * 屏幕的高度
 */
fun Any.kHeight(rate:Float = 1.0f):Float {
    return kDisplay.heightPixels * rate
}

fun Any.kIntHeight(rate:Float = 1.0f):Int {
    return kHeight(rate).toInt()
}

/**
 * 屏幕的高度(全面屏含状态栏)
 */
fun Any.realHeight(rate:Float = 1.0f):Float {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N){
        return kDisplay.heightPixels * rate
    } else {
        val metrics = DisplayMetrics()
        (kApplication.getSystemService(Context.WINDOW_SERVICE) as? WindowManager)?.defaultDisplay?.getRealMetrics(metrics)
        return metrics.heightPixels * rate
    }
}

fun Any.kIntRealHeight(rate:Float = 1.0f):Int {
    return realHeight(rate).toInt()
}

private fun Any.retrpfitCreate(url:String? = null , factory: Converter.Factory): Retrofit {
    val builder = getOkHttpBuilder()

    val setting = KApp.app.kAppSetting

    if (setting.isDebugMode()) {
        builder.addInterceptor(LogInterceptor())
    }
    val interceptors = setting.getCustomInterceptors()
    if (interceptors != null) {
        for (interceptor in interceptors) {
            builder.addInterceptor(interceptor)
        }
    }

    if (!StringUtils.isEmpty(setting.getRestCachePath())) {
        val fileOperator = FileOperator(KApp.app, setting.getRestCachePath())
        val cache = okhttp3.Cache(fileOperator.getOptFile(), 20 * 1024 * 1024.toLong())
        builder.cache(cache)
    }

    val retrofit = Retrofit.Builder().baseUrl( url ?: setting.getRestBaseUrl() )
            .addCallAdapterFactory(RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io()))
            .addConverterFactory(factory)
            .validateEagerly(true).client(builder.build()).build()

    return retrofit
}

@Application
fun Any.retrofit(url:String? = null): Retrofit {
    return retrpfitCreate(url,FastJsonConverterFactory.create())
}

@Application
fun Any.xmlRetrofit(url:String? = null): Retrofit {
    return retrpfitCreate(url,SimpleXmlConverterFactory.create())
}