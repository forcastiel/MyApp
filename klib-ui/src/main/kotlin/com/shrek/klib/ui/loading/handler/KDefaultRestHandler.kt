package com.shrek.klib.ui.loading.handler

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.widget.LinearLayout
import com.shrek.klib.colligate.WRAP_CONTENT
import com.shrek.klib.extension.kApplication
import com.shrek.klib.extension.kIntHeight
import com.shrek.klib.extension.kIntWidth
import com.shrek.klib.extension.viewGroupLP
import com.shrek.klib.retrofit.handler.RestHandler
import com.shrek.klib.ui.CommonUiSetup
import com.shrek.klib.ui.R
import com.shrek.klib.ui.avLoadingIndicatorView
import com.shrek.klib.ui.loading.AVLoadingIndicatorView
import com.shrek.klib.ui.loading.Indicator
import com.shrek.klib.ui.loading.indicators.BallTrianglePathIndicator
import org.jetbrains.anko.*

/**
 * @author Shrek
 * @date:  2017-03-21
 */
enum class KRestHandlerType {
    LEFT_RIGTH,TOP_BOTTOM
}

open class KDefaultRestHandler<Bo>(val context: Context, val msg:String
                              ,val type:KRestHandlerType = KRestHandlerType.LEFT_RIGTH) : RestHandler<Bo> {
    val dialog by lazy {
        val dialog1 = Dialog(context, R.style.Base_Dialog)
        dialog1
    }
    
    lateinit var avLoadingView:AVLoadingIndicatorView
    var indicator: Indicator = BallTrianglePathIndicator()

    override public fun pre() {
        var avWidth = 0
        var avHeight = 0
        
        if(type == KRestHandlerType.LEFT_RIGTH) {
            avWidth = Math.max(kIntWidth(0.12f),kIntHeight(0.09f))
        } else {
            avWidth  = Math.max(kIntWidth(0.3f),kIntHeight(0.2f))
        }
        avHeight = avWidth
        
        val view = kApplication.UI {
            linearLayout {
                backgroundResource = R.drawable.default_loading_bg
                if(type == KRestHandlerType.LEFT_RIGTH) {
                    gravity = Gravity.CENTER_VERTICAL
                    orientation = LinearLayout.HORIZONTAL
                } else {
                    gravity = Gravity.CENTER_HORIZONTAL
                    orientation = LinearLayout.VERTICAL
                }

                avLoadingView = avLoadingIndicatorView(avWidth/2,avWidth) {
                    indicator = this@KDefaultRestHandler.indicator
                    setIndicatorColor(Color.WHITE)
                }.lparams(WRAP_CONTENT, WRAP_CONTENT) {
                    horizontalMargin = kIntWidth(0.02f)
                    verticalMargin = kIntHeight(0.01f)
                }

                textView(msg) {
                    setTextColor(Color.WHITE)
                    textSize = CommonUiSetup.textSize
                }.lparams { }
            }
        }.view

        dialog.setContentView(view,viewGroupLP {  })
        dialog.setCancelable(true) // 可以取消

        if(context != null) {
            val host  = context as? Activity
            if (host != null && !host.isFinishing) {
                dialog.show()
            }
        }
        avLoadingView.show()
    }

    override fun post(bo: Bo) {
        dismiss()
    }

    override fun error(throwable: Throwable?) {
        dismiss()
    }

    fun dismiss() {
        if(dialog != null && dialog.isShowing){
            dialog.dismiss()
        }
    }

}