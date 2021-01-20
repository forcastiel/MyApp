package com.shrek.klib.ormlite;

import com.shrek.klib.colligate.ReflectUtils;
import com.shrek.klib.colligate.StringUtils;
import com.shrek.klib.ormlite.ann.Foreign;
import com.shrek.klib.ormlite.bo.ZDb;
import com.shrek.klib.ormlite.foreign.MiddleOperator;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 外键信息
 * @author shrek
 *
 */
public class ForeignInfo {
	/**
	 * 原表属性 和 对应的 新表的属性
	 */
	Field originalField,foreignField;
	/**
	 * 属性值 可能是集合 或者 实体 
	 */
	Field valueField;
	
	Class<? extends ZDb> originalClazz,foreignClazz;
	
	String middleTableName,originalColumnName,foreignColumnName;
	
	/**
	 * 触发器
	 */
	List<String> trigeerNameArr;
	
	MiddleOperator mMiddleOperator;
	
	public ForeignInfo(){
		super();
		trigeerNameArr = new ArrayList<String>();
	}

	public Field getOriginalField() {
		return originalField;
	}

	public void setOriginalField(Field originalField) {
		this.originalField = originalField;
	}

	public Field getForeignField() {
		return foreignField;
	}

	public void setForeignField(Field foreignField) {
		this.foreignField = foreignField;
	}

	public Class<? extends ZDb> getOriginalClazz() {
		return originalClazz;
	}

	public void setOriginalClazz(Class<? extends ZDb> originalClazz) {
		this.originalClazz = originalClazz;
	}

	public Class<? extends ZDb> getForeignClazz() {
		return foreignClazz;
	}

	public void setForeignClazz(Class<? extends ZDb> foreignClazz) {
		this.foreignClazz = foreignClazz;
	}

	public String getMiddleTableName() {
		return middleTableName;
	}

	public String getOriginalColumnName() {
		return originalColumnName;
	}

	public void setOriginalFieldName(String originalFieldName) {
		this.originalColumnName = originalFieldName;
	}

	public String getForeignColumnName() {
		return foreignColumnName;
	}

	public void setForeignColumnName(String foreignFieldName) {
		this.foreignColumnName = foreignFieldName;
	}

	public Field getValueField() {
		return valueField;
	}

	public MiddleOperator getmMiddleOperator() {
		return mMiddleOperator;
	}

	/**
	 * 初始化值
	 */
	public void initValue(){
		String tableName1 = originalClazz.getSimpleName().toUpperCase();
		String tableName2 = foreignClazz.getSimpleName().toUpperCase();

		StringBuffer sb = new StringBuffer(DBUtil.INTERMEDIATE_CONSTRAINT);
		if (tableName1.compareTo(tableName2) > 0) {
			sb.append(tableName1 + "_" + tableName2);
		} else {
			sb.append(tableName2 + "_" + tableName1);
		}
		
		middleTableName = sb.toString();
		
		originalColumnName = (tableName1.compareTo(tableName2) > 0?"O":"")+DBUtil.FK_CONSTRAINT+ tableName1 +"_"+originalField.getName();
		foreignColumnName = (tableName1.compareTo(tableName2) <= 0?"O":"")+DBUtil.FK_CONSTRAINT+ tableName2 +"_"+foreignField.getName();
		
		mMiddleOperator = new MiddleOperator(this);
	}
	
	/**
	 * 得到外键的注释
	 * @return
	 */
	public Foreign getForeignAnn(){
		return valueField.getAnnotation(Foreign.class);
	}
	
	/**
	 * 添加触发器的 名字
	 * @param trigger
	 */
	public void addTiggerName(String trigger){
		if(!StringUtils.isEmpty(trigger)){
			trigeerNameArr.add(trigger);
		}
	}
	
	/**
	 * 得到 原属性值
	 * @param host
	 * @return
	 */
	public Object getOriginalFieldValue(Object host){
		return ReflectUtils.getFieldValue(host, originalField);
	}
	
	
	/**
	 * 得到 指向的属性值
	 * @param host
	 * @return
	 */
	public Object getForeignFieldValue(Object host){
		return ReflectUtils.getFieldValue(host, foreignField);
	}
	
	
	@Override
	public int hashCode() {
		return middleTableName.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof ForeignInfo){
			ForeignInfo info = (ForeignInfo)o;
			return middleTableName.equals(info.middleTableName);
		}
		return super.equals(o);
	}
}
