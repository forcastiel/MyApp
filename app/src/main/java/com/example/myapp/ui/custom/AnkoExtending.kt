package com.example.myapp.ui.custom

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Environment
import android.view.View
import android.view.ViewManager
import android.widget.FrameLayout
import androidx.cardview.widget.CardView
import com.shrek.klib.extension.kApplication
import com.shrek.klib.file.fileMimeType
import com.shrek.klib.view.KActivity
import com.shrek.klib.view.KFragment
import org.jetbrains.anko.custom.ankoView
import java.io.File
import java.io.FileOutputStream

class Const {

    companion object {
        val BO = "BO"
        val TITLE = "TITLE"
        val DATA = "DATA"
        val ARRAY = "ARRAY"
        val BO1 = "BO1"
        val TYPE = "type"
        val FLAG = "flag"
        var URL = "ROUTINE_URL"
        var SCENES = "ScenesBundle"
        val H5BO = "H5BO"
        val H5FLAG = "H5FLAG"
        var PROGRESS = "PROGRESS"

        //广播
        val LOGIN_ACTION = "LOGIN_ACTION"
        val LOGOUT_ACTION = "LOGOUT_ACTION"
    }

}

open class _CardView(ctx: Context) : CardView(ctx) {
    fun <T : View> T.lparams(
            c: android.content.Context?,
            attrs: android.util.AttributeSet?,
            init: FrameLayout.LayoutParams.() -> Unit = {}
    ): T {
        val layoutParams = FrameLayout.LayoutParams(c!!, attrs!!)
        layoutParams.init()
        this@lparams.layoutParams = layoutParams
        return this
    }

    fun <T : View> T.lparams(
            width: Int = android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
            height: Int = android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
            init: FrameLayout.LayoutParams.() -> Unit = {}
    ): T {
        val layoutParams = FrameLayout.LayoutParams(width, height)
        layoutParams.init()
        this@lparams.layoutParams = layoutParams
        return this
    }
}

inline fun ViewManager.cardView() = cardView {}
inline fun ViewManager.cardView(init: _CardView.() -> Unit) = ankoView({ _CardView(it) }, 0, init)

///**
// * loading动画
// */
//inline fun ViewManager.loadingView() = loadingView {}
//
//inline fun ViewManager.loadingView(init: LoadingView.() -> Unit) = ankoView({ LoadingView(it) }, 0, init)

fun String.toARGB(alpha: Int): Int {
    val color = Integer.parseInt(this.replace("#", ""), 16)
    val red = color and 0xff0000 shr 16
    val green = color and 0x00ff00 shr 8
    val blue = color and 0x0000ff
    return Color.argb(alpha, red, green, blue)
}

//文件分享
fun Activity.shareFile(file: File, reqCode: Int, subject: String = ""): Boolean {
    try {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = file.absolutePath.fileMimeType()
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivityForResult(Intent.createChooser(intent, null), reqCode)
        return true
    } catch (e: ActivityNotFoundException) {
        e.printStackTrace()
        return false
    }
}

fun Bitmap.toFile(context: Context) {
    var fos: FileOutputStream? = null
    try {
        val root = File(Environment.getExternalStorageDirectory().absolutePath+File.separator+"zsx_temp")
        if(!root.exists()) root.mkdirs()
        val file = File(root,
                "${System.currentTimeMillis()}.jpg")
        fos = FileOutputStream(file)
        this.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        fos.flush()
        fos.close()
        val uri = Uri.fromFile(file)
        context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri))
    } catch (e: Exception) { }
}

fun Int.toColorString(): String{
    val stringBuffer = StringBuffer("#")
    val color = kApplication.resources.getColor(this)
    val red = (color and 0xff0000) shr 16
    val green = (color and 0x00ff00) shr 8
    val blue = (color and 0x0000ff)

    stringBuffer.append(String.format("%02x", red))
    stringBuffer.append(String.format("%02x", green))
    stringBuffer.append(String.format("%02x", blue))

    return stringBuffer.toString()
}

fun Int.toColorRed(): Int{
    val color = kApplication.resources.getColor(this)
    return (color and 0xff0000) shr 16
}

fun Int.toColorGreen(): Int{
    val color = kApplication.resources.getColor(this)
    return (color and 0x00ff00) shr 8
}

fun Int.toColorBlue(): Int{
    val color = kApplication.resources.getColor(this)
    return (color and 0x0000ff)
}

inline fun KActivity.toAct(act: String) {
    try {
        val clazz = Class.forName(act)
        this.startActivity(Intent(this, clazz))
    } catch (e: Exception) {
    }
}

inline fun KFragment.toAct(act: String) {
    hostAct?.also { it.toAct(act) }
}
