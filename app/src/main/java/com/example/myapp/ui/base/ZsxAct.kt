package com.example.myapp.ui.base

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import com.example.myapp.R
import com.example.myapp.ui.custom.ZsxTip
import com.shrek.klib.colligate.PermissionDescript
import com.shrek.klib.colligate.toAppSetting
import com.shrek.klib.view.KActivity

/**
 * @author shrek
 * @date:  2018-10-23
 */
abstract class ZsxAct : KActivity() {

    //数据分析的标题
    open var analysisTitle: String = ""
    private var lastUserStatus: Boolean? = null
    open var isNeedActiveOut = false

    override fun preCreate(savedInstanceState: Bundle?) {
    }

    override fun onResume() {
        super.onResume()
    }

    override var refuseCallback: (List<PermissionDescript>) -> Unit = {
        var returnString = ""
        if (it.size != 0) {
            var sb = StringBuilder(it[0].abbDes)
            it.forEachIndexed { index, permissionDescript ->
                if (index > 0) {
                    sb.append(",${permissionDescript.abbDes}")
                }
            }
            returnString = sb.toString()
        }
        ZsxTip(this).noticeDialog("权限操作失败", "您拒绝${getString(R.string.app_name)}的申请，会导致后续操作失败，请重新启动程序并同意权限的申请!", "我知道了")
    }

    override var neverRefuseCallback: (List<PermissionDescript>) -> Unit = {
        var returnString = ""
        if (it.size != 0) {
            var sb = StringBuilder(it[0].abbDes)
            it.forEachIndexed { index, permissionDescript ->
                if (index > 0) {
                    sb.append(",${permissionDescript.abbDes}")
                }
            }
            returnString = sb.toString()
        }
        ZsxTip(this).noticeDialog("权限操作失败", "您已永久拒绝${getString(R.string.app_name)}的申请，需要您去系统设置里手动设置这些权限设置，才能进行后续操作!!", "去设置") {
            toAppSetting()
        }
    }

    fun fullScreenTheme(color: Int = Color.WHITE) {
        val window = getWindow()
//        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        try {
//            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            if (Build.VERSION.SDK_INT < 21) {
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            }
            if (Build.VERSION.SDK_INT >= 21) {
                window.statusBarColor = color
            }
            //View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            getWindow().decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } catch (e: Exception) {
        }

    }

    protected fun transparentStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            if (Build.VERSION.SDK_INT >= 21) {
                window.statusBarColor = Color.TRANSPARENT
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            }
        }
    }

}