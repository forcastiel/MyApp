package com.shrek.klib.retrofit;

/**
 * Rest容器
 * @author shrek
 * @date: 2016-05-06
 */
public interface RestContainer {

    void addRestExcuter(RestExcuter<?> excuter);

    void cancelAllRestExcuter();

    void removeRestExcuter(RestExcuter<?> excuter);
}
