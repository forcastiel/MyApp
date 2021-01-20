package com.shrek.klib.ormlite.dao;
/**
 * 类型中转 Field <----> Column
 * 1.基本类型正常中转
 * 2.boolean ----> int
 * 3.Date Calendar ----> int
 * 4.String ----> Text
 * @author shrek
 *
 */
public interface DBTransforDao<F,C> {
	
	/**
	 * 把Java的 转换为 数据库存储类型
	 * @param fieldObj
	 * @return
	 */
	public C parseFieldToColumn(F fieldObj);
	
	/**
	 * 数据库存储类型 转化 Java的
	 * @param columnObj
	 * @return
	 */
	public F parseColumnToField(C columnObj);
	
	/**
	 * 特殊清空
	 */
	public void specialDoing();
	
	/**
	 * 得到空值<空值不会插入数据库>
	 */
	public Object getFeildValueNull();
	
	/**
	 * 判断值是否为空
	 * @param f
	 * @return
	 */
	public boolean isFeildNullFeild(F f);
}
