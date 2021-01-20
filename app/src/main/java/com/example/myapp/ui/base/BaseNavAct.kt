package com.example.myapp.ui.base

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RelativeLayout
import com.example.myapp.R
import com.shrek.klib.colligate.MATCH_PARENT
import com.shrek.klib.extension.getResColor
import com.shrek.klib.extension.weakClick
import com.shrek.klib.ui.navigate.NavigateBar
import com.shrek.klib.ui.navigateBar
import com.shrek.klib.ui.pixelRelateParams
import com.shrek.klib.view.adaptation.DimensAdapter
import org.jetbrains.anko.*

/**
 * 带导航条的导航界面
 * @author shrek
 * @date:  2018-10-23
 */
abstract class BaseNavAct(var navTitle:String, val addBackArrow:Boolean = false) : ZsxAct() {
    protected lateinit var navRootLayout:ViewGroup
    lateinit var fullScreenLayout:RelativeLayout
    lateinit var navView:NavigateBar
    lateinit var backView:ImageButton
    lateinit var statusView:View
    override var analysisTitle: String = navTitle
    //标题栏下分割线
    lateinit var navLine: View

    override fun initialize(savedInstanceState: Bundle?) {
        fullScreenTheme()
        frameLayout {
            navRootLayout = verticalLayout {
                backgroundColor = getResColor(R.color.window_background)
                statusView = view { backgroundColor = getResColor(R.color.colorPrimary) }.lparams(MATCH_PARENT, DimensAdapter.staus_Height)
                navView = navigateBar(navTitle) {
                    setBackgroundColor(Color.WHITE)
                    setTitleColor(Color.parseColor("#262628"))
                    if (addBackArrow){
                        backView = addLeftDefaultBtn(R.drawable.ic_common_nav_return){ }
                        backView.weakClick(this@BaseNavAct){
                            it.backClick()
                        }
                    }
                    navLine = view { backgroundColorResource = R.color.appTitleBarBolderColor}.pixelRelateParams(matchParent, 2){ alignParentBottom() }
                    initNavigate(this)
                }.lparams(MATCH_PARENT, DimensAdapter.nav_height)
                initContent().invoke(this)
            }
            fullScreenLayout = relativeLayout {
                visibility = View.GONE
                backgroundColor = Color.parseColor("#80000000")
                weakClick(this){
                    this.visibility = View.GONE
                }
            }.lparams(MATCH_PARENT, MATCH_PARENT)
        }

    }

    //初始化内容
    abstract fun initContent() : _LinearLayout.() -> Unit

    open fun backClick():Boolean { finish()
        return true}

    //初始化导航条
    open fun initNavigate(navView:NavigateBar){ }

}