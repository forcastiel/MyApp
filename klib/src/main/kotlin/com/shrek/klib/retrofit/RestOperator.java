package com.shrek.klib.retrofit;

import android.content.Context;

import com.shrek.klib.ZSetting;
import com.shrek.klib.colligate.StringUtils;
import com.shrek.klib.file.FileOperator;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.fastjson.FastJsonConverterFactory;

/**
 * rest请求操作工具
 * @author shrek
 * @date: 2016-04-18
 */
public class RestOperator {

    Context context;

    boolean isLogger;

    String cachePath,baseUrl;

    String gsonDataFormat;

    private RestOperator(Context ctx, ZSetting setting) {
        super();
        this.context = ctx;

        this.isLogger = setting.isDebugMode();
        this.cachePath = setting.getRestCachePath();
        this.baseUrl = setting.getRestBaseUrl();
        this.gsonDataFormat = setting.getGsonTimeFormat();
    }


    public Retrofit build(){
//        Gson gson = new GsonBuilder()
//                .excludeFieldsWithoutExposeAnnotation()
//                .setPrettyPrinting()
//                .setDateFormat(gsonDataFormat).create();

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS);

        if (isLogger) {
            builder.addInterceptor(new LogInterceptor());
        }

        if (!StringUtils.isEmpty(cachePath)) {
            FileOperator fileOperator = new FileOperator(context, cachePath);
            okhttp3.Cache cache = new okhttp3.Cache(fileOperator.getOptFile(), 20 * 1024 * 1024);
            builder.cache(cache);
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
//                .addConverterFactory(GsonConverterFactory.create(gson))
                .addConverterFactory(FastJsonConverterFactory.create())
                .validateEagerly(true)
                .client(builder.build()).build();
        return retrofit;
    }


    public static <T> T createOpt(Context ctx, Class<T> clazz, ZSetting setting){
        return new RestOperator(ctx,setting).build().create(clazz);
    }
}
