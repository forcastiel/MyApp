package com.shrek.klib.net;

public interface ZNetChangeObserver {
	/**
	 * 网络连接连接时调用
	 */
	public void onConnect(ZNetWorkUtil.NetType type);

	/**
	 * 当前没有网络连接
	 */
	public void onDisConnect();
}
