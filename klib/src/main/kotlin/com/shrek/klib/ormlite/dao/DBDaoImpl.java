package com.shrek.klib.ormlite.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.shrek.klib.colligate.ReflectUtils;
import com.shrek.klib.ormlite.DBUtil;
import com.shrek.klib.ormlite.ForeignInfo;
import com.shrek.klib.ormlite.TableInfo;
import com.shrek.klib.ormlite.ZDBHelper;
import com.shrek.klib.ormlite.bo.ZDb;
import com.shrek.klib.ormlite.stmt.DeleteBuider;
import com.shrek.klib.ormlite.stmt.InsertBuider;
import com.shrek.klib.ormlite.stmt.QueryBuilder;
import com.shrek.klib.ormlite.stmt.UpdateBuider;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DBDaoImpl<T extends ZDb> implements DBDao<T> {
	private Class<T> clazz;
	private ZDBHelper helper;

	public DBDaoImpl(Class<T> clazz1, ZDBHelper helper) {
		this.clazz = clazz1;
		this.helper = helper;
	}

	@Override
	public ZDBHelper getHelper(){
		return helper;
	}

	@Override
	public InsertBuider<T> insertBuider() {
		InsertBuider<T> builder = new InsertBuider<T>(clazz);
		return builder;
	}

	@Override
	public long insertObj(T t) {
		return insertObj(false, t);
	}

	@Override
	public long insertObj(boolean isAddFKObject, T t) {
		return insertObjs(isAddFKObject, t);
	}

	@Override
	public long insertObjs(Collection<T> t) {
		// T[] ts = (T[]) t.toArray();
		return insertObjs(false, t);
	}

	public long insertObjs(boolean isAddFKObject, T... t) {
		List<T> list = new ArrayList<T>();
		for (T obj : t) {
			list.add(obj);
		}
		return insertObjs(isAddFKObject, list);
	}

	// @Override
	// public long insertOrUpdateObjs(Collection<T> t) {
	// return insertObjs(false, t);
	// }
	//
	// @Override
	// public long insertOrUpdateObjs(T... t) {
	// return insertObjs(false, t);
	// }

	@Override
	public long insertObjs(boolean isAddFKObject, Collection<T> collectT) {
		InsertBuider<T> buider = insertBuider();
		int optNum = 0;

		if (isAddFKObject) {
			Set<Object> allFKs = new HashSet<Object>();

			for (T obj : collectT) {
				// 先插入外键值
				Set<Object> objs = buider.getForeignKeyObjs(obj);
				allFKs.addAll(objs);
			}

			// ZWLogger.i(this,"获取外键的对象数量："+ allFKs.size());

			// 插入 对应外表值
			for (Object fkObject : allFKs) {
				if (fkObject instanceof ZDb) {
					ZDb bo = (ZDb) fkObject;
					Class<? extends ZDb> fkClazz = bo.getClass();
					DBDao dao = helper.getDao(fkClazz);
					try {
						optNum += dao.insertObj(true, bo);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}

		SQLiteDatabase sd = helper.getDatabase(false);

		for (T obj : collectT) {
			try {
				sd.insertWithOnConflict(buider.tableInfo.getTableName(), null,
						buider.getContentValue(obj),
						SQLiteDatabase.CONFLICT_NONE);
				optNum++;

				if (isAddFKObject) {
					// 插入 中建表
					Map<ForeignInfo, List<ContentValues>> map = buider
							.getForgienValue(obj);
					for (Map.Entry<ForeignInfo, List<ContentValues>> entry : map
							.entrySet()) {
						ForeignInfo info = entry.getKey();
						List<ContentValues> cvses = entry.getValue();
						for (ContentValues cvs : cvses) {
							optNum += info.getmMiddleOperator()
									.replace(sd, cvs);
						}
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return optNum;
	}

	@Override
	public long insertObjs(T... t) {
		// TODO Auto-generated method stub
		List<T> list = new ArrayList<T>();
		for (T obj : t) {
			list.add(obj);
		}
		return insertObjs(list);
		// int optNum = 0;
		// for(T obj : t){
		// InsertBuider<T> buider = insertBuider();
		// buider.addValue(obj);
		// helper.getDatabase(false).insert(buider.tableInfo.tableName, null,
		// buider.cvs);
		// optNum++;
		// }
		// return optNum;
	}

	/** -------------------删除-------------------------- */
	@Override
	public DeleteBuider deleteBuider() {
		return new DeleteBuider(clazz);
	}

	@Override
	public long deleteObjs(DeleteBuider builder) {
		long optNum = helper.getDatabase(false).delete(
				builder.getTableNameWithAliases(), builder.getWhereSql(), null);
		return optNum;
	}

	@Override
	public long deleteAll() {
		// TODO Auto-generated method stub
		// DeleteBuider builder = deleteBuider();
		return deleteAll(false);
	}

	/**
	 * 是否删除中建表
	 * 
	 * @param isDelMiddle
	 * @return
	 */
	public long deleteAll(boolean isDelMiddle) {
		DeleteBuider builder = deleteBuider();
		if (isDelMiddle) {
			List<ForeignInfo> fInfos = builder.tableInfo.allforeignInfos;
			for (ForeignInfo info : fInfos) {
				helper.getDatabase(false).delete(info.getMiddleTableName(),
						null, null);
			}
		}
		return deleteObjs(builder);
	}

	/**
	 * 不推荐试用
	 * 
	 * @param whereSql
	 *            条件的sql的语句
	 */
	@Override
	public long deleteObj(String whereSql) {
		TableInfo info = TableInfo.newInstance(clazz);
		long optNum = helper.getDatabase(false).delete(info.getTableName(),
				whereSql, null);
		return optNum;
	}

	@Override
	public void clearObj(T t) {
		TableInfo info = TableInfo.newInstance(clazz);
		for (int j = 0; j < info.allField.size(); j++) {
			Field field = info.allField.get(j);
			Class<?> typeClazz = info.getFieldType(j);
			Object objValue = DBTransforFactory.getFieldNullValue(typeClazz);
			ReflectUtils.setFieldValue(t, field, objValue);
		}
	}

	/** ------------------- 更新 ----------------------- */
	@Override
	public UpdateBuider<T> updateBuider() {
		return new UpdateBuider<T>(clazz);
	}

	@Override
	public long updateObj(T t) {
		UpdateBuider<T> updateBuider = updateBuider();
		updateBuider.addValue(t);
		return updateObj(updateBuider);
	}

	@Override
	public long updateObj(UpdateBuider<T> mUpdateBuider) {
		return helper.getDatabase(false).update(
				mUpdateBuider.tableInfo.getTableName(), mUpdateBuider.cvs,
				mUpdateBuider.getWhereSql(), null);
	}

	@Override
	public long updateObj(Map<String, Object> updateMap) {
		UpdateBuider<T> updateBuider = updateBuider();
		updateBuider.addValue(updateMap);
		return updateObj(updateBuider);
	}

	/**
	 * 替换 对象
	 */
	@Override
	public long replaceObj(T t) {
		UpdateBuider<T> updateBuider = updateBuider();
		updateBuider.addValue(t);
		return replaceObj(updateBuider);
	}

	@Override
	public long replaceObj(UpdateBuider<T> mUpdateBuider) {
		helper.getDatabase(false)
				.replace(mUpdateBuider.tableInfo.getTableName(), null,
						mUpdateBuider.cvs);
		return 1;
	}

	@Override
	public long replaceObj(Map<String, Object> updateMap) {
		UpdateBuider<T> updateBuider = updateBuider();
		updateBuider.addValue(updateMap);
		return replaceObj(updateBuider);
	}

	@Override
	public long replaceObjs(Collection<T> ts) {
		long sum = 0;
		for (T obj : ts) {
			sum += replaceObj(obj);
		}
		return sum;
	}

	@Override
	public long replaceObjs(T... ts) {
		List<T> list = new ArrayList<T>();
		for (T obj : ts) {
			list.add(obj);
		}
		return replaceObjs(list);
	}

	@Override
	public QueryBuilder queryBuilder() {
		return new QueryBuilder(clazz);
	}

	@Override
	public List<T> queryAllObjs() {
		QueryBuilder mQueryBuilder = queryBuilder();
		mQueryBuilder.addSelectColumn("*");
		return queryObjs(mQueryBuilder);
	}

	@Override
	public List<T> queryJoinAllObjs() {
		QueryBuilder mQueryBuilder = queryBuilder();
		mQueryBuilder.addSelectColumn("*");
		return queryJoinObjs(mQueryBuilder);
	}

	@Override
	public List<T> queryObjs(QueryBuilder mQueryBuilder) {
		String sql = mQueryBuilder.getSql();
		mQueryBuilder.cycle();
		return queryObjs(false, sql, null);
	}

	/**
	 * 连接主要是左连接查询 例如Employee查询的时候 顺便把所属公司Company 查出来 1.得到所有的外键
	 */
	@Override
	public List<T> queryJoinObjs(QueryBuilder mQueryBuilder) {
		String sql = mQueryBuilder.getSql();
		// 得到所有外键
		List<ForeignInfo> queryColumns = mQueryBuilder.getQueryFkColumns();
		mQueryBuilder.cycle();
		return queryObjs(true, sql, queryColumns);
	}

	@Override
	public List<T> queryObjs(String sql) {
		return queryObjs(false, sql, null);
	}

	@Override
	public List<T> queryJoinObjs(String sql, String... fkParas) {
		// 通过fk 找foreign
		TableInfo info = TableInfo.newInstance(clazz);
		List<ForeignInfo> fkInfos = new ArrayList<ForeignInfo>();
		for (String fkName : fkParas) {
			ForeignInfo fi = info.getForeign(fkName);
			if (fi != null) {
				fkInfos.add(fi);
			}
		}
		return queryObjs(true, sql, fkInfos);
	}

	/**
	 * 查询
	 * 
	 * @param isJoin
	 *            是否连接查询
	 * @param sql
	 * @param fkParas
	 *            查哪些关联的信息 如果为null & isJoin = true 查全部
	 * @return
	 */
	private List<T> queryObjs(boolean isJoin, String sql,
			List<ForeignInfo> fkParas) {
		List<T> list = new ArrayList<T>();
		Cursor cursor = helper.getDatabase(true).rawQuery(sql, null);
		while (cursor.moveToNext()) {
			T t = parseCurser(cursor);
			if (t == null) {
				continue;
			}
			list.add(t);
		}
		cursor.close();

		// 连接查询
		if (isJoin) {
			TableInfo info = TableInfo.newInstance(clazz);
			List<ForeignInfo> fInfos = null;

			if (fkParas != null && fkParas.size() > 0) {
				fInfos = new ArrayList<ForeignInfo>();
				// for(String fkName : fkParas){
				// ForeignInfo fInfo = info.getForeign(fkName);
				// if(fInfo != null){
				// fInfos.add(fInfo);
				// }
				// }
				fInfos = fkParas;
			} else {
				fInfos = info.allforeignInfos;
			}

			for (ForeignInfo fInfo : fInfos) {
				fInfo.getmMiddleOperator().joinSelect(helper, list);
			}
		}

		return list;
	}

	@Override
	public T queryFirstObj(QueryBuilder mQueryBuilder) {
		// TODO Auto-generated method stub
		mQueryBuilder.limitIndex = 1;
		mQueryBuilder.offsetIndex = 0;
		List<T> list = queryObjs(mQueryBuilder);
		if (list.size() > 0) {
			return list.get(0);
		}
		return null;
	}

	@Override
	public T queryJoinFirstObj(QueryBuilder mQueryBuilder) {
		// TODO Auto-generated method stub
		mQueryBuilder.limitIndex = 1;
		mQueryBuilder.offsetIndex = 0;
		List<T> list = queryJoinObjs(mQueryBuilder);
		if (list.size() > 0) {
			return list.get(0);
		}
		return null;
	}

	@Override
	public int queryCount(QueryBuilder mQueryBuilder) {
		mQueryBuilder.clearSelectSection();
		mQueryBuilder.limitIndex = -1;
		mQueryBuilder.offsetIndex = -1;
		mQueryBuilder.addSelectColumn("COUNT(id)");
		Cursor cursor = helper.getDatabase(true).rawQuery(
				mQueryBuilder.getSql(), null);
		mQueryBuilder.cycle();
		int countNum = 0;
		if (cursor.moveToNext()) {
			countNum = cursor.getInt(0);
		}
		cursor.close();
		return countNum;
	}

	public T parseCurser(Cursor cursor) {
		return DBUtil.parseCurser(cursor, clazz);
	}

}
