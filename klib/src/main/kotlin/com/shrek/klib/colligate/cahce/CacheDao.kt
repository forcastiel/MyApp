package com.shrek.klib.colligate.cahce

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.alibaba.fastjson.TypeReference
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.io.Serializable

/**
 * @author shrek
 * @date:  2016-05-31
 */
interface CacheDao {

    /**
     * 保存 String数据 到 缓存中
     */
    fun put(key: String, value: String)

    /**
     * 保存 String数据 到 缓存中
     * @param key   保存的key
     * @param value  保存的String数据
     * @param saveTime  保存的时间，单位：秒
     */
    fun put(key: String, value: String, saveTime: Int)

    /**
     * 读取 String数据
     * @param key
     * @return String 数据
     */
    fun getAsString(key: String): String?

    /**
     * 保存 JSONObject数据 到 缓存中
     * @param key 保存的key
     * @param value  保存的JSON数据
     */
    fun put(key: String, value: JSONObject)

    /**
     * 保存 JSONObject数据 到 缓存中
     * @param key  保存的key
     * @param value  保存的JSONObject数据
     * @param saveTime 保存的时间，单位：秒
     */
    fun put(key: String, value: JSONObject, saveTime: Int)

    /**
     * 读取JSONObject数据
     * @param key
     * @return JSONObject数据
     */
    fun getAsJSONObject(key: String): JSONObject?

    /**
     * 保存 JSONArray数据 到 缓存中
     * @param key  保存的key
     * @param value   保存的JSONArray数据
     */
    fun put(key: String, value: JSONArray)

    /**
     * 保存 JSONArray数据 到 缓存中
     * @param key 保存的key
     * @param value 保存的JSONArray数据
     * @param saveTime 保存的时间，单位：秒
     */
    fun put(key: String, value: JSONArray, saveTime: Int)

    /**
     * 读取JSONArray数据
     * @param key
     * @return JSONArray数据
     */
    fun getAsJSONArray(key: String): JSONArray?

    /**
     * 保存 JSON 数据 到 缓存中
     * @param key  保存的key
     * @param value 保存的JSON 数据
     */
    fun putJson(key: String, value: Any)

    /**
     * 保存 JSONArray数据 到 缓存中
     * @param key  保存的key
     * @param value  保存的JSONArray数据
     * @param saveTime 保存的时间，单位：秒
     */
    fun putJson(key: String, value: Any, saveTime: Int)

    /**
     * 读取JSONObject数据
     * @param key
     * @return JSONArray数据
     */
    fun <T> getAsJsonObject(key: String, reference: TypeReference<T>): T?

    /**
     * 保存 byte数据 到 缓存中
     * @param key 保存的key
     * @param value 保存的数据
     */
    fun put(key: String, value: ByteArray)

    /**
     * Cache for a stream
     * @param key the file name.
     * @return OutputStream stream for writing data.
     * @throws FileNotFoundException f the file can not be created.
     */
    fun put(key: String): OutputStream

    /**
     * @param key the file name.
     * @return (InputStream or null) stream previously saved in cache.
     * @throws FileNotFoundException if the file can not be opened
     */
    fun get(key: String): InputStream?

    /**
     * 保存 byte数据 到 缓存中
     * @param key 保存的key
     * @param value 保存的数据
     * @param saveTime 保存的时间，单位：秒
     */
    fun put(key: String, value: ByteArray, saveTime: Int)

    /**
     * 获取 byte 数据
     * @param key
     * @return byte 数据
     */
    fun getAsBinary(key: String): ByteArray?

    /**
     * 保存 Serializable数据 到 缓存中
     * @param key 保存的key
     * @param value 保存的value
     */
    fun put(key: String, value: Serializable)

    /**
     * 保存 Serializable数据到 缓存中
     * @param key 保存的key
     * @param value 保存的value
     * @param saveTime 保存的时间，单位：秒
     */
    fun put(key: String, value: Serializable, saveTime: Int)

    /**
     * 读取 Serializable数据
     * @param key
     * @return Serializable 数据
     */
    fun getAsObject(key: String): Any?

    /**
     * 保存 bitmap 到 缓存中
     * @param key 保存的key
     * @param value 保存的bitmap数据
     */
    fun put(key: String, value: Bitmap)

    /**
     * 保存 bitmap 到 缓存中
     * @param key 保存的key
     * @param value 保存的 bitmap 数据
     * @param saveTime 保存的时间，单位：秒
     */
    fun put(key: String, value: Bitmap, saveTime: Int)

    /**
     * 读取 bitmap 数据
     * @param key
     * @return bitmap 数据
     */
    fun getAsBitmap(key: String): Bitmap?

    /**
     * 保存 drawable 到 缓存中
     * @param key 保存的key
     * @param value 保存的drawable数据
     */
    fun put(key: String, value: Drawable)

    /**
     * 保存 drawable 到 缓存中
     * @param key 保存的key
     * @param value 保存的 drawable 数据
     * @param saveTime 保存的时间，单位：秒
     */
    fun put(key: String, value: Drawable, saveTime: Int)

    /**
     * 读取 Drawable 数据
     * @param key
     * @return Drawable 数据
     */
    fun getAsDrawable(key: String): Drawable?

    /**
     * 获取缓存文件
     * @param key
     * @return value 缓存的文件
     */
    fun file(key: String): File?

    /**
     * 移除某个key
     * @param key
     * @return 是否移除成功
     */
    fun remove(key: String): Boolean

    /**
     * 清除所有数据
     */
    fun clear()
}