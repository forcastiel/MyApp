package com.shrek.klib.ormlite.task;

import android.database.sqlite.SQLiteDatabase;

import com.shrek.klib.logger.ZLog;
import com.shrek.klib.ormlite.DBUtil;
import com.shrek.klib.ormlite.ZDBHelper;
import com.shrek.klib.ormlite.bo.ZDb;
import com.shrek.klib.ormlite.dao.DBDao;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * 数据库 执行器
 *
 * @author shrek
 * @date: 2016-04-22
 */
public class DBExcuter<DAO extends ZDb, RETURN> {

    DBDao<DAO> dao;

    DBOpt<DAO, RETURN> opt;

    //是否开启事物
    boolean isTransaction = false;

    boolean isAsync = false;

    Action1<RETURN> subDoing;

//    public static <T extends ZDb,F>DBExcuter create(DBDao<T> dao){
//        return new DBExcuter<T,F>(dao);
//    }

    public DBExcuter(DBDao<DAO> dao) {
        super();
        this.dao = dao;
    }

    public DBExcuter<DAO, RETURN> setTransaction(boolean transaction) {
        this.isTransaction = transaction;
        return this;
    }

    public DBExcuter<DAO, RETURN> setDoing(DBOpt<DAO, RETURN> opt) {
        this.opt = opt;
        return this;
    }

    public DBExcuter<DAO, RETURN> setAsync(boolean async) {
        this.isAsync = async;
        return this;
    }

    public DBExcuter<DAO, RETURN> setSubDoing(Action1<RETURN> subDoing) {
        this.subDoing = subDoing;
        return this;
    }

    public void excute() {
        Observable<RETURN> obsevable = Observable.create(new Observable.OnSubscribe<RETURN>() {

            @Override
            public void call(Subscriber<? super RETURN> subscriber) {
                RETURN retObj = null;

                ZDBHelper mHelper = dao.getHelper();
                mHelper.lockOperator();
                int optNum = 0;
                synchronized (ZDBHelper.LOCK_OBJ) {
                    long before = System.currentTimeMillis();
                    SQLiteDatabase db = mHelper.getDatabase(false);

                    if (isTransaction) {
                        db.beginTransaction();  //手动设置开始事务
                    }
                    try {
                        retObj = opt.opt(dao);
                        if (isTransaction) {
                            db.setTransactionSuccessful(); //处理完成
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        ZLog.i(this, "数据库操作失败 事务回滚!");
                    } finally {
                        if (isTransaction) {
                            db.endTransaction(); //处理完成
                        }
                    }
                    long after = System.currentTimeMillis();
                    DBUtil.timeCompute(before, after);
                }

                mHelper.unLockOperator();

                subscriber.onNext(retObj);
                subscriber.onCompleted();
            }
        });


        obsevable.subscribeOn(isAsync ? Schedulers.computation() : Schedulers.immediate())
                .observeOn(isAsync ? AndroidSchedulers.mainThread() : Schedulers.immediate())
                .subscribe(subDoing);
    }

    public static interface DBOpt<DAO extends ZDb, RETURN> {

        RETURN opt(DBDao<DAO> optObj);

    }
}
