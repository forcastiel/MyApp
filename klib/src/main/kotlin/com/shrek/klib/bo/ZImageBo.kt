package com.shrek.klib.bo

import com.squareup.picasso.Transformation

/**
 * @author shrek
 * @date:  2016-05-25
 */
data class KImgBo(var imgUrl: String
                    , var errorImg: Int = -1
                    , var placeHolder: Int = -1
                    , var sizeWidth: Int = 0
                    , var sizeHeight: Int = 0
                    , var isCenterCrop: Boolean = false
                    , var transformation:Transformation? = null)