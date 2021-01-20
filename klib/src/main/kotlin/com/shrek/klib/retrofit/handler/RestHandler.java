package com.shrek.klib.retrofit.handler;

/**
 * @author shrek
 * @date: 2016-04-21
 */
public interface RestHandler<RestBo> {

    /**
     * 前面做什么
     */
    void pre();

    /**
     * 完成做什么
     * @param bo
     */
    void post(RestBo bo);

    /**
     * 异常做什么
     * @param throwable
     */
    void error(Throwable throwable);

}
