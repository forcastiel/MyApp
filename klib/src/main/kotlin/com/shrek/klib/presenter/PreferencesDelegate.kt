package com.shrek.klib.presenter

import android.content.SharedPreferences
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * @author shrek
 * @date:  2016-06-08
 */
class IntPreDelegate(val operator:SharedPreferences,val defVal:Int = 0) : ReadWriteProperty<Any?, Int> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): Int {
        return operator.getInt(property.name,defVal)
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
        val edit = operator.edit()
        edit.putInt(property.name,value)
        edit.commit()
    }
}

class BooleanPreDelegate(val operator:SharedPreferences,val defVal:Boolean = false) : ReadWriteProperty<Any?, Boolean> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): Boolean {
        return operator.getBoolean(property.name,defVal)
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
        val edit = operator.edit()
        edit.putBoolean(property.name,value)
        edit.commit()
    }
}


class LongPreDelegate(val operator:SharedPreferences,val defVal:Long = 0) : ReadWriteProperty<Any?, Long> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): Long {
        return operator.getLong(property.name,defVal)
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Long) {
        val edit = operator.edit()
        edit.putLong(property.name,value)
        edit.commit()
    }
}


class FloatPreDelegate(val operator:SharedPreferences,val defVal:Float = 0f) : ReadWriteProperty<Any?, Float> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): Float {
        return operator.getFloat(property.name,defVal)
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Float) {
        val edit = operator.edit()
        edit.putFloat(property.name,value)
        edit.commit()
    }
}

class StringPreDelegate(val operator:SharedPreferences,val defVal:String = "") : ReadWriteProperty<Any?, String> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): String {
        return operator.getString(property.name,defVal)
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        val edit = operator.edit()
        edit.putString(property.name,value)
        edit.commit()
    }
}


class EnumPreDelegate<T:Enum<T>>(val operator:SharedPreferences
                                 ,val defVal:T,val instanceMethod:(String)->T)
    : ReadWriteProperty<Any?, T> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return instanceMethod(operator.getString(property.name,defVal.name))
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        val edit = operator.edit()
        edit.putString(property.name,value.name)
        edit.commit()
    }
}