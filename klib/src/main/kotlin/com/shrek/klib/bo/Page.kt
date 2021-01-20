package com.shrek.klib.bo

/**
 * @author shrek
 * @date:  2018-11-28
 */
class Page {
    var pageIndex = 1
    var pageSize = 20
    var totalCount = 0

    //总页数
    val totalPage:Int
        get() {
            if (pageSize == 0){ return 1 }
            val yushu = totalCount % pageSize
            val value = totalCount / pageSize
            return if (pageSize == 0)  value else value + 1
        }

    //是否有更多
    val isMore:Boolean
         get() = totalCount > pageIndex * pageSize

}