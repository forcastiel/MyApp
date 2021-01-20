package com.shrek.klib.colligate.cahce

import com.shrek.klib.extension.kAppSetting
import com.shrek.klib.extension.kApplication

/**
 * 对Zcache的扩展
 * @author shrek
 * @date:  2016-05-31
 */
class KCache(zCache: ZCache) : CacheDao by zCache{

//    val cacheEngine:ZCache by lazy {
//        ZCache.get(application,appSetting.cachePath)
//    }
    companion object {

        fun instance():KCache {
           val cache =  ZCache.get(kApplication,kAppSetting.cachePath)
            return KCache(cache)
        }
    }

}
