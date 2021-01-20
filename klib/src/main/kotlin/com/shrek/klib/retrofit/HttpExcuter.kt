package com.shrek.klib.retrofit

import com.shrek.klib.retrofit.handler.RestHandler
import retrofit2.Call

/**
 * @author Shrek
 * @date:  2017-04-11
 */
class HttpExcuter<Bo>(val retrofitCall:Call<Bo>,process: (HttpExcuter<Bo>.()->Unit)? = null ){
   
    var handler: RestHandler<Bo>? = null
    var postProcess:(Bo)->Unit = {}
    var wrapPostProcess:(Bo)->Unit = {}
    
    init {
        process?.invoke(this)
    }

    fun handler(handler:RestHandler<Bo>):HttpExcuter<Bo>{
        this.handler = handler
        return this
    }
    
}
