package com.example.myapp.ui.custom

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.*
import com.shrek.klib.colligate.MATCH_PARENT
import com.shrek.klib.colligate.WRAP_CONTENT
import com.shrek.klib.extension.*
import com.shrek.klib.ui.*
import com.shrek.klib.ui.loading.indicators.BallSpinFadeLoaderIndicator
import org.jetbrains.anko.*
import com.shrek.klib.view.adaptation.*
import org.jetbrains.anko.textView
import org.jetbrains.anko.linearLayout
import com.example.myapp.R

/**
 * 卓师兄
 * @author shrek
 * @date:  2018-11-02
 */
class ZsxTip(val context: Context) {

    fun dialog(customView: View, style: Int = R.style.DialogTheme, isCancelable: Boolean = true, width: Int? = null, height: Int? = null): AlertDialog? {
        //判断宿主情况
        var isVaild = true
        (context as? Activity)?.also {
            isVaild = !it.isFinishing && (if (Build.VERSION.SDK_INT >= 17) !it.isDestroyed else true)
        }
        if (!isVaild) {
            return null
        }
        val dialog = AlertDialog.Builder(context, style).setCancelable(isCancelable).show()
        dialog.setContentView(customView)
        val window = dialog?.getWindow()
        window?.setGravity(Gravity.CENTER)
        window?.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        window?.setWindowAnimations(R.style.popupAnimation);
        val m = (context as Activity).windowManager
        val d = m.defaultDisplay  //为获取屏幕宽、高
        width?.also {
            val params = window?.attributes
            params?.width = it
            dialog?.window?.attributes = params
        }
        height?.also {
            val params = window?.attributes
            params?.height = it
            dialog?.window?.attributes = params
        }
        return dialog
    }

    fun dialog(verPadding: Int = kIntWidth(0.04f), horPadding: Int = kIntWidth(0.04f), isCancelable: Boolean = true, bgColor: Int = R.color.colorWhite, viewProcess: _RelativeLayout.(AlertDialog) -> Unit): AlertDialog? {
        val dialog = dialog(View(context), R.style.DialogTheme, isCancelable, kIntWidth(0.8f))
        if (dialog == null) {
            return null
        }
        val customView = context.UI {
            cardView {
                radius = DimensAdapter.dip5
                preventCornerOverlap = true
                useCompatPadding = true
//                val paddingVal = kIntWidth(0.04f)
                setContentPadding(horPadding, verPadding, horPadding, verPadding)
                setCardBackgroundColor(kApplication.getResColor(bgColor))
//                backgroundColor = Color.WHITE
                relativeLayout {
                    viewProcess.invoke(this, dialog)
                }
            }
        }.view
        dialog?.setContentView(customView)
        return dialog
    }

    fun chooseDialog(title: String, content: CharSequence, leftTipTxt: String, rightTipTxt: String, isNeedModifyRightTipColor: Boolean = false, isBold: Boolean = false, iconResId: Int = R.mipmap.ic_launcher, isCancelable: Boolean = true, process: (Boolean) -> Unit = { }): AlertDialog? {
        return dialog(0, 0, isCancelable) { dialog ->
            val titleView = textView(title) {
                kRandomId()
                textColor = Color.parseColor("#4C4C4C")
                textSize = DimensAdapter.textSpSize(CustomTSDimens.BIG)
                paint.isFakeBoldText = true
            }.lparams {
                topMargin = kIntHeight(0.01f) + kIntWidth(0.04f)
                centerHorizontally()
                horizontalMargin = kIntWidth(0.02f)
            }

            val contentView = textView(content) {
                kRandomId()
                textColor = Color.parseColor("#666666")
                relateTextSize = if (isBold) CustomTSDimens.BIG else CustomTSDimens.NORMAL
                paint.isFakeBoldText = isBold
            }.lparams {
                topMargin = kIntHeight(0.015f)
                leftMargin = kIntWidth(0.04f)
                rightMargin = kIntWidth(0.04f)
                below(titleView)
            }

            val iconView = imageView {
                adjustViewBounds = true
                kRandomId()
                visibility = if (iconResId == R.mipmap.ic_launcher) View.GONE else View.VISIBLE
                imageResource = iconResId
            }.lparams {
                below(contentView)
                centerHorizontally()
            }

            val line = view {
                kRandomId()
                backgroundColorResource = R.color.colorLineGray
            }.lparams(MATCH_PARENT, 2) {
                below(iconView)
                topMargin = kIntHeight(0.02f)
            }

            linearLayout {
                textView(leftTipTxt) {
                    kRandomId()
                    visibility = if (leftTipTxt.isNotEmpty()) View.VISIBLE else View.GONE
                    gravity = Gravity.CENTER
                    padding = kIntWidth(0.01f)
                    textColor = Color.parseColor("#4C4C4C")
                    textSize = DimensAdapter.textSpSize(CustomTSDimens.SLIGHTLY_BIG)
                    setOnClickListener {
                        process.invoke(false)
                        dialog.dismiss()
                    }
                }.pixelLinearParams(0, WRAP_CONTENT, 1f) {
                    topMargin = kIntWidth(0.02f)
                    bottomMargin = kIntWidth(0.02f)
                }

                view {
                    kRandomId()
                    visibility = if (leftTipTxt.isNotEmpty()) View.VISIBLE else View.GONE
                    backgroundColorResource = R.color.colorLineGray
                }.pixelLinearParams(2, MATCH_PARENT) {}

                textView(rightTipTxt) {
                    kRandomId()
                    gravity = Gravity.CENTER
                    padding = kIntWidth(0.01f)
                    textColor = if (isNeedModifyRightTipColor) Color.parseColor("#4C4C4C") else resources.getColor(R.color.colorPrimary)
                    textSize = DimensAdapter.textSpSize(CustomTSDimens.SLIGHTLY_BIG)
                    setOnClickListener {
                        process.invoke(true)
                        dialog.dismiss()
                    }
                }.pixelLinearParams(0, WRAP_CONTENT, 1f) {
                    topMargin = kIntWidth(0.02f)
                    bottomMargin = kIntWidth(0.02f)
                }
            }.lparams(MATCH_PARENT, WRAP_CONTENT) {
                below(line)
            }
        }
    }

    //显示正常的提示弹出框
    fun noticeDialog(title: String, content: String, tipTxt: String, process: () -> Unit = { }): AlertDialog? {
        return chooseDialog(title, content, "", tipTxt) {
            if (true) {
                process.invoke()
            }
        }
    }

    fun noticeDialog(title: String, content: CharSequence, tipTxt: String, iconResId: Int, process: () -> Unit = { }): AlertDialog? {
        return chooseDialog(title, content, "", tipTxt, false, false, iconResId) {
            process.invoke()
        }
    }

    fun noticeDialog(title: String, content: String, leftTipTxt: String, rightTipTxt: String, isCancelable: Boolean, process: (Boolean) -> Unit = { }): AlertDialog? {
        return chooseDialog(title, content, leftTipTxt, rightTipTxt, false, true, R.mipmap.ic_launcher, isCancelable) {
            process.invoke(it)
        }
    }

    fun loading(): AlertDialog? {
        val cusView = context.UI {
            cardView {
                radius = DimensAdapter.dip5
                preventCornerOverlap = true
                useCompatPadding = true
                val paddingVal = kIntWidth(0.04f)
                setContentPadding(paddingVal, paddingVal, paddingVal, paddingVal)
                backgroundResource = R.drawable.loading_bg

                val avWidth = Math.max(kIntWidth(0.08f), kIntHeight(0.06f))
                avLoadingIndicatorView(avWidth / 2, avWidth) {
                    indicator = BallSpinFadeLoaderIndicator()
                    setIndicatorColor(context.getResColor(R.color.colorPrimary))
                }.lparams(avWidth, avWidth)
            }
        }.view
        return dialog(cusView)
    }


}

