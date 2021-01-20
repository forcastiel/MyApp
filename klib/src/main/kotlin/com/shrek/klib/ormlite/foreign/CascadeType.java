package com.shrek.klib.ormlite.foreign;

/**
 * 级联操作类型
 * @author shrek
 *
 */
public enum CascadeType {
	/**
	 * 级联新建
	 */
	PERSIST
	/**
	 * 级联删除
	 */
	, REMOVE
	/**
	 * 刷新 检测
	 */
	, REFRESH
	/**
	 * 级联更新
	 */
	, MERGE

	/**
	 * 表示选择全部四项
	 */
	, ALL;
}
