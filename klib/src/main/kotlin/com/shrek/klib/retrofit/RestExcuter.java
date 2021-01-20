package com.shrek.klib.retrofit;

import androidx.annotation.NonNull;

import com.shrek.klib.logger.ZLog;
import com.shrek.klib.retrofit.handler.NothingHandler;
import com.shrek.klib.retrofit.handler.RestHandler;
import com.shrek.klib.thread.HandlerEnforcer;

import java.util.concurrent.atomic.AtomicBoolean;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * @author shrek
 * @date: 2016-04-21
 */
public class RestExcuter<Bo> {

    private Observable<Bo> observable;

    //处理器
    public RestHandler<Bo> handler;

    Runnable preDoing;

    //wrapPostDoing 包装 请求后做什么
    Action1<Bo> postDoing,wrapPostDoing;

    //post的包装
    public Action1<Throwable> errorDoing;

    WrapAction<Bo> wrapAction;

    WrapAction<Throwable> wrapError;

    RestContainer container;

    //任务是否取消
    AtomicBoolean isCancel;
    //任务是否取消
    AtomicBoolean isRetry;

    public static <T> RestExcuter<T> create(Observable<T> observable){
        return new RestExcuter(observable);
    }

    protected RestExcuter(@NonNull Observable<Bo> observable) {
        super();
        if (observable == null) {
            throw new NullPointerException("Observable null !");
        }
        this.observable = observable;
        this.isCancel = new AtomicBoolean(false);
        this.isRetry = new AtomicBoolean(false);
    }

    /**
     * 设置处理器
     * @param handler
     * @return
     */
    public RestExcuter<Bo> handler(RestHandler<Bo> handler){
        if (handler == null) {
            throw new NullPointerException("RestHandler null !");
        }
        this.handler = handler;
        return this;
    }

    /**
     * 请求前做什么
     * @return
     */
    public RestExcuter<Bo> pre(Runnable preDoing){
        this.preDoing = preDoing;
        return this;
    }

    /**
     * 中途做什么
     * @param postDoing
     * @return
     */
    public RestExcuter<Bo> post(Action1<Bo> postDoing){
        if (postDoing == null) {
            throw new NullPointerException("Post Doing null !");
        }
        this.postDoing = postDoing;
        return this;
    }

    /**
     * 给业务层 做的方法
     * @param postDoing
     * @return
     */
    public RestExcuter<Bo> wrapPost(Action1<Bo> postDoing){
        if (postDoing == null) {
            throw new NullPointerException("Post Doing null !");
        }
        this.wrapPostDoing = postDoing;
        return this;
    }

    /**
     * 这是包装方法
     * @param wrapAction
     * @return
     */
    public RestExcuter<Bo> wrapPost(WrapAction<Bo> wrapAction){
        if (wrapAction == null) {
            throw new NullPointerException("Wrap Doing null !");
        }
        this.wrapAction = wrapAction;
        return this;
    }

    /**
     * 出错做什么
     * @return
     */
    public RestExcuter<Bo> error(Action1<Throwable> errDoing){
        if (errDoing == null) {
            throw new NullPointerException("Error Doing null !");
        }
        this.errorDoing = errDoing;
        return this;
    }

    public RestExcuter<Bo> wrapError(WrapAction<Throwable> wrapError){
        if (wrapError == null) {
            throw new NullPointerException("Wrap error doing null !");
        }
        this.wrapError = wrapError;
        return this;
    }

    /**
     * 设置rest容器
     * @param container
     * @return
     */
    public RestExcuter<Bo> setContainer(@NonNull RestContainer container) {
        this.container = container;
        if(container != null){
            container.addRestExcuter(this);
        }
        return this;
    }

    public RestContainer getContainer() {
        return container;
    }

    public void excute(){ excute(null);}

    public void excute(RestContainer container) {
        setContainer(container);
        if (handler == null) {
            ZLog.i("Invalid rest handler, use default <NothingHandler>");
            handler = new NothingHandler<Bo>();
//            return;
        }

        if (isCancel()) {
            return;
        }

        HandlerEnforcer.newInstance().enforceMainThread(new Runnable() {
            @Override
            public void run() {
                if (isCancel()) {
                    return;
                }
                handler.pre();
                if (preDoing != null ) {
                    preDoing.run();
                }
            }
        });

        Action1<Bo> onNext = new Action1<Bo>() {
            @Override
            public void call(Bo bo) {

                if (isCancel()) {
                    return;
                }

                if(wrapAction != null){
                    wrapAction.setAction(postDoing);
                    wrapAction.call(bo);
                } else {
                    
                    if(wrapPostDoing != null) {
                        wrapPostDoing.call(bo);
                    }
                    
                    if(postDoing != null){
                        postDoing.call(bo);
                    }
                }

                if (!isCancel()) {
                    handler.post(bo);
                }

                removeExcuter();
            }
        };

        Action1<Throwable> onError = new Action1<Throwable>() {
            @Override
            public void call(final Throwable throwable) {


                if (isCancel()) {
                    return;
                }
    
                HandlerEnforcer.newInstance().enforceMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if(wrapError != null){
                            wrapError.setAction(errorDoing);
                            wrapError.call(throwable);
                        } else {
                            if(errorDoing != null){
                                errorDoing.call(throwable);
                            }
                        }

                        if (!isCancel()) {
                            handler.error(throwable);
                        }

                        removeExcuter();
                    }
                });
                
            }
        };

        observable.observeOn(AndroidSchedulers.mainThread()).subscribe(onNext, onError);
    }


    boolean isCancel(){
        boolean result = isCancel.get();
        if (result) {
            ZLog.i("任务已经被取消!");

//            if (errorDoing != null) {
//                errorDoing.call(new RestCancelException());
//            }
            handler.post(null);
        }
        return result;
    }

    public void retry(){
        isRetry.set(true);
    }

    public void setCancel(Boolean value){ isCancel.set(value); }


    //如果需要重试  不需要清除数据
    private void removeExcuter(){
        if (isRetry.get()){
            isRetry.set(true);
            return;
        }
        if(container != null){
            container.removeRestExcuter(this);
            container = null;
            cancel();
        }
        handler = null;
        postDoing = null;
        errorDoing = null;
        observable = null;
        preDoing = null;
        wrapPostDoing = null;
        wrapAction = null;
        wrapError = null;
        container = null;
    }

    /**
     * 取消任务
     */
    public void cancel(){
        isCancel.compareAndSet(false,true);
    }


    public static class WrapAction<Bo> {

        Action1<Bo> action;

        public WrapAction(){
            super();
        }

        public WrapAction(Action1<Bo> action){
            this.action = action;
        }

        public void setAction(Action1<Bo> action) {
            this.action = action;
        }

        public void call(Bo bo) {
            action.call(bo);
        }

    }
}
