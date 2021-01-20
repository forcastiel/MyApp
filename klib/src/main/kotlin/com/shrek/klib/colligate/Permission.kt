package com.shrek.klib.colligate

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


/**
 * 权限验证 和 扩展
 * @author Shrek
 * @date:  2017-09-14
 */
class PermissionUtils {

}

enum class PermissionDescript(val pName:String,val abbDes:String ,val descript:String) {
    WRITE_STORAGE(Manifest.permission.WRITE_EXTERNAL_STORAGE,"文件存储权限","外部存储写入权限"),
    READ_STORAGE(Manifest.permission.READ_EXTERNAL_STORAGE,"文件读取权限","外部存储读取权限"),
    READ_PHONE_STATE(Manifest.permission.READ_PHONE_STATE,"读取手机信息权限","读取手机信息权限"),
    CAMERA(Manifest.permission.CAMERA,"访问照相机权限","访问照相机权限"),
    INTERNET(Manifest.permission.INTERNET,"访问网络权限","访问网络权限")
}


/**
 * 检测权限
 *
 * @return true：已授权； false：未授权；
 */
fun Context.checkPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}


/**
 * 检测多个权限
 * @return 未授权的权限
 */
fun Context.checkMorePermissions(vararg permissions: String): List<String> {
    return permissions.filter { checkPermission(it) }
}

//
fun Context.permissionDesc(permissions: List<String>): List<PermissionDescript> {
    val allPermissions = arrayListOf(PermissionDescript.WRITE_STORAGE)
    return allPermissions.filter { permissions.contains(it.pName) }
}

/**
 * 用户申请多个权限返回
 */
fun Activity.permissionsResult(requestCode: Int, permissions: Array<out String>,grantResults: IntArray, refuseCallback: (List<String>) -> Unit, neverRefuseCallback: (List<String>) -> Unit, passCallback: () -> Unit) {
    var needPermissionList = arrayListOf<String>()
    grantResults.forEachIndexed { index, i -> 
        if(i == PackageManager.PERMISSION_DENIED){ needPermissionList.add(permissions.get(index) )}
    }
    if( needPermissionList.isEmpty() ){
        passCallback()
        return 
    }
    
    var refuseList = arrayListOf<String>()
    var neverRefuseList = arrayListOf<String>()
    needPermissionList.forEach {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, it)) {
            refuseList.add(it)
        } else {
            neverRefuseList.add(it)
        }
    }

    if (refuseList.size > 0) {
        refuseCallback(refuseList)
    }
    if (neverRefuseList.size > 0) {
        neverRefuseCallback(neverRefuseList)
    }
}


/**
 * 跳转到权限设置界面
 */
fun Context.toAppSetting() {
    val intent = Intent()
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    if (Build.VERSION.SDK_INT >= 9) {
        intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS")
        intent.setData(Uri.fromParts("package", getPackageName(), null))
    } else if (Build.VERSION.SDK_INT <= 8) {
        intent.setAction(Intent.ACTION_VIEW)
        intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails")
        intent.putExtra("com.android.settings.ApplicationPkgName", getPackageName())
    }
    startActivity(intent)
}

