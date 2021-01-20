package com.shrek.klib.extension

import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.annotation.ColorRes
import com.shrek.klib.KApp

/**
 * Context的扩展
 * @author shrek
 * @date:  2016-05-27
 */
fun Context.getResColor(@ColorRes colorVal:Int):Int {
    return getResources().getColor(colorVal)
}

//fun Context.getDimens(@ColorRes colorVal:Int):Int {
//    return getResources().getDimension(colorVal)
//}

fun Context.toastLongShow(info: String) {
    Toast.makeText(this, info, Toast.LENGTH_LONG).show()
}

fun Context.toastShortShow(info: String) {
    Toast.makeText(this, info, Toast.LENGTH_SHORT).show()
}

fun Context.appVersion(): Int {
    val manager = this.packageManager
    val info = manager.getPackageInfo(this.packageName, 0)
    return info.versionCode
}

fun Context.appVersionName(): String {
    val manager = this.packageManager
    val info = manager.getPackageInfo(this.packageName, 0)
    return info.versionName
}

fun Context.targetSdkVersion(): Int {
    val manager = this.packageManager
    val info = manager.getPackageInfo(this.packageName, 0)
    return info.applicationInfo.targetSdkVersion
}

/**
 * 跳转activity
 */
inline fun  <reified T: Activity> Context.jumpActivity(noinline intentOpt: ((Intent) -> Unit)? = null) {
    val intent = Intent()
    intent.setClass(this, T::class.java)
    intentOpt?.invoke(intent)
    startActivity(intent)
}


inline fun  <reified T: Activity> Activity.jumpActivityResult(requestCode:Int ,noinline intentOpt: ((Intent) -> Unit)? = null) {
    val intent = Intent()
    intent.setClass(this, T::class.java)
    intentOpt?.invoke(intent)
    startActivityForResult(intent,requestCode)
}

inline fun <reified T: KApp> Activity.kApp():T {
    val application = this.getApplication()
    if(application !is T) {
        throw ClassCastException("application not [ ${T::class.simpleName} ]")
    }
    return application
}

/**
 * 启动服务
 */
inline fun <reified T: Service> Context.jumpService(noinline intentOpt: ((Intent) -> Unit)? = null) {
    val intent = Intent()
    intent.setClass(this, T::class.java)
    intentOpt?.invoke(intent)
    startService(intent)
}


