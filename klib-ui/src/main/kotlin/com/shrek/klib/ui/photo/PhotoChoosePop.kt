package com.shrek.klib.ui.photo

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import com.shrek.klib.colligate.MATCH_PARENT
import com.shrek.klib.colligate.WRAP_CONTENT
import com.shrek.klib.extension.*
import com.shrek.klib.view.adaptation.CustomTSDimens
import com.shrek.klib.view.adaptation.DimensAdapter
import org.jetbrains.anko.*
import java.io.File
import java.io.FileNotFoundException

/**
 * @author Shrek
 * @date:  2017-03-22
 */
class PhotoChoosePop(var hostAct: Activity, var showImageView: ImageView?, var isPNG: Boolean = false
                     , var randomFileName:Boolean = false,var imgProcess: ((imgUri: String) -> Unit)? = null) : PopupWindow(hostAct) {

    private val PIC_FROM_CAMERA = 1
    private val PIC_FROM_LOCALPHOTO = 0
    private val PIC_FROM_LOCAL = 2

    lateinit var rootView: View

    lateinit var takePhotoBtn: TextView
    lateinit var galleryBtn: TextView
    lateinit var cancelBtn: TextView

    var photoUri: Uri? = null

    var picFile: File? = null

    companion object {
        fun decodeUriAsBitmap(hostAct: Activity, uri: Uri): Bitmap? {
            var bitmap: Bitmap? = null
            try {
                bitmap = BitmapFactory.decodeStream(hostAct.contentResolver
                        .openInputStream(uri))
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                return null
            }
            return bitmap
        }
    }
    
    init {
        val rootView = kApplication.UI {
            relativeLayout() {
                backgroundColor = Color.parseColor("#80000000")
                onMyClick { dismiss() }
                
                verticalLayout {
                    backgroundColor = Color.WHITE
                    gravity = Gravity.BOTTOM
                    
                    val textColor1 = Color.parseColor("#939393")
                    val textColor2 = Color.parseColor("#f5f5f5")
                    takePhotoBtn = textView("拍照") {
//                        backgroundResource = R.drawable.comm_photo_btn_bg
                        textColor = textColor1
                        textSize = DimensAdapter.textSpSize(CustomTSDimens.NORMAL)
                        gravity = Gravity.CENTER
                        verticalPadding = kIntHeight(0.015f)
                        onMyClick { doHandlerPhoto(PIC_FROM_CAMERA) }
                    }.lparams(MATCH_PARENT, WRAP_CONTENT) {
                        horizontalMargin = kIntWidth(0.04f)
                        topMargin = kIntHeight(0.02f)
                    }
                    view{ backgroundColor = textColor2 }.lparams(MATCH_PARENT,1)
                    galleryBtn = textView("从手机相册选择") {
//                        backgroundResource = R.drawable.comm_photo_btn_bg
                        textColor = textColor1
                        textSize = DimensAdapter.textSpSize(CustomTSDimens.NORMAL)
                        gravity = Gravity.CENTER
                        verticalPadding = kIntHeight(0.015f)
                        onMyClick { doHandlerPhoto(PIC_FROM_LOCALPHOTO) }
                    }.lparams(MATCH_PARENT, WRAP_CONTENT) {
                        horizontalMargin = kIntWidth(0.04f)
                        topMargin = kIntHeight(0.02f)
                    }
                    view{ backgroundColor = textColor2 }.lparams(MATCH_PARENT,kIntHeight(0.01f))
                    cancelBtn = textView("取消") {
//                        backgroundResource = R.drawable.comm_yellow_btn
                        textColor = textColor1
                        textSize = DimensAdapter.textSpSize(CustomTSDimens.NORMAL)
                        gravity = Gravity.CENTER
                        verticalPadding = kIntHeight(0.015f)
                        onMyClick { dismiss() }
                    }.lparams(MATCH_PARENT, WRAP_CONTENT) {
                        horizontalMargin = kIntWidth(0.04f)
                        topMargin = kIntHeight(0.02f)
                        bottomMargin = kIntHeight(0.06f)
                    }
                }.lparams(MATCH_PARENT, WRAP_CONTENT) { alignParentBottom() }
            }
        }.view

        contentView = rootView
        width = kIntWidth(1f)
        height = kIntHeight(1f)
        isFocusable = true
        isOutsideTouchable = true
        setBackgroundDrawable(BitmapDrawable())
        inputMethodMode = PopupWindow.INPUT_METHOD_NEEDED
        softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        setOnDismissListener { backgroundAlpha(1f) }
    }

    fun setIsPNG(isPNG: Boolean) {
        this.isPNG = isPNG
    }

    fun show(parentView: View) {
        showAtLocation(parentView, Gravity.BOTTOM, 0, 0)
//        backgroundAlpha(0.5f)
    }

    /**
     * 设置添加屏幕的背景透明度
     * @param bgAlpha
     */
    fun backgroundAlpha(bgAlpha: Float) {
        val lp = hostAct.window.attributes
        lp.alpha = bgAlpha // 0.0-1.0
        hostAct.window.attributes = lp
    }

    /**
     * 根据不同方式选择图片设置ImageView

     * @param type 0-本地相册选择，非0为拍照
     */
    private fun doHandlerPhoto(type: Int) {
        try {
            // 保存裁剪后的图片文件
            val pictureFileDir = File(
                    Environment.getExternalStorageDirectory(), "/shrek_canteen")
            if (!pictureFileDir.exists()) {
                pictureFileDir.mkdirs()
            }
            val suffix= if (isPNG) "png" else "jpeg"
            val nameSuffix = if(randomFileName) System.currentTimeMillis().toString() else ""
            picFile = File(pictureFileDir, "upload${nameSuffix}.${suffix}")
            if (!(picFile?.exists()!!)) {
                picFile?.createNewFile()
            }
            photoUri = Uri.fromFile(picFile)

            if (type == PIC_FROM_LOCALPHOTO) {
                val intent = Intent(Intent.ACTION_PICK, null)
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
                hostAct.startActivityForResult(intent, PIC_FROM_LOCAL)
            } else {
                val cameraIntent = Intent(
                        MediaStore.ACTION_IMAGE_CAPTURE)
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                hostAct.startActivityForResult(cameraIntent, PIC_FROM_CAMERA)
            }

        } catch (e: Exception) {
            Log.i("HandlerPicError", "处理图片出现错误")
        }

    }

    /**
     * 启动裁剪
     */
    private fun cropImageUriByTakePhoto(mUri: Uri) {
        val intent = Intent("com.android.camera.action.CROP")
        intent.setDataAndType(mUri, "image/*")
        setIntentParams(intent)
        hostAct.startActivityForResult(intent, PIC_FROM_LOCALPHOTO)
    }

    /**
     * 设置公用参数
     */
    private fun setIntentParams(intent: Intent) {
        try {
            intent.putExtra("crop", "true")
            intent.putExtra("aspectX", 0.5)
            intent.putExtra("aspectY", 0.5)
//            intent.putExtra("outputX", 1000)
//            intent.putExtra("outputY", 1000)
            intent.putExtra("noFaceDetection", true) // no face detection
            intent.putExtra("scale", true)
            intent.putExtra("return-data", false)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            intent.putExtra("outputFormat", if (isPNG)
                Bitmap.CompressFormat.PNG.toString()
            else
                Bitmap.CompressFormat.JPEG.toString())

        } catch (e: ActivityNotFoundException) {
            val errorMessage = "Whoops - your device doesn't support the crop action!"
            hostAct.toastLongShow(errorMessage)
        }

    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            PIC_FROM_CAMERA ->// 拍照
                try {
                    cropImageUriByTakePhoto(photoUri!!)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                
            PIC_FROM_LOCAL -> {
                var uri: Uri? = null
                if (data == null) {
                    return
                }
                if (resultCode == Activity.RESULT_OK) {
                    if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
                        Toast.makeText(hostAct, "SD不可用", Toast.LENGTH_SHORT).show()
                        return
                    }
                    uri = data.data
                    cropImageUriByTakePhoto(uri)
                } else {
                    Toast.makeText(hostAct, "照片获取失败", Toast.LENGTH_SHORT).show()
                }
            }

            PIC_FROM_LOCALPHOTO ->
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        if (photoUri != null) {
                            val bitmap = decodeUriAsBitmap(hostAct, photoUri!!)
                            showImageView?.setImageBitmap(bitmap)
                            imgProcess?.invoke(picFile!!.path)
                            dismiss()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    Toast.makeText(hostAct, "您取消了操作", Toast.LENGTH_SHORT).show()
                }
        }
    }
}