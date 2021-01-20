package com.shrek.klib.bo;

public interface Identity {
	/**
	 * 得到身份码  防止重复
	 * @return
	 */
	int getIdentityID();
	
	/**
	 * 销毁
	 */
	void recycle();
}
