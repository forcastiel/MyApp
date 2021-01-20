package com.shrek.klib.ui

import android.graphics.Color
import com.shrek.klib.view.adaptation.CustomTSDimens
import com.shrek.klib.view.adaptation.DimensAdapter

/**
 * @author Shrek
 * @date:  2017-03-16
 */
object CommonUiSetup {
    //主色调
    var pramaryColor:Int = 0
    var textColor = Color.parseColor("#1a1a1a")
    var grayTextColor = Color.parseColor("#a01a1a1a")
    
    var textSize = DimensAdapter.textSpSize(CustomTSDimens.SLIGHTLY_SMALL)
    
}