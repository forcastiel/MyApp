package com.shrek.klib.retrofit;

import rx.Observable;

/**
 * @author shrek
 * @date: 2016-04-18
 */
public class ZCall extends Observable {

    public ZCall(OnSubscribe f) {
        super(f);
    }
}
