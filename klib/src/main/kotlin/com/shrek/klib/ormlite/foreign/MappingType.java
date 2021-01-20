package com.shrek.klib.ormlite.foreign;

/**
 * 映射 关系
 * 
 * @author shrek
 *
 */
public enum MappingType {
	/**
	 * 无
	 */
	NONE,
	
	/**
	 * 一一对应
	 */
	ONE_TO_ONE,
	
	/**
	 * 多对一
	 */
	MANY_TO_ONE,
	
	/**
	 * 一对多
	 */
	ONE_TO_MANY,
	
	/**
	 * 多对多
	 */
	MANY_TO_MANY;
}
