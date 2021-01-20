package com.shrek.klib.ormlite.dao;

import com.shrek.klib.logger.ZLog;
import com.shrek.klib.ormlite.stmt.StmtBuilder;

import java.lang.ref.SoftReference;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class DBTransforFactory {
	public static SoftReference<HashMap<Class<?>, DBTransforDao<?, ?>>> allTransforMap;
	
//	static{
//		HashMap<Class<?>,DBTransforDao<?,?>> map = new HashMap<Class<?>, DBTransforDao<?,?>>();
//		map.put(Boolean.class, new BooleanTransfor());
//		map.put(boolean.class, new BooleanTransfor());
//		map.put(Date.class, new DateTransfor());
//		map.put(Calendar.class, new CalendarTransfor());
//		map.put(String.class, new StringTransfor());
//		allTransforMap = new SoftReference<HashMap<Class<?>,DBTransforDao<?,?>>>(map);
//	}
	
	/**
	 * 得到属性值 通过从数据库得到的值
	 * @param column
	 * @param clazzType
	 * @return
	 */
	public static Object getFieldValue(Object column,Class<?> clazzType){
		HashMap<Class<?>, DBTransforDao<?, ?>> map;
		try {
			map = getMap();
			DBTransforDao dao = map.get(clazzType);
			if(dao == null){
				if(int.class.isAssignableFrom(clazzType)
						|| Integer.class.isAssignableFrom(clazzType)){
					return column ==null?0:Integer.valueOf(column.toString());
				}else if(short.class.isAssignableFrom(clazzType)
						|| Short.class.isAssignableFrom(clazzType)){
					return column ==null?0:Short.valueOf(column.toString());
				}else if(Float.class.isAssignableFrom(clazzType)
						|| float.class.isAssignableFrom(clazzType)){
					return column ==null?0:Float.valueOf(column.toString());
				}else if(Double.class.isAssignableFrom(clazzType)
						|| double.class.isAssignableFrom(clazzType)){
					return column ==null?0:Double.valueOf(column.toString());
				}else if(Long.class.isAssignableFrom(clazzType)
						|| long.class.isAssignableFrom(clazzType)){
					return column ==null?0:Long.valueOf(column.toString());
				}
				return column;
			}
			return dao.parseColumnToField(column);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	public static Object getColumnValue(Object fieldValue){
		HashMap<Class<?>, DBTransforDao<?, ?>> map;
		if(fieldValue == null){
			return null;
		}
		try {
			map = getMap();
			DBTransforDao dao = map.get(fieldValue.getClass());
			if(dao == null){
				return fieldValue;
			}
			return dao.parseFieldToColumn(fieldValue);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static Object getFieldNullValue(Class<?> clazz){
		HashMap<Class<?>, DBTransforDao<?, ?>> map;
		try {
			map = getMap();
			DBTransforDao dao = map.get(clazz);
			if(dao == null){
				if(int.class.isAssignableFrom(clazz)
						|| Integer.class.isAssignableFrom(clazz)){
					return StmtBuilder.NULL_INTEGER;
				}else if(short.class.isAssignableFrom(clazz)
						|| Short.class.isAssignableFrom(clazz)){
					return StmtBuilder.NULL_SHORT;
				}else if(boolean.class.isAssignableFrom(clazz)){
					return false;
				}
			}else{
				return dao.getFeildValueNull();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * 判断 是不是 空值
	 * @param object
	 * @return
	 */
	public static boolean isFieldNullValue(Object object){
		if(object == null){
			return true;
		}
		HashMap<Class<?>, DBTransforDao<?, ?>> map;
		try {
			map = getMap();
			Class<?> objClazz = object.getClass();
			DBTransforDao dao = map.get(objClazz);
			if(dao == null){
				if(int.class.isAssignableFrom(objClazz)
						|| Integer.class.isAssignableFrom(objClazz)){
					return Integer.valueOf(object.toString()) == StmtBuilder.NULL_INTEGER;
				}else if(short.class.isAssignableFrom(objClazz)
						|| Short.class.isAssignableFrom(objClazz)){
					return Short.valueOf(object.toString()) == StmtBuilder.NULL_SHORT;
				}
			}else{
				return dao.isFeildNullFeild(object);
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}
	
	
	private static HashMap<Class<?>, DBTransforDao<?, ?>> getMap() throws Exception{
		HashMap<Class<?>, DBTransforDao<?, ?>> map = null;
		if(allTransforMap != null){
			map = allTransforMap.get();
		}
		if(map == null){
			ZLog.i("DBTransforFactory", "allTransforMap 已被回收!");
//			throw new Exception("allTransforMap 已被回收!");
			
			map = new HashMap<Class<?>, DBTransforDao<?,?>>();
			map.put(Boolean.class, new BooleanTransfor());
			map.put(boolean.class, new BooleanTransfor());
			map.put(Date.class, new DateTransfor());
			map.put(Calendar.class, new CalendarTransfor());
			map.put(String.class, new StringTransfor());
			
			allTransforMap = new SoftReference<HashMap<Class<?>,DBTransforDao<?,?>>>(map);
		}
		return map;
	}
}
