package com.shrek.klib.ormlite;

import com.shrek.klib.colligate.ReflectUtils;
import com.shrek.klib.colligate.StringUtils;
import com.shrek.klib.logger.ZLog;
import com.shrek.klib.ormlite.ann.Foreign;
import com.shrek.klib.ormlite.bo.ZDb;
import com.shrek.klib.ormlite.exception.ForeignKeyValidException;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class TableInfo {
    private static final Map<Class<?>, TableInfo> tableInfoFactory = Collections
            .synchronizedMap(new WeakHashMap<Class<?>, TableInfo>());
    // 所有字段名
    public List<String> allColumnNames;
    // 所有属性
    public List<Field> allField;
    // 所有外键 key:外键字段名 value:对于Teacher 的id属性
    public List<ForeignInfo> allforeignInfos;

    public Class<? extends ZDb> clazz;
    /**
     * tableName：表名 indexTableName: 索引表名
     */
    String tableName, indexTableName;

    private TableInfo(Class<? extends ZDb> clazz) {
        this.clazz = clazz;
        // 必须有无参数的构造方法
        try {
            clazz.getConstructor();
        } catch (NoSuchMethodException e1) {
            ZLog.i(TableInfo.this, "类" + clazz.getSimpleName()
                    + "请提供无参数的构造方法！");
            e1.printStackTrace();
        }
        tableName = DBUtil.getTableName(clazz);
        allColumnNames = new ArrayList<String>();

        allField = new ArrayList<Field>();

        this.allforeignInfos = new ArrayList<ForeignInfo>();

        List<Field> allFieldCache = ReflectUtils.getAllClassField(clazz,
                new ReflectUtils.FieldCondition() {

                    @Override
                    public boolean isFieldValid(Field field) {
                        return DBUtil.judgeFieldAvaid(field);
                    }
                });

        for (Field field : allFieldCache) {
            // 属性的类型
            Class<?> fieldType = field.getType();

            /**
             * ########## 外键判断 ##########
             */
            Foreign foreignAnn = field.getAnnotation(Foreign.class);
            if (foreignAnn != null) {
                String originalName = foreignAnn.originalColumnName();
                String fkFieldName = foreignAnn.foreignColumnName();

                String errorInfo = "设置了无效的外键:" + field.getName();

                if (StringUtils.isEmpty(originalName)
                        || StringUtils.isEmpty(fkFieldName)) {
                    ZLog.e(this, errorInfo);
                    throw new ForeignKeyValidException(errorInfo);
                }

                Class<?> genericClazz = null;

                if (ZDb.class.isAssignableFrom(fieldType)) {
                    genericClazz = fieldType;
                } else if (Collection.class.isAssignableFrom(fieldType)) {
                    // 外键是集合类型
                    Type fc = field.getGenericType(); // 关键的地方，如果是List类型，得到其Generic的类型
                    if (fc == null) {
                        ZLog.i(this,
                                "外键指向的 类名：" + fieldType.getSimpleName()
                                        + "字段名:" + fkFieldName + " list 未设置泛型");
                        continue;
                    }

                    if (fc instanceof ParameterizedType) {// 【3】如果是泛型参数的类型
                        ParameterizedType pt = (ParameterizedType) fc;
                        genericClazz = (Class<?>) pt.getActualTypeArguments()[0]; // 得到泛型里的class类型对象。
                    }
                }

                if (genericClazz == null) {
                    continue;
                }

                Field objField = ReflectUtils.getFieldByName(genericClazz,
                        fkFieldName);
                Field orgField = ReflectUtils.getFieldByName(clazz, originalName);

                if (DBUtil.judgeFieldAvaid(objField)) {
                    ForeignInfo fInfo = new ForeignInfo();
                    fInfo.valueField = field;
                    fInfo.setOriginalField(orgField);
                    fInfo.setForeignField(objField);
                    fInfo.setOriginalClazz(clazz);
                    fInfo.setForeignClazz((Class<? extends ZDb>) genericClazz);
                    fInfo.initValue();

                    allforeignInfos.add(fInfo);
                } else {
                    ZLog.i(this,
                            "外键指向的 类名：" + fieldType.getSimpleName() + "字段名:"
                                    + fkFieldName + "不符合DataBaseField的条件!");
                }

                // } catch (NoSuchFieldException e) {
                // e.printStackTrace();
                // throw new
                // ForeignKeyValidException(errorInfo+" 类名:"+foreignName+"里面根本没有叫"+fkFieldName+"字段");
                // }
                continue;
            } else {
                // 添加字段名
                String fieldName = DBUtil.getColumnName(field);
                allField.add(field);
                allColumnNames.add(fieldName);
            }

        }

    }

    public static final TableInfo newInstance(
            Class<? extends ZDb> clazz) {
        TableInfo mTableInfo = null;
        if (tableInfoFactory.containsKey(clazz)) {
            mTableInfo = tableInfoFactory.get(clazz);
        }

        if (mTableInfo == null) {
            mTableInfo = new TableInfo(clazz);
            tableInfoFactory.put(clazz, mTableInfo);
        }
        return mTableInfo;
    }

    /**
     * 通过 field name找到表字段名
     *
     * @param fieldName
     * @return
     */
    public String getColumnByFieldStr(String fieldName) {
        for (int i = 0; i < allField.size(); i++) {
            Field field = allField.get(i);
            if (fieldName.equals(field.getName())) {
                return allColumnNames.get(i);
            }
        }
        return null;
    }

    /**
     * 通过 field name找到表字段下标
     *
     * @param fieldName
     * @return
     */
    public int getColumnIndexByFieldStr(String fieldName) {
        for (int i = 0; i < allField.size(); i++) {
            Field field = allField.get(i);
            if (fieldName.equals(field.getName())) {
                return i;
            }
        }
        ZLog.e(this, "类：" + clazz.toString() + " 属性名叫:" + fieldName
                + " 找不到~~");
        return -1;
    }

    /**
     * 通过名字 得到外键信息
     *
     * @param fieldName
     * @return
     */
    public ForeignInfo getForeign(String fieldName) {
        for (ForeignInfo fInfo : allforeignInfos) {
            if (fInfo.getValueField().getName().equals(fieldName)) {
                return fInfo;
            }
        }
        return null;
    }

    /**
     * 通过表中 字段下标 找到 对应属性的Class对象
     *
     * @param index
     * @return
     */
    public Class<?> getFieldType(int index) {
        // String columnName = allColumnNames.get(index);
        Field field = allField.get(index);
        Class<?> fieldType = null;
        // 判断属性是否 外键
        // if (allforeignClassMaps.containsKey(columnName)) {
        // fieldType = allforeignClassMaps.get(columnName);
        // } else {
        fieldType = field.getType();
        // }
        return fieldType;
    }

    /**
     * 是否存在名字叫fieldName 属性
     *
     * @param fieldName
     * @return
     */
    public boolean isExistFieldByName(String fieldName) {
        for (Field field : allField) {
            if (field.getName().equals(fieldName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否存在这个字段名
     *
     * @param columnName
     * @return
     */
    public boolean isExistColumnByName(String columnName) {
        for (String temp : allColumnNames) {
            if (temp.equals(columnName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否纯在名字叫fkFieldName 的外键属性
     *
     * @param fkFieldName
     * @return
     */
    public boolean isExistFKFieldByName(String fkFieldName) {
        for (ForeignInfo fInfo : allforeignInfos) {
            if (fInfo.valueField.getName().equals(fkFieldName)) {
                return true;
            }
        }
        return false;
    }

    public String getTableName() {
        return tableName;
    }

    public String getIndexTableName() {
        return indexTableName;
    }

}
