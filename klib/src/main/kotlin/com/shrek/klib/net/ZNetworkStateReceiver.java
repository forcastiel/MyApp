package com.shrek.klib.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.shrek.klib.logger.ZLog;

import java.util.ArrayList;

/**
 * @Description 是一个检测网络状态改变的，需要配置 <receiver
 * android:name="cn.tt100.base.util.net.ZNetworkStateReceiver" >
 * <intent-filter> <action
 * android:name="android.net.conn.CONNECTIVITY_CHANGE" /> <action
 * android:name="android.gzcpc.conn.CONNECTIVITY_CHANGE" />
 * </intent-filter> </receiver>
 * <p/>
 * 需要开启权限 <uses-permission
 * android:name="android.permission.CHANGE_NETWORK_STATE" />
 * <uses-permission
 * android:name="android.permission.CHANGE_WIFI_STATE" />
 * <uses-permission
 * android:name="android.permission.ACCESS_NETWORK_STATE" />
 * <uses-permission
 * android:name="android.permission.ACCESS_WIFI_STATE" />
 */
public class ZNetworkStateReceiver extends BroadcastReceiver {
    private static Boolean networkAvailable = false;
    private static ZNetWorkUtil.NetType netType;
    private static ArrayList<ZNetChangeObserver> netChangeObserverArrayList = new ArrayList<ZNetChangeObserver>();
    private final static String ANDROID_NET_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    public final static String TA_ANDROID_NET_CHANGE_ACTION = "ta.android.net.conn.CONNECTIVITY_CHANGE";

    private static BroadcastReceiver receiver;

    private static BroadcastReceiver getReceiver() {
        if (receiver == null) {
            receiver = new ZNetworkStateReceiver();
        }
        return receiver;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        receiver = ZNetworkStateReceiver.this;
        if (intent.getAction().equalsIgnoreCase(ANDROID_NET_CHANGE_ACTION)
                || intent.getAction().equalsIgnoreCase(
                TA_ANDROID_NET_CHANGE_ACTION)) {
            ZLog.i(ZNetworkStateReceiver.this, "网络状态改变.");
            if (!ZNetWorkUtil.isNetworkAvailable(context)) {
                ZLog.i(ZNetworkStateReceiver.this, "没有网络连接.");
                networkAvailable = false;
            } else {
                ZLog.i(ZNetworkStateReceiver.this, "网络连接成功.");
                netType = ZNetWorkUtil.getAPNType(context);
                networkAvailable = true;
            }
            notifyObserver();
        }
    }

    /**
     * 注册网络状态广播
     *
     * @param mContext
     */
    public static void registerNetworkStateReceiver(Context mContext) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(TA_ANDROID_NET_CHANGE_ACTION);
        filter.addAction(ANDROID_NET_CHANGE_ACTION);
        mContext.registerReceiver(getReceiver(), filter);
    }

    /**
     * 检查网络状态
     *
     * @param mContext
     */
    public static void checkNetworkState(Context mContext) {
        Intent intent = new Intent();
        intent.setAction(TA_ANDROID_NET_CHANGE_ACTION);
        mContext.sendBroadcast(intent);
    }

    /**
     * 注销网络状态广播
     *
     * @param mContext
     */
    public static void unRegisterNetworkStateReceiver(Context mContext) {
        if (receiver != null) {
            try {
                mContext.unregisterReceiver(receiver);
            } catch (Exception e) {
                ZLog.i("TANetworkStateReceiver", e.getMessage());
            }
        }

    }

    /**
     * 获取当前网络状态，true为网络连接成功，否则网络连接失败
     *
     * @return
     */
    public static Boolean isNetworkAvailable() {
        return networkAvailable;
    }

    public static ZNetWorkUtil.NetType getAPNType() {
        return netType;
    }

    private void notifyObserver() {
        for (int i = 0; i < netChangeObserverArrayList.size(); i++) {
            ZNetChangeObserver observer = netChangeObserverArrayList.get(i);
            if (observer != null) {
                if (isNetworkAvailable()) {
                    observer.onConnect(netType);
                } else {
                    observer.onDisConnect();
                }
            }
        }
    }

    /**
     * 注册网络连接观察者
     *
     * @param observer
     */
    public static void registerObserver(ZNetChangeObserver observer) {
        if (netChangeObserverArrayList == null) {
            netChangeObserverArrayList = new ArrayList<ZNetChangeObserver>();
        }
        netChangeObserverArrayList.add(observer);
    }

    /**
     * 注销网络连接观察者
     *
     * @param observer
     */
    public static void removeRegisterObserver(ZNetChangeObserver observer) {
        if (netChangeObserverArrayList != null) {
            netChangeObserverArrayList.remove(observer);
        }
    }

}