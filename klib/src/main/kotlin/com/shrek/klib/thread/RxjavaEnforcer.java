package com.shrek.klib.thread;

/**
 * Rxandroid原理就是用handler
 * @author shrek
 * @date: 2016-04-04
 */
public class RxjavaEnforcer implements ZThreadEnforcer{

    @Override
    public void enforceMainThread(Runnable run) {

    }

    @Override
    public void enforceMainThreadDelay(Runnable run, long millisecond) {

    }

    @Override
    public void removeMainThread(Runnable run) {

    }

    @Override
    public void enforceBackgroud(Runnable run) {

    }

    @Override
    public void enforceBackgroudDelay(Runnable run, long millisecond) {

    }

    @Override
    public void removeBackgroud(Runnable run) {

    }

    @Override
    public void enforce(ThreadMode tMode, Runnable run) {

    }

    @Override
    public void enforceDelay(ThreadMode tMode, Runnable run, long millisecond) {

    }
}
