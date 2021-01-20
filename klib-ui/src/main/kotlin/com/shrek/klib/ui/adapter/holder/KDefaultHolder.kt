package com.shrek.klib.ui.adapter.holder

import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.shrek.klib.colligate.MATCH_PARENT
import com.shrek.klib.colligate.WRAP_CONTENT
import com.shrek.klib.extension.kIntHeight
import com.shrek.klib.extension.kIntWidth
import com.shrek.klib.ui.CommonUiSetup
import com.shrek.klib.ui.adapter.HolderBo
import org.jetbrains.anko.*

/**
 * 缺省的holder
 * @author Shrek
 * @date:  2017-03-16
 */
enum class KDefaultHolderType {
    TITLE, TITLE_DES, ICON_TITLE_DES, ICON_TITLE_DES1
}

open class KDefaultHolder(val holderType: KDefaultHolderType, cellHeight: Int = WRAP_CONTENT)
    : HolderBo(cellHeight, mapOf("holderType" to holderType)) {

    var titleView: TextView? = null
    var desView: TextView? = null
   lateinit var iconView: ImageView

    lateinit var superView: View
    
    override fun rootViewInit(): _LinearLayout.() -> View {
        return {
            val vMargin = kIntHeight(0.01f)
            val hMargin = kIntWidth(0.02f)

            val holderType = params?.get("holderType") as KDefaultHolderType
            superView = relativeLayout {
                if (holderType == KDefaultHolderType.TITLE || holderType == KDefaultHolderType.TITLE_DES) {

                    titleView = textView("dsadasdasda") {
                        textSize = CommonUiSetup.textSize
                        textColor = CommonUiSetup.textColor
                    }

                    if (holderType == KDefaultHolderType.TITLE) {
                        titleView?.lparams(WRAP_CONTENT, WRAP_CONTENT) {
                            horizontalMargin = hMargin
                            verticalMargin = vMargin
                            centerVertically()
                        }
                        return@relativeLayout
                    }

                    desView = textView {
                        textSize = CommonUiSetup.textSize
                        textColor = CommonUiSetup.grayTextColor
                    }

                    if (holderType == KDefaultHolderType.TITLE_DES) {
                        titleView?.lparams(WRAP_CONTENT, WRAP_CONTENT) {
                            horizontalMargin = hMargin
                            verticalMargin = vMargin
                            centerVertically()
                        }

                        desView?.lparams(WRAP_CONTENT, WRAP_CONTENT) {
                            horizontalMargin = hMargin
                            centerVertically()
                            alignParentRight()
                        }
                        return@relativeLayout
                    }
                    return@relativeLayout
                }

                linearLayout {
                    iconView = imageView().lparams(MATCH_PARENT, MATCH_PARENT, 2f) {
                        horizontalMargin = hMargin
                        verticalMargin = vMargin
                    }

                    verticalLayout {
                        if (holderType == KDefaultHolderType.ICON_TITLE_DES) {
                            gravity = Gravity.LEFT
                        } else {
                            gravity = Gravity.RIGHT
                        }
                        
                        titleView = textView {
                            textSize = CommonUiSetup.textSize
                            textColor = CommonUiSetup.textColor
                            gravity = Gravity.CENTER_VERTICAL
                        }.lparams(WRAP_CONTENT, MATCH_PARENT, 1f)

                        desView = textView {
                            textSize = CommonUiSetup.textSize
                            textColor = CommonUiSetup.grayTextColor
                            gravity = Gravity.CENTER_VERTICAL
                        }.lparams(WRAP_CONTENT, MATCH_PARENT, 1f) 

                    }.lparams(MATCH_PARENT, MATCH_PARENT, 1f) {
                        horizontalMargin = hMargin
                        verticalMargin = vMargin
                    }
                }.lparams(MATCH_PARENT, MATCH_PARENT)
            }
            superView
        }
    }
}