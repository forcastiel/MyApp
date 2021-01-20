package com.shrek.klib.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Activity
import android.view.ViewGroup
import android.view.ViewManager
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import com.rengwuxian.materialedittext.MaterialEditText
import com.shrek.klib.colligate.ReflectUtils
import com.shrek.klib.ui.crouton.Crouton
import com.shrek.klib.ui.crouton.Style
import com.shrek.klib.ui.loading.AVLoadingIndicatorView
import com.shrek.klib.ui.loading.handler.KDefaultRestHandler
import com.shrek.klib.ui.loading.handler.KRestHandlerType
import com.shrek.klib.ui.material.MaterialCheckBox
import com.shrek.klib.ui.material.RectangleButton
import com.shrek.klib.ui.navigate.NavigateBar
import com.shrek.klib.ui.photo.CircleImageView
import com.shrek.klib.ui.selector.datepick.wheel.WheelView
import com.shrek.klib.ui.swipe.SwipeMenuRecyclerView
import org.jetbrains.anko.custom.ankoView
import org.jetbrains.anko.singleLine

/**
 * @author shrek
 * @date:  2016-06-06
 */
var nonTheme = 0
inline fun ViewManager.navigateBar() = navigateBar {}
inline fun ViewManager.navigateBar(init: NavigateBar.() -> Unit) = ankoView({ NavigateBar(it)}, nonTheme , init)

fun ViewManager.navigateBar(title:String,init: (NavigateBar.() -> Unit)? = null ):NavigateBar {
    return ankoView({ NavigateBar(it) }, nonTheme){
        setTitle(title)
        //setNavBgColor(CommonUiSetup.pramaryColor)
        init?.invoke(this)
    }
}


/**
 *  Crouton扩展
 * */
fun Activity.showCrouton(text:String, style:Style = Style.INFO, viewGroup: ViewGroup? = null) {
    if(viewGroup != null) {
        Crouton.showText(this,text,style,viewGroup!!)
    } else {
        Crouton.showText(this,text,style)
    }
}

fun Activity.showInfoCrouton(text:String,viewGroup: ViewGroup? = null) {
    showCrouton(text,Style.INFO,viewGroup)
}

fun Activity.showComfirmCrouton(text:String,viewGroup: ViewGroup? = null) {
    showCrouton(text,Style.CONFIRM,viewGroup)
}

fun Activity.showAlertCrouton(text:String,viewGroup: ViewGroup? = null) {
    showCrouton(text,Style.ALERT,viewGroup)
}

/*
*   AVLoadingIndicatorView 扩展
* */
fun ViewManager.avLoadingIndicatorView(minLength:Int = 0,maxLength:Int = 0,init: (AVLoadingIndicatorView.() -> Unit)? = null ):AVLoadingIndicatorView {
    return ankoView({ AVLoadingIndicatorView(it) }, nonTheme){
        if (minLength > 0) {
            mMinWidth = minLength
            mMinHeight = minLength
        }
        if (maxLength > 0) {
            mMaxWidth = maxLength
            mMaxHeight = maxLength
        }
        init?.invoke(this)
    }
}

/*
*   kDefaultRestHandler 扩展
* */
fun <BO> Activity.kDefaultRestHandler(msg:String ,type: KRestHandlerType = KRestHandlerType.LEFT_RIGTH,
                                      init:(KDefaultRestHandler<BO>.() -> Unit)? = null ):KDefaultRestHandler<BO> {
    val handler = KDefaultRestHandler<BO>(this,msg,type)
    init?.invoke(handler)
    return handler
}

/**
 * material editTest 
 */
fun ViewManager.materialEditTest(hintStr:String = "" ,init: MaterialEditText.() -> Unit):MaterialEditText {
    return ankoView({ MaterialEditText(it) }, nonTheme){
        hint = hintStr
        init?.invoke(this)
    }
}

fun MaterialEditText.iconPadding(paddingVal:Int) {
    ReflectUtils.setFieldValue(this,"iconPadding",paddingVal)
}

//Material控件
inline fun ViewManager.rectangleButton() = rectangleButton {}
inline fun ViewManager.rectangleButton(init: RectangleButton.() -> Unit) = ankoView({ RectangleButton(it) }, nonTheme, init)

fun ViewManager.rectangleButton(text: String, backColor: Int, textColor: Int, init: (RectangleButton.() -> Unit)? = null): RectangleButton {
    return ankoView({ RectangleButton(it)}, nonTheme) {
        //        setRippleBackground(backColor)
        setButtonColor(backColor)
        setBtnText(text, textColor)
        init?.invoke(this)
    }
}

fun ViewManager.materialCheckBox(text: String,isCheck:Boolean, init: (MaterialCheckBox.() -> Unit)? = null): MaterialCheckBox {
    return ankoView({ MaterialCheckBox(it,text,isCheck)}, nonTheme) {
        init?.invoke(this)
    }
}

/**
 * 圆角图片
 */
inline fun ViewManager.circleImageView() = circleImageView {}
inline fun ViewManager.circleImageView(init: CircleImageView.() -> Unit) = ankoView({ CircleImageView(it) }, nonTheme, init)
inline fun ViewManager.circleImageView(res:Int , init: CircleImageView.() -> Unit) = ankoView({
    val view = CircleImageView(it)
    view.setImageResource(res)
    view
}, nonTheme, init)
/**
 * 滑动的recycleView
 * */
inline fun ViewManager.swipeRecyclerView() = swipeRecyclerView {}
inline fun ViewManager.swipeRecyclerView(init: SwipeMenuRecyclerView.() -> Unit)= ankoView({ SwipeMenuRecyclerView(it) }, nonTheme, init)

inline fun ViewManager.wheelView() = wheelView {}
inline fun ViewManager.wheelView(init: WheelView.() -> Unit)= ankoView({ WheelView(it) }, nonTheme, init)

/**
 * textview 收放动画
 */
inline fun TextView.createDropAnim(start: Int, end: Int, lines: Int, minLines: Int = 1): ValueAnimator{
    val animator = ValueAnimator.ofInt(start, end)
    animator.interpolator = DecelerateInterpolator()
    if (lines == minLines) {
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                this@createDropAnim.maxLines = lines
            }
        })
    } else {
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                this@createDropAnim.maxLines = lines
            }
        })
    }
    animator.addUpdateListener { arg ->
        val value = arg.animatedValue as Int
        val layoutParams = this.layoutParams
        layoutParams.height = value
        this.layoutParams = layoutParams
    }
    return animator
}

/**
 * textView收放动画
 */
inline fun TextView.expandAnim(start: Int, end: Int): ValueAnimator {
    val animator = ValueAnimator.ofInt(start, end)
    animator.duration = 200L
    animator.interpolator = DecelerateInterpolator()
    animator.addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator?) {
            if (start > end) {
                this@expandAnim.singleLine = true
            }
        }

        override fun onAnimationStart(animation: Animator?) {
            if (start < end) {
                this@expandAnim.singleLine = false
            }
        }

    })

    animator.addUpdateListener { arg ->
        val value = arg.animatedValue as Int
        this.height = value
    }
    return animator
}
