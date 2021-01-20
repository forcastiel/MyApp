package com.shrek.klib.ormlite;

/**
 * sqlite 数据库类型
 * @author shrek
 *
 */
public enum SqliteColumnType {
	BLOB,INTEGER,NULL,REAL,TEXT,
	/**
	 * 时间类型 2013-01-07 02:30:20
	 */
	TIMESTAMP,
	/**
	 * 日期类型 2013-01-07
	 */
	DATE,
	/**
	 * 时间类型 02:30:20
	 */
	TIME;
}
