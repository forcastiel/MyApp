package com.shrek.klib.retrofit;

import com.shrek.klib.colligate.StringUtils;
import com.shrek.klib.logger.ZLog;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author shrek
 * @date: 2016-04-17
 */
public class LogInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        ZLog.i(" 【 Request URL: "+request.url()+" 】");
        StringBuffer sb = new StringBuffer();
        Headers headers = request.headers();
        for (String key : headers.names()) {
            sb.append(String.format("[%s:%s]",key,headers.get(key)));
        }

        if (!StringUtils.isEmpty(sb.toString()))
        ZLog.i(" 【 Request Params: "+sb.toString()+" 】");

        if (request.body() != null) {
            ZLog.i(" 【 Request Body : "+request.body().toString()+" 】");
        }

        long beiginTime = System.nanoTime();
        Response response = chain.proceed(request);

        long endTime = System.nanoTime();
        //想看结果 断点 excute  response.body().string()
        ZLog.i(" 【 Response Body : "+response.body().toString()+" 】");
        ZLog.i(" 【 Cost Time : "+ Math.abs(endTime - beiginTime)/1e6+"ms 】");
        return response;
    }
}
