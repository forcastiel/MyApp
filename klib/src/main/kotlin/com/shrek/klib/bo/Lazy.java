package com.shrek.klib.bo;

/**
 * @author shrek
 * @date: 2016-04-01
 */
public class Lazy<T>  {

    Constructor<T> constructor;

    T instance;

    public Lazy(Constructor<T> constructor){
        super();
        this.constructor = constructor;
    }


    public T get(){
        if (instance == null) {
            instance = constructor.instance();
            constructor = null;
        }
        return instance;
    }


}
