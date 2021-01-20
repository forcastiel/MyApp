package com.shrek.klib

import android.app.Activity
import android.content.Context
import com.shrek.klib.extension.weakSelf
import com.shrek.klib.view.KActivity
import java.lang.ref.WeakReference
import java.util.*

class ZActivityManager {
    private var activityStack = Stack<WeakReference<KActivity>>()
    val current: KActivity?
        get() = if( activityStack.empty() ) null else activityStack.lastElement().get()
    //关闭最上层 activity
    fun popActivity() {
        activityStack.lastOrNull()?.also {
            it.get()?.finish()
            it.clear()
        }
    }

    /**
     * 关闭 指定的 activity
     * @param activity
     */
    fun popActivity(activity: KActivity) {
        if (!activity.isFinishing){
            activity.finish()
        }
        val index = activityStack.indexOfLast { it.get() == activity }
        if (index >= 0 && index < activityStack.size ){
            activityStack.removeAt(index)
        }
    }

    /**
     * 添加 新的 activity
     * @param activity
     */
    fun pushActivity(activity: KActivity) {
        activityStack.add(activity.weakSelf())
    }

    fun currentProcess(defCtx:Context? = null,process:(KActivity)->Unit){
        val host = defCtx as? KActivity
        (current ?: host)?.also {
            process.invoke(it)
        }
    }

    /*
     * 关闭其他除了指定的
     */
    fun popAllActivityExceptOne(cls: Class<out Activity>) {

    }

    /**
     * 弹出activity 直到遇到cls
     *
     * @param cls
     */
    fun popActivityUntilOne(cls: Class<out KActivity>) {

    }

    /*
     * 关闭其他除了指定的
     */
    fun popAllActivity() {
        activityStack.forEach {
            it.get()?.finish()
            it.clear()
        }
        activityStack.clear()
    }

}