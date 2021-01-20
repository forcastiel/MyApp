package com.shrek.klib.view

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment
import com.shrek.klib.extension.kApplication
import java.lang.ref.WeakReference

open class KFragment : Fragment() {
    var weakSelf = WeakReference<KFragment>(this)
    val hostAct: KActivity?
        get() {
            var act: Activity? = activity
            if (act == null) {
                act = kApplication.actManager.current
            }
            return act as? KActivity
        }

    override fun getContext(): Context {
        return super.getContext() ?: kApplication
    }
    // onshow 在 tab页面中调用有个500毫秒的延时 用来规避页面还没初始化完成调用页面元素的问题 导致onshow和onhide无法在调用顺序上保证一致，此处使用willshow来保证其对应顺序
    open fun willShow(){}
    open fun onShow(showCode:Int = 0){}
    open fun onHide(){}
    open fun onKeyBackPressed(): Boolean {
        return false
    }
}