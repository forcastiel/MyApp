package com.shrek.klib.ormlite.stmt;

import com.shrek.klib.logger.ZLog;
import com.shrek.klib.ormlite.DBUtil;
import com.shrek.klib.ormlite.ForeignInfo;
import com.shrek.klib.ormlite.bo.ZDb;
import com.shrek.klib.ormlite.dao.DBTransforFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 查询的构造器 order by group by having Limit 9 Offset 10
 * 
 * @author shrek
 *
 */
public class QueryBuilder extends StmtBuilder<QueryBuilder> {
	public static final String SELECT_KEYWORD = "SELECT ";
	private static final String ORDER_KEYWORD = " ORDER BY ";
	private static final String GROUP_KEYWORD = " GROUP BY ";
	private static final String HAVING_KEYWORD = " HAVING ";
	// 查询列名
	private List<String> queryColumns, groupColumns, orderColumns;
	private Map<String, String> orderColumnsMap;
	public String havingStr;
	// limitIndex 为查询多少行 offsetIndex 跳过多少行
	public int limitIndex, offsetIndex;
	// 连接查询语句 joinSelect查询时候添加的
	public StringBuffer joinSB, joinSelect;

	public QueryBuilder(Class<? extends ZDb> clazz) {
		super(clazz);
		// TODO Auto-generated constructor stub
		sqlBuffer = new StringBuffer(SELECT_KEYWORD + " ");
		// order by 条件 key:属性名 value：asc or desc
		orderColumnsMap = new HashMap<String, String>();
		orderColumns = new ArrayList<String>();
		queryColumns = new ArrayList<String>();
		groupColumns = new ArrayList<String>();

		joinSB = new StringBuffer();
		joinSelect = new StringBuffer();
		// havingStr = new StringBuffer();
		limitIndex = offsetIndex = -1;
	}

	/**
	 * 添加order 条件 去重复
	 * 
	 * @param fieldName
	 * @param isAsc
	 *            是否升序
	 */
	public void addOrderByCon(String fieldName, boolean isAsc) {
		if (fieldName != null && !orderColumnsMap.containsKey(fieldName)) {
			orderColumnsMap.put(fieldName, isAsc ? "ASC" : "DESC");
			orderColumns.add(fieldName);
		}
	}

	/**
	 * 添加 group 条件，
	 */
	public void addGroupByCon(String... fieldNames) {
		for (String fieldName : fieldNames) {
			if (fieldName == null || fieldName.equals("")) {
				continue;
			}
			for (String groupStr : groupColumns) {
				if (groupStr.equals(fieldName)) {
					return;
				}
			}
			groupColumns.add(fieldName);
		}
	}

	/**
	 * 属性名 包括函数 count(id) 支持别名 name as name1
	 */
	public void addSelectColumn(String... fieldNames) {
		for (String addStr : fieldNames) {
			if (addStr == null || addStr.equals("")) {
				continue;
			}

			for(String columns : queryColumns){
				if(addStr.equals(columns)){
					continue;
				}
			}

			queryColumns.add(addStr);
		}
	}

	/**
	 * 得到查询的 外键字段 没有就会null
	 * 
	 * @return
	 */
	public List<ForeignInfo> getQueryFkColumns() {
		List<ForeignInfo> fks = new ArrayList<ForeignInfo>();
		for (String columnName : queryColumns) {
			if (columnName.equals("*")) {
				return null;
			}

			ForeignInfo info = tableInfo.getForeign(columnName);
			if (info != null) {
				fks.add(info);
			}
		}
		return fks;
	}

	/**
	 * 添加外键比较的条件
	 * 
	 * @param compareStr
	 *            比较符 = <= >= != <>
	 * @param fieldName
	 * @param obj
	 * @return
	 */
	public StmtBuilder fkCompare(String compareStr, String fieldName, Object obj) {
		ForeignInfo fInfo = tableInfo.getForeign(fieldName);
		if (fInfo == null) {
			ZLog.e(this, "在" + tableInfo.getTableName() + "表中找不到叫"
					+ fieldName + "外键!");
			return this;
		}
		Field mField = fInfo.getForeignField();
		String columnName = fInfo.getForeignColumnName();
		Class<?> fieldType = mField.getType();

		if (initContinue(fieldName, mField, columnName, fieldType, true, obj)) {
			joinSB = new StringBuffer();
			joinSB.append(" LEFT JOIN "
					+ fInfo.getMiddleTableName()
					+ " fk ON fk."
					+ fInfo.getOriginalColumnName()
					+ " = "
					+ getColumnNameWithAliases(DBUtil.getColumnName(fInfo
							.getOriginalField())));
			appendWhereStr("fk." + columnName + compareStr
					+ DBTransforFactory.getColumnValue(obj));
		}
		return this;
	}

	public StmtBuilder fkEq(String fieldName, Object obj) {
		return fkCompare("=", fieldName, obj);
	}

	public StmtBuilder fkNotEq(String fieldName, Object obj) {
		return fkCompare("<>", fieldName, obj);
	}

	@Override
	public String getSql() {
		// TODO Auto-generated method stu
		/** ------------------- 添加查询的字段 ------------------------- */
		if (queryColumns.size() == 0) {
			queryColumns.add("*");
		}

		for (int j = 0; j < queryColumns.size(); j++) {
			String hasAddStr = queryColumns.get(j);
			int leftKH = hasAddStr.indexOf("(");
			int rightKH = hasAddStr.indexOf(")");
			int xIndex = hasAddStr.indexOf("*");
			int asIndex = hasAddStr.indexOf(" as ");

			String columnName = null;
			if (leftKH != -1 && rightKH != -1) {
				// 函数
				String fieldName = hasAddStr.substring(leftKH + 1, rightKH);
				columnName = tableInfo.getColumnByFieldStr(fieldName);
				if (columnName == null) {
					continue;
				}
				columnName = hasAddStr.replace(fieldName,
						getColumnNameWithAliases(columnName));
			} else if (asIndex != -1) {
				// 有别名
				String fieldName = hasAddStr.substring(0, asIndex);
				columnName = tableInfo.getColumnByFieldStr(fieldName);
				if (columnName == null) {
					continue;
				}
				columnName = hasAddStr.replace(fieldName,
						getColumnNameWithAliases(columnName));
			} else if (xIndex != -1) {
				// * 查询所有
				columnName = hasAddStr;
			} else {
				columnName = getColumnNameWithAliases(tableInfo
						.getColumnByFieldStr(hasAddStr));
				if (columnName == null) {
					continue;
				}
			}

			if (j != 0) {
				sqlBuffer.append(",");
			}
			sqlBuffer.append(columnName);
		}

		sqlBuffer.append(joinSelect + " FROM " + getTableNameWithAliases()
				+ " " + joinSB);
		/** ------------------- 添加where的字段 ------------------------- */
		sqlBuffer.append(getWhereSql());
		/** ------------------- 添加group by的字段 ------------------------- */
		for (int i = 0; i < groupColumns.size(); i++) {
			String groupStr = groupColumns.get(i);
			String columnName = tableInfo.getColumnByFieldStr(groupStr);
			if (columnName == null) {
				continue;
			}
			if (i == 0) {
				sqlBuffer.append(GROUP_KEYWORD);
			} else {
				sqlBuffer.append(",");
			}
			sqlBuffer.append(getColumnNameWithAliases(columnName));
		}

		/** ------------------- 添加Having的字段 ------------------------- */
		if (havingStr != null && "".equals(havingStr)) {
			sqlBuffer.append(HAVING_KEYWORD + havingStr);
		}
		/** ------------------- 添加order by的字段 ------------------------- */
		for (int i = 0; i < orderColumns.size(); i++) {
			String orderStr = orderColumns.get(i);
			String columnName = tableInfo.getColumnByFieldStr(orderStr);
			if (columnName == null) {
				continue;
			}
			if (i == 0) {
				sqlBuffer.append(ORDER_KEYWORD);
			} else {
				sqlBuffer.append(",");
			}
			sqlBuffer.append(getColumnNameWithAliases(columnName) + " "
					+ orderColumnsMap.get(orderStr));
		}
		/** ------------------- 添加limit的字段 ------------------------- */
		if (limitIndex != -1) {
			sqlBuffer.append(" LIMIT " + limitIndex);
		}

		if (offsetIndex != -1) {
			sqlBuffer.append(" OFFSET  " + offsetIndex);
		}

		ZLog.i(this, "查询的SQL为:" + sqlBuffer.toString());
		return sqlBuffer.toString();
	}

	/**
	 * 清空查询的section
	 */
	public void clearSelectSection() {
		queryColumns.clear();
	}

	public String getTableNameWithAliases() {
		if (tableAliases != null) {
			return tableInfo.getTableName() + " " + tableAliases;
		}
		return tableInfo.getTableName();
	}

	public String getColumnNameWithAliases(String columnName) {
//		if(columnName != null && columnName.indexOf("*") != -1){
//			return " * ";
//		}
		
//		if (!tableInfo.isExistFieldByName(columnName)) {
//			ZWLogger.e(this, " 查询的表中 没有叫" + columnName + "的字段名 或者 属性名!");
//			return null;
//		}
		if (tableAliases != null) {
			return tableAliases + "." + columnName;
		}
		return columnName;
	}

	@Override
	public void cycle() {
		// TODO Auto-generated method stub
		super.cycle();
		queryColumns.clear();
		groupColumns.clear();
		orderColumns.clear();

		queryColumns = null;
		groupColumns = null;
		orderColumns = null;

		orderColumnsMap.clear();
		orderColumnsMap = null;

		havingStr = null;
	}
}
