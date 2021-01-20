package com.shrek.klib.ormlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.shrek.klib.ZSetting;
import com.shrek.klib.colligate.AndroidVersionCheckUtils;
import com.shrek.klib.colligate.ReflectUtils;
import com.shrek.klib.logger.ZLog;
import com.shrek.klib.ormlite.bo.ZDb;
import com.shrek.klib.ormlite.dao.DBDao;
import com.shrek.klib.ormlite.dao.DBDaoImpl;
import com.shrek.klib.ormlite.dao.DBTransforFactory;
import com.shrek.klib.ormlite.exception.TableInfoInvalidException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class ZDBHelper extends SQLiteOpenHelper {
    private static final int CLOSE_DBOPERATOR = 0x89;

    private static Map<Class<? extends ZDb>, DBDao> allDBDaos = new HashMap<Class<? extends ZDb>, DBDao>();
    // DatabaseErrorHandler是API 11的
    // private static DatabaseErrorHandler mErrorHandler = new
    // DefaultDatabaseErrorHandler();
    private static SQLiteDatabase currentDBOperator;

    private static final Object SQLITEDATABASE_LOCK = new Object();
    public static final Object LOCK_OBJ = new Object();
    private static Handler mHandler;

    private static AtomicBoolean isLockOperator = new AtomicBoolean(false); //是否锁 数据库操作 不让释放

    //数据操作 有效时间
    int dbOPeratorAvailTime;

    private Class<? extends ZDb>[] loadDbBos;


    public ZDBHelper(Context context, String dbName, int dbVersion, int dbOPeratorAvailTime) {
        super(context, dbName, null, dbVersion);
        this.dbOPeratorAvailTime = dbOPeratorAvailTime;

        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {

                @Override
                public boolean handleMessage(Message msg) {
                    switch (msg.what) {
                        case CLOSE_DBOPERATOR:
                            if (currentDBOperator != null) {
                                if (!isLockOperator.get()) {
                                    currentDBOperator.close();
                                    currentDBOperator = null;
                                    ZLog.i(ZDBHelper.this, ">>>>>> 数据库操作有效时间已过,关闭操作对象 <<<<<<");
                                } else {
                                    sendCloseMsg();
                                }
                            }
                            break;
                        default:
                            break;
                    }
                    return true;
                }
            });
            loadDbBos = loadDatabaseClazz();
            // Looper.prepare();
        }
    }

    public ZDBHelper(Context context, ZSetting setting) {
        this(context, setting.getDbName(), setting.getDbVersion(), setting.getDbOPeratorAvailTime());
    }

    @Override
    public void onCreate(SQLiteDatabase arg0) {
        if (loadDbBos == null) {
            loadDbBos = loadDatabaseClazz();
        }
        arg0.setLocale(Locale.CHINA);
        createTables(arg0, loadDbBos);
    }

    @Override
    public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
        dropTables(arg0, loadDbBos);
        onCreate(arg0);
    }

    public abstract Class<? extends ZDb>[] loadDatabaseClazz();

    public SQLiteDatabase getDatabase(boolean isReadOnly) {
        synchronized (SQLITEDATABASE_LOCK) {
            if (currentDBOperator != null && currentDBOperator.isOpen()) {
                //之前写的
//				if (currentDBOperator.isReadOnly() == isReadOnly) {
//					mHandler.removeMessages(CLOSE_DBOPERATOR);
//					mHandler.sendEmptyMessageDelayed(CLOSE_DBOPERATOR,
//							ZWApplication.dbOPeratorAvailTime);
//					return currentDBOperator;
//				} else {
//					mHandler.removeMessages(CLOSE_DBOPERATOR);
//					currentDBOperator.close();
//				}
                removeCloseMsg();
//				mHandler.sendEmptyMessageDelayed(CLOSE_DBOPERATOR,
//						ZWApplication.dbOPeratorAvailTime);
                sendCloseMsg();
                return currentDBOperator;
            }
//			if (isReadOnly) {
//				currentDBOperator = getReadableDatabase();
//			} else {
//				currentDBOperator = getWritableDatabase();
//			}
            try {
                currentDBOperator = getWritableDatabase();
            } catch (Exception e) {
                ZLog.e(ZDBHelper.class, "创建数据库出现异常,尝试获取只读操作");
                if (isReadOnly) {
                    currentDBOperator = getReadableDatabase();
                } else {
                    e.printStackTrace();
                }
            }
            sendCloseMsg();
            return currentDBOperator;
        }

    }

    private void removeCloseMsg() {
        mHandler.removeMessages(CLOSE_DBOPERATOR);
    }

    private void sendCloseMsg() {
        mHandler.sendEmptyMessageDelayed(CLOSE_DBOPERATOR, dbOPeratorAvailTime);
    }

    public void lockOperator() {
        isLockOperator.set(true);
    }

    public void unLockOperator() {
        isLockOperator.set(false);
    }

    /**
     * 得到操作类
     *
     * @param clazz
     * @return
     */
    public <T extends ZDb> DBDao<T> getDao(Class<T> clazz) {
        if (allDBDaos.containsKey(clazz)) {
            return allDBDaos.get(clazz);
        }

        DBDao<T> dao = new DBDaoImpl<>(clazz, this);
        allDBDaos.put(clazz, dao);
        return dao;
    }

    /**
     * 创建多个表
     *
     * @param arg0
     * @param createTablClss
     */
    private void createTables(SQLiteDatabase arg0,
                              Class<? extends ZDb>... createTablClss) {
        for (Class<? extends ZDb> clazz : createTablClss) {
            TableInfo info = TableInfo.newInstance(clazz);
            if (info == null) {
                throw new TableInfoInvalidException("获取不到 类:" + clazz.getSimpleName() + " TableInfo对象!");
            }
            DBUtil.createTable(arg0, info, true);
        }
    }

    /**
     * 删除多张表
     *
     * @param arg0
     * @param createTablClss
     */
    private void dropTables(SQLiteDatabase arg0,
                            Class<? extends ZDb>... createTablClss) {
        for (Class<? extends ZDb> clazz : createTablClss) {
            TableInfo info = TableInfo.newInstance(clazz);
            if (info == null) {
                throw new TableInfoInvalidException("获取不到 类:" + clazz.getSimpleName() + " TableInfo对象!");
            }
            DBUtil.dropTable(arg0, info);
        }
    }

    /**
     * 不用生成 dao对象 直接做操作
     *
     * @param sql
     * @param objClass
     * @return
     */
    public <ParseObjec extends ZDb> ParseObjec queryObj(String sql,
                                                        Class<ParseObjec> objClass) {
        Cursor cursor = getDatabase(true).rawQuery(sql, null);
        ParseObjec obj = null;
        if (cursor.moveToNext()) {
            obj = DBUtil.parseCurser(cursor, objClass);
        }
        cursor.close();
        return obj;
    }

    /**
     * 不用生成 dao对象 直接做操作
     *
     * @param sql
     * @param objClass
     * @return
     */
    public <ParseObjec extends ZDb> List<ParseObjec> queryObjs(String sql,
                                                               Class<ParseObjec> objClass) {
        Cursor cursor = getDatabase(true).rawQuery(sql, null);
        List<ParseObjec> list = new ArrayList<ParseObjec>();
        while (cursor.moveToNext()) {
            ParseObjec obj = DBUtil.parseCurser(cursor, objClass);
            if (obj != null) {
                list.add(obj);
            }
        }

        cursor.close();
        return list;
    }

    /**
     * 通过sql查询 得到map的映射
     *
     * @param sql
     * @return
     */
    public Map<String, Object> queryMap(String sql) {
        Cursor cursor = getDatabase(true).rawQuery(sql, null);
        Map<String, Object> map = new HashMap<String, Object>();
        if (cursor.moveToNext()) {
            int columnCount = cursor.getColumnCount();
            for (int i = 0; i < columnCount; i++) {
                String columnName = cursor.getColumnName(i);
                Object obj = DBUtil.getColumnValueFromCoursor(cursor, columnName, null);
                map.put(columnName, obj);
            }
        }
        cursor.close();
        return map;
    }

    public List<Map<String, Object>> queryMaps(String sql) {
        Cursor cursor = getDatabase(true).rawQuery(sql, null);
        List<Map<String, Object>> lists = new ArrayList<Map<String, Object>>();

        while (cursor.moveToNext()) {
            int columnCount = cursor.getColumnCount();
            Map<String, Object> map = new HashMap<String, Object>();
            for (int i = 0; i < columnCount; i++) {
                String columnName = cursor.getColumnName(i);
                Object obj = DBUtil.getColumnValueFromCoursor(cursor, columnName, null);
                map.put(columnName, obj);
            }
            lists.add(map);
        }

        cursor.close();
        return lists;
    }

    /**
     * 通过游标 直接得到对象
     *
     * @param cursor
     * @param objClass
     * @return
     */
    private <ParseObjec> ParseObjec getObjByCursor(Cursor cursor,
                                                   Class<ParseObjec> objClass) {
        ParseObjec obj = null;
        try {
            obj = objClass.getConstructor().newInstance();
        } catch (Exception e) {
            ZLog.e(objClass.getSimpleName(), "请提供一个无参的构造方法!");
            e.printStackTrace();
            return null;
        }

        int columnCount = cursor.getColumnCount();
        for (int i = 0; i < columnCount; i++) {
            String columnName = cursor.getColumnName(i);
            Field field = ReflectUtils.getFieldByName(objClass, columnName);
            // 属性类型
            Class<?> fieldType = field.getType();
            // 从字段的值 转换为 Java里面的值
            Object fieldValue = DBTransforFactory.getFieldValue(
                    getObjectValueByCursor(cursor, i), fieldType);
            ReflectUtils.setFieldValue(obj, field, fieldValue);
        }

        return obj;
    }

    /**
     * 通过下标 得到游标里面的值
     *
     * @param cursor
     * @param index
     * @return
     */
    private Object getObjectValueByCursor(Cursor cursor, int index) {
        Object columnValue = new Object();
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

            columnValue = cursor.getString(index);

        }

        return columnValue;
    }

}
