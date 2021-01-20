package com.shrek.klib.view.adaptation

import android.view.Gravity
import android.widget.LinearLayout
import com.shrek.klib.colligate.MATCH_PARENT
import com.shrek.klib.colligate.WRAP_CONTENT

/**
 * 通用的样式
 * @author shrek
 * @date:  2016-06-01
 */

/**
 * 垂直等分 水平充满
 */
fun verticalMatch(weightVal:Float,init : (LinearLayout.LayoutParams.() -> Unit)? = null):LinearLayout.LayoutParams.() -> Unit  {
    return {
        width = MATCH_PARENT
        height = 0
        weight = weightVal
        init?.invoke(this)
    }
}

/**
 *  水平等分 垂直充满
 */
fun horizontalMatch(weightVal:Float,init : (LinearLayout.LayoutParams.() -> Unit)? = null):LinearLayout.LayoutParams.() -> Unit  {
    return {
        width = 0
        height = MATCH_PARENT
        weight = weightVal
        init?.invoke(this)
    }
}

fun linearCenter(init : (LinearLayout.LayoutParams.() -> Unit)? = null):LinearLayout.LayoutParams.() -> Unit{
    return {
        width = WRAP_CONTENT
        height = WRAP_CONTENT
        gravity = Gravity.CENTER
        init?.invoke(this)
    }
}