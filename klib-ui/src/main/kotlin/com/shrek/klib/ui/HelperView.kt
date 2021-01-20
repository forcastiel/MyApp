package com.shrek.klib.ui

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import android.view.ViewManager
import com.shrek.klib.colligate.BaseUtils
import com.shrek.klib.extension.kDisplay
import com.shrek.klib.extension.rectF
import com.shrek.klib.view.adaptation.CustomTSDimens
import com.shrek.klib.view.adaptation.DimensAdapter
import org.jetbrains.anko.custom.ankoView

/**
 * @author shrek
 * @date:  2016-06-23
 */
class HelperView(ctx: Context) : View(ctx) {

    var framePaint = Paint()

    var whitePaint = Paint()

    //当前的view
    var currView: Pair<View, String>? = null

    val helperViews = mutableMapOf<View, String>()

    val xfermodeVal = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)

    val statusBarHeight by lazy { DimensAdapter.statusBarHeight(context) }

    init {
        with(framePaint) {
            color = CommonUiSetup.pramaryColor
            strokeWidth = 5f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        with(whitePaint) {
            color = Color.WHITE
            isAntiAlias = true
            style = Paint.Style.STROKE
            textSize = DimensAdapter.textPxSize(CustomTSDimens.BIGGER)
        }
    }

    override fun onDraw(canvas: Canvas) {
        val layerId = canvas.saveLayer(0f, 0f, kDisplay.widthPixels.toFloat(), kDisplay.heightPixels.toFloat()
                , whitePaint, Canvas.ALL_SAVE_FLAG);

        with(whitePaint) {
            color = Color.BLACK
            setAlpha(170)
            style = Paint.Style.FILL_AND_STROKE
            xfermode = null
        }

        canvas.drawRect(0f, 0f, kDisplay.widthPixels.toFloat(), kDisplay.heightPixels.toFloat(), whitePaint)

        with(whitePaint) {
            xfermode = xfermodeVal
            color = Color.WHITE
        }

        val currRectF = getCurrRect()
        if (currRectF != null) {
            canvas.drawOval(RectF(currRectF), whitePaint)
            canvas.drawOval(RectF(currRectF.left + 2, currRectF.top + 2, currRectF.right - 2, currRectF.bottom - 2), framePaint)

            //第二个椭圆偏移量
            //得到2个椭圆的宽高
            var secWidth = 0f
            var thirdWidth = 0f
            if (currRectF.width() > kDisplay.widthPixels / 10) {
                secWidth = kDisplay.widthPixels / 15f
                thirdWidth = kDisplay.widthPixels / 20f
            } else {
                secWidth = currRectF.width() / 2
                thirdWidth = currRectF.width() / 3
            }

            //水平和垂直的偏移量
            val heightGap = kDisplay.heightPixels / 30
            val widthGap = Math.abs(kDisplay.widthPixels / 2 - currRectF.centerX()) / 4

            var secCenterX = 0f
            var thirdCenterX = 0f
            if (currRectF.centerX() <= kDisplay.widthPixels / 2) {
                secCenterX = currRectF.centerX() + secWidth / 2
                thirdCenterX = secCenterX + widthGap

            } else {
                secCenterX = currRectF.centerX() - secWidth / 2
                thirdCenterX = secCenterX - widthGap
            }

            var secCenterY = 0f
            var thirdCenterY = 0f
            //方向 false下面  true 上面
            var textY = 0f
            if (currRectF.centerY() <= kDisplay.heightPixels / 2) {
                secCenterY = currRectF.bottom + heightGap + secWidth / 2
                thirdCenterY = secCenterY + heightGap + secWidth / 2 + thirdWidth / 2
                textY = thirdCenterY + heightGap + thirdWidth / 2 + whitePaint.textSize
            } else {
                secCenterY = currRectF.top - heightGap - secWidth / 2
                thirdCenterY = secCenterY - heightGap - secWidth / 2 - thirdWidth / 2

                textY = thirdCenterY - heightGap - thirdWidth / 2
            }

            canvas.drawCircle(secCenterX, secCenterY, secWidth / 2, whitePaint)
            canvas.drawCircle(thirdCenterX, thirdCenterY, thirdWidth / 2, whitePaint)

            canvas.drawCircle(secCenterX, secCenterY, secWidth / 2, framePaint)
            canvas.drawCircle(thirdCenterX, thirdCenterY, thirdWidth / 2, framePaint)

            //算文字的x坐标
            val contentTxt = currView?.second!!
            var textX = thirdCenterX - (contentTxt.length / 2) * whitePaint.textSize
            textX = Math.max(0f, textX)
            canvas.drawText(contentTxt, textX, textY, whitePaint)
        }

        whitePaint.xfermode = null
        canvas.restoreToCount(layerId);

    }


    fun addViews(vararg views: Pair<View, String>) {
        views.forEach { helperViews.put(it.first, it.second) }
        if (views.size > 0) {
            val first = helperViews.entries.first()
            currView = first.toPair()
        }
    }


    fun getCurrRect(): RectF? {
        val rectFVal = currView?.first?.rectF
        if (rectFVal != null) {
            val wGap = rectFVal.width() / 6
            val hGap = rectFVal.height() / 6
            return RectF(rectFVal.left - wGap, rectFVal.top - hGap - statusBarHeight, rectFVal.right + wGap, rectFVal.bottom + hGap - statusBarHeight)
        }
        return null
    }

    fun next() {
        if (currView == null) {
            if (helperViews.size > 0) {
                val first = helperViews.entries.first()
                currView = first.toPair()
            }
        } else {
            var isFind = false
            val key = currView?.first
            currView = null
            helperViews.forEach {
                if (isFind && currView == null) {
                    currView = it.toPair()
                }

                if (!isFind && it.key === key) {
                    isFind = true
                }
            }
        }

        if (currView != null) {
            invalidate()
        } else {
            visibility = GONE
            invalidate()
        }
    }


    //点击事件 只相应点击相应的控件
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (currView == null) {
                    return false
                }

                val touchView = currView!!.first
                return !BaseUtils.isViewInArea(event.x.toFloat(), event.y.toFloat() + statusBarHeight, touchView)
            }
        }
        return super.onTouchEvent(event)
    }
}


inline fun ViewManager.helperView() = helperView {}
inline fun ViewManager.helperView(init: HelperView.() -> Unit) = ankoView({ HelperView(it) }, nonTheme, init)