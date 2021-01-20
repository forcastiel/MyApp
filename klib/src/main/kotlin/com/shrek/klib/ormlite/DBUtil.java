package com.shrek.klib.ormlite;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.shrek.klib.colligate.AndroidVersionCheckUtils;
import com.shrek.klib.colligate.KConstantsKt;
import com.shrek.klib.colligate.ReflectUtils;
import com.shrek.klib.colligate.StringUtils;
import com.shrek.klib.logger.ZLog;
import com.shrek.klib.ormlite.ann.DatabaseField;
import com.shrek.klib.ormlite.ann.DatabaseTable;
import com.shrek.klib.ormlite.ann.Foreign;
import com.shrek.klib.ormlite.bo.ZDb;
import com.shrek.klib.ormlite.dao.DBTransforFactory;
import com.shrek.klib.ormlite.foreign.CascadeType;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class DBUtil {

    static final String FK_CONSTRAINT = "FK_";
    private static final String NOT_NULL_CONSTRAINT = " NOT NULL ";
    private static final String UNIQUE_CONSTRAINT = " UNIQUE ";

    private static final String INDEX_CONSTRAINT = "INDEX_";

    static final String INTERMEDIATE_CONSTRAINT = "INTERMEDIATE_";

    public static final String SPEPARANT_STR = " ";

    public static final String YINHAO_STR = "'";

    public static final Object LOCK_OBJ = new Object();

    private static final SimpleDateFormat DB_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final String TAG = "DBUtil";

    public static final void createTable(SQLiteDatabase mDatabase,
                                         TableInfo mTableInfo, boolean isExists) {
        String tableName = mTableInfo.tableName;
        StringBuffer createSqlSB = new StringBuffer("create table ");
        if (isExists) {
            createSqlSB.append(" IF NOT EXISTS ");
        }
        createSqlSB.append(tableName + "(");

        // 主键
        StringBuffer premarySB = new StringBuffer();
        // 索引<支持组合索引>
        List<String> indexColNames = new ArrayList<String>();

        boolean isFirstAddField = true;
        for (int i = 0; i < mTableInfo.allColumnNames.size(); i++) {
            String columnName = mTableInfo.allColumnNames.get(i);
            Field columnField = mTableInfo.allField.get(i);

            DatabaseField mDatabaseField = columnField
                    .getAnnotation(DatabaseField.class);
            /** ---------------- -- ---------------------- */
            if (!isFirstAddField) {
                createSqlSB.append(",");
            } else {
                isFirstAddField = false;
            }

            SqliteColumnType columnType = getObjMapping(columnField);
            createSqlSB.append(columnName + " " + columnType.name());

            /** ---------------- 约束 ---------------------- */
            if (!mDatabaseField.canBeNull()) {
                createSqlSB.append(NOT_NULL_CONSTRAINT);
            }
            if (mDatabaseField.unique()) {
                createSqlSB.append(UNIQUE_CONSTRAINT);
            }

            String defaultStr = mDatabaseField.defaultValue();
            if (defaultStr != null && !"".equals(defaultStr)) {
                if (columnType == SqliteColumnType.TEXT
                        || columnType == SqliteColumnType.TIMESTAMP
                        || columnType == SqliteColumnType.TIME
                        || columnType == SqliteColumnType.DATE) {
                    createSqlSB.append("DEFAULT '" + defaultStr + "' ");
                } else {
                    createSqlSB.append("DEFAULT " + defaultStr + " ");
                }
            }

            /** ---------------- 索引 ---------------------- */
            if (mDatabaseField.index()) {
                indexColNames.add(columnName);
            }

            /** ---------------- 主键 ---------------------- */
            if (mDatabaseField.id()) {
                String gapStr = " ";
                if (premarySB.length() != 0) {
                    gapStr = ",";
                }
                premarySB.append(gapStr + columnName);
            }
        }

        if (premarySB.length() > 0) {
            createSqlSB.append(",primary key(" + premarySB.toString() + ")");
        }
        createSqlSB.append(");");
        ZLog.i(TAG, "创建表的语句：" + createSqlSB.toString());
        mDatabase.execSQL(createSqlSB.toString());

        /** ---------------- 创建索引 ---------------------- */
        if (indexColNames.size() > 0) {
            boolean isFirstAddIndex = true;
            String indeName = INDEX_CONSTRAINT + tableName;
            StringBuffer indexSB = new StringBuffer(
                    "CREATE INDEX  IF NOT EXISTS " + indeName + " ON "
                            + tableName + "(");
            for (String indexColumn : indexColNames) {
                indexSB.append(indexColumn + (isFirstAddIndex ? "," : ""));
                isFirstAddIndex = false;
            }
            indexSB.append(");");
            ZLog.i(TAG, "创建索引的语句：" + indexSB.toString());
            mDatabase.execSQL(indexSB.toString());

            mTableInfo.indexTableName = indeName;
        }

        // 触发器
        List<String> trigeerArr = new ArrayList<String>();

        /** ---------------- 外键 ---------------------- */
        for (ForeignInfo fInfo : mTableInfo.allforeignInfos) {
            // 创建 中建表
            if (!tabbleIsExist(mDatabase, fInfo.getMiddleTableName())) {
                StringBuffer middleCreateSqlSB = new StringBuffer(
                        "create table ");
                middleCreateSqlSB.append(" IF NOT EXISTS "
                        + fInfo.getMiddleTableName() + "(");
                middleCreateSqlSB.append(fInfo.getOriginalColumnName() + " "
                        + getObjMapping(fInfo.getOriginalField()) + ",");
                middleCreateSqlSB.append(fInfo.getForeignColumnName() + " "
                        + getObjMapping(fInfo.getForeignField())
                        + ",primary key(" + fInfo.getOriginalColumnName() + ","
                        + fInfo.getForeignColumnName() + "));");
                ZLog.i(TAG,
                        "创建中建表的语句：" + middleCreateSqlSB.toString());

                mDatabase.execSQL(middleCreateSqlSB.toString());
            }
            trigeerArr.addAll(getTrigeerFKCaceade(fInfo));
        }

        /** ---------------- 创建触发器 ---------------------- */
        for (String trige : trigeerArr) {
            try {
                mDatabase.execSQL(trige);
            } catch (SQLException e) {
                // e.printStackTrace();
            }
        }
    }

    /**
     * 删除表 同时 也删除 和该表相关的 索引 触发器 中建表
     *
     * @param mDatabase
     * @param tableInfo
     */
    public static final void dropTable(SQLiteDatabase mDatabase,
                                       TableInfo tableInfo) {
        StringBuffer dropSB = new StringBuffer("DROP TABLE IF EXISTS "
                + tableInfo.tableName);
        ZLog.i(TAG, "DROP TABLE的语句：" + dropSB.toString());
        mDatabase.execSQL(dropSB.toString());

        /** --------------- 删除索引 ------------------ */
        if (!StringUtils.isEmpty(tableInfo.indexTableName)) {
            String indexDropName = "DROP INDEX IF EXISTS "
                    + tableInfo.indexTableName + ";";
            ZLog.i(TAG, "DROP TRIGGER的语句：" + indexDropName);
            mDatabase.execSQL(indexDropName);
        }

        /** --------------- 删除外键 ------------------ */
        for (ForeignInfo fInfo : tableInfo.allforeignInfos) {
            dropForeignTable(mDatabase, fInfo);
        }

    }

    /**
     * 删除外键 和外键相关的
     *
     * @param mDatabase
     * @param info
     */
    public static final void dropForeignTable(SQLiteDatabase mDatabase,
                                              ForeignInfo info) {
        StringBuffer dropSB = new StringBuffer("DROP TABLE IF EXISTS "
                + info.middleTableName);
        ZLog.i(TAG, "DROP TABLE的语句：" + dropSB.toString());
        mDatabase.execSQL(dropSB.toString());

        // 删除触发器
        for (String trigger : info.trigeerNameArr) {
            String dropTrigger = "DROP TRIGGER IF EXISTS " + trigger + ";";
            ZLog.i(TAG, "DROP TRIGGER的语句：" + dropTrigger);
            mDatabase.execSQL(dropTrigger);
        }
    }

    /**
     * 主表 和中间表的 级联操作 创建触发器 & 级联操作
     *
     */
    private static final List<String> getTrigeerFKCaceade(ForeignInfo info) {
        Foreign mForeign = info.getForeignAnn();
        String objTableName = getTableName(info.originalClazz);
        String objFieldName = getColumnName(info.originalField);
        String middleTableName = info.getMiddleTableName();
        String middleFieldName = info.getOriginalColumnName();

        List<String> trigeerArr = new ArrayList<String>();

        boolean isPersist = false, isMerge = false, isRefresh = false, isRemove = false;

        for (CascadeType cType : mForeign.cascade()) {

            if (cType == CascadeType.ALL) {
                isPersist = isMerge = isRefresh = isRemove = true;
                break;
            }

            switch (cType) {
                case PERSIST:
                    // 插入
                    isPersist = true;
                    break;
                case MERGE:
                    // 更新
                    isMerge = true;
                    break;
                case REFRESH:
                    // 更新
                    isRefresh = true;
                    break;
                case REMOVE:
                    // 删除
                    isRemove = true;
                    break;
                default:
                    break;
            }
        }

        if (isPersist) {
            // 创建插入触发器
            String triggerTableName = middleFieldName + "_Insert";
            StringBuffer insertSB = new StringBuffer("CREATE TRIGGER "
                    + triggerTableName);
            insertSB.append(" BEFORE Insert ON " + middleTableName);
            insertSB.append(" FOR EACH ROW BEGIN ");
            insertSB.append(" SELECT RAISE(ROLLBACK,'没有这个字段名 " + objFieldName
                    + " in " + objTableName + "')  ");
            insertSB.append(" WHERE (SELECT " + objFieldName + " FROM "
                    + objTableName + " WHERE " + objFieldName + " = NEW."
                    + middleFieldName + ") IS NULL; ");
            insertSB.append(" END ");
            print("创建插入触发器 ：" + insertSB.toString());
            trigeerArr.add(insertSB.toString());

            info.addTiggerName(triggerTableName);
        }

        if (isRefresh) {
            // 创建更新触发器
            String triggerTableName = middleFieldName + "_Update";

            StringBuffer updateSB = new StringBuffer("CREATE TRIGGER "
                    + triggerTableName);
            updateSB.append(" BEFORE Update ON " + middleTableName);
            updateSB.append(" FOR EACH ROW BEGIN ");
            updateSB.append(" SELECT RAISE(ROLLBACK,'没有这个字段名 " + objFieldName
                    + " in " + objTableName + "')  ");
            updateSB.append(" WHERE (SELECT " + objFieldName + " FROM "
                    + objTableName + " WHERE " + objFieldName + " = NEW."
                    + middleFieldName + ") IS NULL; ");
            updateSB.append(" END ");
            print("创建更新触发器 ：" + updateSB.toString());
            trigeerArr.add(updateSB.toString());

            info.addTiggerName(triggerTableName);
        }

        if (isRemove) {
            // 创建Delete触发器
            String triggerTableName = middleFieldName + "_Delete";

            StringBuffer deleteSB = new StringBuffer("CREATE TRIGGER "
                    + triggerTableName);
            deleteSB.append(" BEFORE DELETE ON " + objTableName);
            deleteSB.append(" FOR EACH ROW BEGIN ");
            deleteSB.append(" DELETE FROM " + middleTableName + " WHERE "
                    + middleFieldName + " = OLD." + objFieldName + ";");
            deleteSB.append(" END ");
            print("创建Delete触发器 ：" + deleteSB.toString());
            trigeerArr.add(deleteSB.toString());

            info.addTiggerName(triggerTableName);
        }

        if (isMerge) {
            // 创建级联操作
            String triggerTableName = middleFieldName + "_Caceade_Update";
            StringBuffer caceadeUpdateSB = new StringBuffer("CREATE TRIGGER "
                    + middleFieldName + "_Caceade_Update ");
            caceadeUpdateSB.append(" AFTER Update ON " + objTableName);
            caceadeUpdateSB.append(" FOR EACH ROW BEGIN ");
            caceadeUpdateSB.append(" update " + middleTableName + " set "
                    + middleFieldName + " = new." + objFieldName + " where "
                    + middleFieldName + " = old." + objFieldName + ";");
            caceadeUpdateSB.append(" END ");
            print("创建级联操作 更新触发器 ：" + caceadeUpdateSB.toString());
            trigeerArr.add(caceadeUpdateSB.toString());

            info.addTiggerName(triggerTableName);
        }
        return trigeerArr;
    }

    /**
     * 得到属性 在数据库中的字段名
     *
     * @param field
     * @return
     */
    public static final String getColumnName(Field field) {
        String fieldName = field.getAnnotation(DatabaseField.class)
                .columnName();
        if (fieldName == null || "".equals(fieldName)) {
            fieldName = field.getName();
        }
        return fieldName;
    }

    /**
     * 返回外键名称 FK_TEACHER_ID
     *
     * @param paramString
     * @param clazz
     * @return
     */
    public static final String getMapFKCulmonName(String paramString,
                                                  Class<?> clazz) {
        return FK_CONSTRAINT + clazz.getSimpleName() + "_" + paramString;
    }

    /**
     * 得到中建表的 名称
     *
     * @param clazz1
     * @param clazz2
     * @return
     */
    public static final String getIntermediateTableName(Class<?> clazz1,
                                                        Class<?> clazz2) {
        String tableName1 = clazz1.getSimpleName().toUpperCase();
        String tableName2 = clazz2.getSimpleName().toUpperCase();

        StringBuffer sb = new StringBuffer(INTERMEDIATE_CONSTRAINT);
        if (tableName1.compareTo(tableName2) > 0) {
            sb.append(tableName1 + "_" + tableName2);
        } else {
            sb.append(tableName2 + "_" + tableName1);
        }
        return sb.toString();
    }

    /**
     * 通过 field类型 得到对应数据库的字段类型
     *
     * @param field
     * @return
     */
    public static final SqliteColumnType getObjMapping(Field field) {
        Class<?> fieldClazz = field.getType();
        SqliteColumnType cType = SqliteColumnType.TEXT;
        if (fieldClazz.isAssignableFrom(String.class)) {
            cType = SqliteColumnType.TEXT;
        } else if (fieldClazz.isAssignableFrom(Date.class)
                || fieldClazz.isAssignableFrom(Calendar.class)) {
            cType = SqliteColumnType.TIMESTAMP;
        } else if (fieldClazz.isPrimitive()) {
            if (fieldClazz.isAssignableFrom(Integer.class)
                    || fieldClazz.isAssignableFrom(int.class)
                    || fieldClazz.isAssignableFrom(Boolean.class)
                    || fieldClazz.isAssignableFrom(boolean.class)) {
                cType = SqliteColumnType.INTEGER;
            } else if (fieldClazz.isAssignableFrom(Double.class)
                    || fieldClazz.isAssignableFrom(double.class)
                    || fieldClazz.isAssignableFrom(Float.class)
                    || fieldClazz.isAssignableFrom(float.class)) {
                cType = SqliteColumnType.REAL;
            }
        } else if (fieldClazz.isAssignableFrom(Collection.class)) {
            print("可能是List");
        } else {
            print("可能是外键");
        }
        return cType;
    }

    /**
     * 得到表名
     *
     * @param clazz
     * @return
     */
    public static final String getTableName(Class<?> clazz) {
        String tableName = clazz.getAnnotation(DatabaseTable.class).tableName();
        if (tableName == null || "".equals(tableName)) {
            tableName = clazz.getSimpleName();
        }
        return tableName;
    }

    /**
     * 判断属性 是否有效
     *
     * @param field
     * @return
     */
    public static final boolean judgeFieldAvaid(Field field) {
        DatabaseField mDatabaseField = field.getAnnotation(DatabaseField.class);
        Foreign mForeign = field.getAnnotation(Foreign.class);

        Class<?> clazz = field.getType();

        if ((mDatabaseField != null && isSupportType(field))
                || (mForeign != null && (Collection.class
                .isAssignableFrom(clazz) || ZDb.class
                .isAssignableFrom(clazz)))) {
            return true;
        }
        ZLog.i(TAG, "属性" + field.getName() + "判定为无效的属性!");
        return false;
    }

    /**
     * 是否是 支持的类型
     *
     * @param mField
     * @return
     */
    public static boolean isSupportType(Field mField) {
        Class<?> clazz = mField.getType();
        if (!clazz.isPrimitive() && !String.class.isAssignableFrom(clazz)
                && !clazz.isAssignableFrom(Date.class)
                && !clazz.isAssignableFrom(Calendar.class)
                && !Number.class.isAssignableFrom(clazz)
                && !List.class.isAssignableFrom(clazz)
                && !ZDb.class.isAssignableFrom(clazz)) {
            ZLog.e(TAG, clazz.getName() + " 不支持的数据类型");
            return false;
        }
        return true;
    }

    /**
     * 判断表 是否存在
     *
     * @param tableName
     * @return
     */
    private static boolean tabbleIsExist(SQLiteDatabase mDatabase,
                                         String tableName) {
        boolean result = false;
        if (tableName == null) {
            return false;
        }
        Cursor cursor = null;
        try {
            String sql = "select count(*) as c from "
                    + KConstantsKt.DATABASE_NAME
                    + " where type ='table' and name ='" + tableName.trim()
                    + "' ";
            cursor = mDatabase.rawQuery(sql, null);
            if (cursor.moveToNext()) {
                int count = cursor.getInt(0);
                if (count > 0) {
                    ZLog.i(TAG, "表名叫:" + tableName + "已经存在!!!!!");
                    result = true;
                }
            }
        } catch (Exception e) {

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }

    public static <F extends ZDb> F parseCurser(Cursor cursor,
                                                Class<F> giveClazz) {
        TableInfo info = TableInfo.newInstance(giveClazz);

        F obj = ReflectUtils.getInstance(giveClazz);
        for (int i = 0; i < info.allColumnNames.size(); i++) {
            String columnName = info.allColumnNames.get(i);
            Field field = info.allField.get(i);

            // 属性类型
            Class<?> fieldType = info.getFieldType(i);

            Object columnValue = getColumnValueFromCoursor(cursor, columnName,
                    fieldType);
            if (columnName == null) {
                continue;
            }
            // 从字段的值 转换为 Java里面的值
            Object fieldValues = DBTransforFactory.getFieldValue(columnValue,
                    fieldType);
            ReflectUtils.setFieldValue(obj, field, fieldValues);
        }
        return obj;
    }

    /**
     * 得到字段值
     *
     * @param cursor
     * @param columnName
     * @param fieldType
     * @return
     */
    public static Object getColumnValueFromCoursor(Cursor cursor,
                                                   String columnName, Class<?> fieldType) {
        Object columnValue = null;
        int index = cursor.getColumnIndex(columnName);
        if (index == -1) {
            return columnValue;
        }
        // 兼容性 2.3
        if (AndroidVersionCheckUtils.hasHoneycomb()) {
            switch (cursor.getType(index)) {
                case Cursor.FIELD_TYPE_STRING:
                    columnValue = cursor.getString(index);
                    break;
                case Cursor.FIELD_TYPE_FLOAT:
                    columnValue = cursor.getFloat(index);
                    break;
                case Cursor.FIELD_TYPE_INTEGER:
                    columnValue = cursor.getInt(index);
                    break;
                case Cursor.FIELD_TYPE_NULL:
                    columnValue = null;
                    break;
                case Cursor.FIELD_TYPE_BLOB:
                    columnValue = cursor.getBlob(index);
                    break;
            }
        } else {

            if (Integer.class.isAssignableFrom(fieldType)
                    || int.class.isAssignableFrom(fieldType)
                    || Boolean.class.isAssignableFrom(fieldType)
                    || boolean.class.isAssignableFrom(fieldType)
                    || Date.class.isAssignableFrom(fieldType)) {
                columnValue = cursor.getInt(index);
            } else if (Long.class.isAssignableFrom(fieldType)
                    || long.class.isAssignableFrom(fieldType)
                    || Calendar.class.isAssignableFrom(fieldType)) {
                columnValue = cursor.getLong(index);
            } else if (Float.class.isAssignableFrom(fieldType)
                    || float.class.isAssignableFrom(fieldType)) {
                columnValue = cursor.getFloat(index);
            } else {
                columnValue = cursor.getString(index);
            }

        }
        return columnValue;
    }

    private static void print(String paramString) {
        ZLog.i( "DBUtil", paramString);
    }

    public static String getFormatDateStr(Date formatDate) {
        return DB_DATE_FORMAT.format(formatDate);
    }

    public static Date getFormatDate(String formatStr) {
        try {
            return DB_DATE_FORMAT.parse(formatStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date();
    }

    /**
     * 时间 和 long之间转换
     *
     * @return
     */
//	public static long parseDateToLong(Date obj) {
//		return obj.getTime();
//	}
//
//	public static long parseCalendarToLong(Calendar obj) {
//		return parseDateToLong(obj.getTime());
//	}
//
//	public static Date parseLongToDate(long value) {
//		return new Date(value);
//	}
//
//	public static Calendar parseLongToCalendar(long value) {
//		Calendar cal = Calendar.getInstance();
//		cal.setTime(parseLongToDate(value));
//		return cal;
//	}
    public static void timeCompute(long before, long after) {
        ZLog.i("数据库操作:", "数据库操作耗时:" + (after - before) + "毫秒!");
    }
}
