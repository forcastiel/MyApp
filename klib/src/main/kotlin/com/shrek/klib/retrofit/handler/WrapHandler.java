package com.shrek.klib.retrofit.handler;

/**
 * Handler包装类
 * @author shrek
 * @date: 2016-04-21
 */
public class WrapHandler<Bo> implements RestHandler<Bo>{

    RestHandler<Bo> byWrapHandler;

    public WrapHandler(RestHandler<Bo> byWrapHandler){
        super();
        if (byWrapHandler == null) {
            throw new NullPointerException("Wrap handler null!");
        }
        this.byWrapHandler = byWrapHandler;
    }

    @Override
    public void pre() {
        byWrapHandler.pre();
    }

    @Override
    public void post(Bo bo) {
        byWrapHandler.post(bo);
    }

    @Override
    public void error(Throwable throwable) {
        byWrapHandler.error(throwable);
    }
}
