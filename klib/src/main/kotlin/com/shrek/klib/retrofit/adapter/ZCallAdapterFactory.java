package com.shrek.klib.retrofit.adapter;

import com.shrek.klib.retrofit.ZCall;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import rx.Observable;
import rx.Scheduler;
import rx.schedulers.Schedulers;

/**
 * Call 适配器
 *
 * @author shrek
 * @date: 2016-04-18
 */
public class ZCallAdapterFactory extends CallAdapter.Factory {

    public ZCallAdapterFactory() {
        super();
    }

    @Override
    public CallAdapter<?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
        Class<?> rawType = getRawType(returnType);
        if (rawType != ZCall.class) {
            RxJavaCallAdapterFactory factory = RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io());
            return factory.get(returnType, annotations, retrofit);
        }
        return null;
    }


    static final class ResponseCallAdapter implements CallAdapter<Observable<?>> {
        private final Type responseType;
        private final Scheduler scheduler;

        ResponseCallAdapter(Type responseType, Scheduler scheduler) {
            this.responseType = responseType;
            this.scheduler = scheduler;
        }

        @Override
        public Type responseType() {
            return responseType;
        }

        @Override
        public <R> Observable<Response<R>> adapt(Call<R> call) {
            return null;
        }
    }
}
