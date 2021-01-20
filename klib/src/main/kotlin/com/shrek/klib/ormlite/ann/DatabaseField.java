package com.shrek.klib.ormlite.ann;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ java.lang.annotation.ElementType.FIELD })
public @interface DatabaseField {

	public static final int DEFAULT_MAX_FOREIGN_AUTO_REFRESH_LEVEL = 2;
	public static final String DEFAULT_STRING = "";
	public static final int NO_MAX_FOREIGN_AUTO_REFRESH_LEVEL_SPECIFIED = 255;

	boolean allowGeneratedIdInsert() default true;

	/**
	 * 是否可以为空
	 * 
	 * @return
	 */
	boolean canBeNull() default true;

//	String columnDefinition();

	/**
	 * 缺省的 字段名
	 * 
	 * @return
	 */
	String columnName() default "";

	/**
	 * 数据类型
	 * 
	 * @return
	 */
//	DataType dataType();

	/**
	 * 默认值
	 * 
	 * @return
	 */
	String defaultValue() default DEFAULT_STRING;
	
	/**
	 * 暂时用在日期的 格式化
	 * @return
	 */
	String format() default "";

	/**
	 * Whether the field is an auto-generated id field. Default is false.
	 */
	boolean generatedId() default false;

//	public String generatedIdSequence();

	/**
	 * Whether the field is the id field or not. Default is false. Only one
	 * field can have this set in a class.
	 * 支持联合主键
	 */
	boolean id() default false;
	
	/**
	 * 是否 创建 索引
	 * @return
	 */
	boolean index() default false;

//	public String indexName();

//	public int maxForeignAutoRefreshLevel();

	public boolean throwIfNull() default false;

	/**
	 * 是否唯一
	 * @return
	 */
	boolean unique() default false;

//	public String unknownEnumName();

	/**
	 * 是否试用 get set方法
	 * @return
	 */
	boolean useGetSet() default false;

	
//	public boolean version();

//	public int width();
}
