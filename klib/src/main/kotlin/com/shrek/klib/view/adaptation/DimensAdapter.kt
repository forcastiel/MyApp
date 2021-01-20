package com.shrek.klib.view.adaptation

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.widget.TextView
import com.shrek.klib.colligate.DimenUtils
import com.shrek.klib.extension.kApplication
import com.shrek.klib.extension.kDisplay

/**
 * @author shrek
 * @date:  2016-06-02
 */

var TextView.relateTextSize:CustomTSDimens
    get() = CustomTSDimens.NORMAL
    set(value) { textSize = DimensAdapter.textSpSize(value) }

var TextView.textSizeOffset:Float
    get() = 0f
    set(value) { textSize = DimensAdapter.textSpSizeOffset(value) }

object DimensAdapter {

    val nav_view_marge = 10

    val nav_height:Int by lazy {
        (kDisplay.heightPixels/13f).toInt()
    }

    val staus_Height : Int by lazy {
        var statusResult = 20
        val resourceId = kApplication.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusResult = kApplication.getResources().getDimensionPixelSize(resourceId);
        }
        statusResult
    }

    val tsDimensOffset = -0.5f
    
    //缩放因子
    val scaleFactor:Float by lazy {
        1.0f
    }


    fun dip2Px(dipVal : Float):Float {
       return DimenUtils.dpToPx(dipVal,kApplication.resources)
    }

    /**
     * dip配置
     */
    val dip1:Float by lazy {
        dip2Px(1f) * scaleFactor
    }

    val dip2:Float by lazy {
        dip2Px(2f) * scaleFactor
    }

    val dip3:Float by lazy {
        dip2Px(3f) * scaleFactor
    }

    val dip4:Float by lazy {
        dip2Px(4f) * scaleFactor
    }

    val dip5:Float by lazy {
        dip2Px(5f) * scaleFactor
    }

    val dip6:Float by lazy {
        dip2Px(6f) * scaleFactor
    }

    val dip7:Float by lazy {
        dip2Px(7f) * scaleFactor
    }

    val dip8:Float by lazy {
        dip2Px(8f) * scaleFactor
    }

    val dip9:Float by lazy {
        dip2Px(9f) * scaleFactor
    }

    val dip10:Float by lazy {
        dip2Px(10f) * scaleFactor
    }

    val dip12:Float by lazy {
        dip2Px(12f) * scaleFactor
    }

    val dip15:Float by lazy {
        dip2Px(15f) * scaleFactor
    }

    val dip20:Float by lazy {
        dip2Px(20f) * scaleFactor
    }

    val five_dip:Float by lazy {
        5 * scaleFactor
    }

    val ten_dip:Float by lazy {
        10 * scaleFactor
    }

    //获取系统的字体
    val defTextSize:Float by lazy {
        val view = TextView(kApplication)
        (view.textSize/kDisplay.scaledDensity)+tsDimensOffset
    }

    /**
     * 得到状态栏的高度
     */
    fun statusBarHeight(ctx: Context):Int {
        if(ctx is Activity) {
            val outRect = Rect()
            ctx.getWindow().getDecorView().getWindowVisibleDisplayFrame(outRect);
            return outRect.top
        }
        return 0
    }

    /**
     * 屏幕的比例
     */
    fun screenWidthScale(scaleVal:Float):Int {
        return (kDisplay.widthPixels*scaleVal).toInt()
    }

    fun screenHeightScale(scaleVal:Float):Int {
        return (kDisplay.heightPixels*scaleVal).toInt()
    }

    /**
     * 获得字体的大小
     */

    fun textSpSize(tsType : CustomTSDimens = CustomTSDimens.NULL):Float {
        return ( defTextSize + tsType.offsetSP ) * kDisplay.density / kDisplay.scaledDensity
    }

    fun textPxSize(tsType : CustomTSDimens = CustomTSDimens.NULL):Float {
        return (defTextSize + tsType.offsetSP) * kDisplay.density
    }

    fun textSpSizeOffset(offsetSP:Float):Float{
        return defTextSize + offsetSP
    }


}

/**
 * 自定义字体大小
 */
enum class CustomTSDimens(val offsetSP:Float) {

    VERY_BIG(56f),SUPER_BIG(28f),EXTREAM_BIG(8f),ABNORMAL_BIG(7f),NAV_TITLE(5f),NORMAL(0f),SLIGHTLY_BIG(1f),BIG(2f),MID_BIG(3f),BIGGER(4f)
    ,SLIGHTLY_SMALL(-1f),SMALL(-2f),MID_SMALL(-3f),VERY_SMALL(-4f),NULL(0f)
}