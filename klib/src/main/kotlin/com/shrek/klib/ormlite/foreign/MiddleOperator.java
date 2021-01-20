package com.shrek.klib.ormlite.foreign;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.shrek.klib.colligate.ReflectUtils;
import com.shrek.klib.logger.ZLog;
import com.shrek.klib.ormlite.DBUtil;
import com.shrek.klib.ormlite.ForeignInfo;
import com.shrek.klib.ormlite.TableInfo;
import com.shrek.klib.ormlite.ZDBHelper;
import com.shrek.klib.ormlite.dao.DBDao;
import com.shrek.klib.ormlite.dao.DBTransforFactory;
import com.shrek.klib.ormlite.stmt.QueryBuilder;
import com.shrek.klib.ormlite.stmt.StmtBuilder;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

/**
 * 中间表操作类
 * 
 * @author shrek
 *
 */
public class MiddleOperator {
	private static final String TAG = "MiddleOperator";
	private ForeignInfo fInfo;

	public MiddleOperator(ForeignInfo fInfo) {
		super();
		this.fInfo = fInfo;
	}

	public long replace(SQLiteDatabase opt, ContentValues cvs) {
		opt.replace(fInfo.getMiddleTableName(), null, cvs);
		prinf(1);
		return 1;
	}

	/**
	 * 通过中间表 连接查询
	 * 
	 */
	public void joinSelect(ZDBHelper helper, List<?> allObject) {
//		SQLiteDatabase opt = helper.getDatabase(true);

//		StringBuffer sb = new StringBuffer(QueryBuilder.SELECT_KEYWORD
//				+ fInfo.getForeignColumnName() + " FROM "
//				+ fInfo.getMiddleTableName() + StmtBuilder.WHERE_KEYWORD
//				+ fInfo.getOriginalColumnName() + " = ?;");
//		ZWLogger.i(TAG, "执行中建表SQL 表名:" + fInfo.getMiddleTableName()
//				+ "，执行的SQL:" + sb.toString());
//
//		for (Object selectObj : allObject) {
//			Object orgFieldValue = ReflectUtil.getFieldValue(selectObj,
//					fInfo.getOriginalField());
//
//			Cursor middleCursor = opt.rawQuery(sb.toString(),
//					new String[] { orgFieldValue.toString() });
//			Class<?> fieldType = fInfo.getForeignField().getType();
//
//			List<Object> objs = new ArrayList<Object>();
//			while (middleCursor.moveToNext()) {
//				Object value = DBUtil.getColumnValueFromCoursor(middleCursor,
//						fInfo.getForeignColumnName(), fieldType);
//				if (value != null) {
//					objs.add(value);
//				}
//			}
//			middleCursor.close();
//
//			if (objs.size() == 0) {
//				continue;
//			}
//
//			DBDao dao = helper.getDao(fInfo.getForeignClazz());
//			QueryBuilder qBuilder = dao.queryBuilder();
//			qBuilder.in(fInfo.getForeignField().getName(), objs.toArray());
//			List result = dao.queryObjs(qBuilder);
//
//			if (result.size() == 0) {
//				continue;
//			}
//
//			Field valueField = fInfo.getValueField();
//			if (Collection.class.isAssignableFrom(valueField.getType())) {
//				ReflectUtil.setFieldValue(selectObj, valueField, result);
//			} else {
//				ReflectUtil.setFieldValue(selectObj, valueField, result.get(0));
//			}
//		}

		TableInfo targetInfo = TableInfo.newInstance(fInfo.getForeignClazz());

		String sql = QueryBuilder.SELECT_KEYWORD + "* FROM "
				+ targetInfo.getTableName() + " A LEFT JOIN "
				+ fInfo.getMiddleTableName() + " B on A."
				+ DBUtil.getColumnName(fInfo.getForeignField())+" = B."+ fInfo.getForeignColumnName()
				+ StmtBuilder.WHERE_KEYWORD + "B."+fInfo.getOriginalColumnName()
				+ " = ";
		
		DBDao dao = helper.getDao(fInfo.getForeignClazz());
		
		for (Object selectObj : allObject) {
			StringBuffer sqlBuffer = new StringBuffer(sql);
			Object orgFieldValue = ReflectUtils.getFieldValue(selectObj,
					fInfo.getOriginalField());
			sqlBuffer.append(DBTransforFactory.getColumnValue(orgFieldValue)+";");
			
			ZLog.i(TAG, "执行中建表SQL 表名:" + fInfo.getMiddleTableName()
					+ "，执行的SQL:" + sqlBuffer.toString());
			
			List result = dao.queryObjs(sqlBuffer.toString());

			if (result.size() == 0) {
				continue;
			}

			Field valueField = fInfo.getValueField();
			if (Collection.class.isAssignableFrom(valueField.getType())) {
				ReflectUtils.setFieldValue(selectObj, valueField, result);
			} else {
				ReflectUtils.setFieldValue(selectObj, valueField, result.get(0));
			}
		}
		
	}

	private void prinf(long num) {
		String mName = fInfo.getMiddleTableName();
		ZLog.i(TAG, "操作表名:" + mName + "，影响数据条数:" + num);
	}
}
