package com.shrek.klib.retrofit.handler;

/**
 * @author shrek
 * @date: 2016-04-21
 */
public class NothingHandler<Bo> implements RestHandler<Bo>{

    @Override
    public final void pre() {

    }

    @Override
    public void error(Throwable throwable) {

    }

    @Override
    public void post(Bo bo) {
        
    }
}
