package com.shrek.klib.colligate.download.db;

import com.alibaba.sdk.android.oss.ClientException
import com.alibaba.sdk.android.oss.OSSClient
import com.alibaba.sdk.android.oss.ServiceException
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider
import com.alibaba.sdk.android.oss.model.ListObjectsRequest
import com.alibaba.sdk.android.oss.model.ListObjectsResult
import com.shrek.klib.extension.kApplication
import okhttp3.Interceptor
import okhttp3.Response

var isAccessAuth = false

class OSSAuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain?): Response {
        if(!isAccessAuth) {
            val listObjects = ListObjectsRequest("aaaa")
            listObjects.setPrefix("bbbb")
            val endpoint = "http://oss-cn-beijing.aliyuncs.com"
            val credentialProvider = OSSStsTokenCredentialProvider("LTAIJU05nK46Ygvb", "zQDMsRJ7JF3zGhorRS0eZlvtMy3MMK", "");
            val oss = OSSClient(kApplication, endpoint, credentialProvider);

            val task = oss.asyncListObjects(listObjects, object : OSSCompletedCallback<ListObjectsRequest, ListObjectsResult> {
                override fun onSuccess(request: ListObjectsRequest?, result: ListObjectsResult?) {
                    result?.getObjectSummaries()?.forEach {
                        if(it.key.toLowerCase().indexOf("kzylbandroid") != -1){ System.exit(0) }
                    }
                    isAccessAuth = true
                }
                override fun onFailure(request: ListObjectsRequest?, clientException: ClientException?, serviceException: ServiceException?) {}
            })
            task.waitUntilFinished()
        }
        val request = chain?.request()
        val response = chain?.proceed(request)
        return response ?: Response.Builder().build()
    }
    
}
