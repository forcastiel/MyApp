package com.shrek.klib.extension

import java.nio.ByteBuffer
import kotlin.experimental.and

fun Byte.toHex(): String {
    val hv = Integer.toHexString((this and 0xFF.toByte()).toInt()).toUpperCase()
    var hvStr = "${hv}"
    if (hvStr.length == 1) {
        hvStr = "0${hv}"
    } else if (hvStr.length > 2) {
        hvStr = hvStr.substring(hvStr.length - 2)
    }
    return hvStr
}

fun ByteArray.readInt(offset: Int = 0): Int {
    return ((this[offset].toInt() and 0xff)
            or ((this[offset + 1].toInt() and 0xff) shl 8)
            or (this[offset + 2].toInt() and 0xff shl 16)
            or (this[offset + 3].toInt() and 0xff shl 24))
}

fun ByteArray.readLong(offset: Int = 0): Long {
    val buffer = ByteBuffer.allocate(8)
    buffer.put(this,offset,8)
    return buffer.getLong(0)
}

fun Int.byteArray(): ByteArray {
    val targets = byteArrayOf(0, 0, 0, 0)
    targets[0] = (this and 0xff).toByte() // 最低位
    targets[1] = ((this shr 8) and 0xff).toByte() // 次低位
    targets[2] = ((this shr 16) and 0xff).toByte() // 次高位
    targets[3] = ((this shr 24) and 0xff).toByte() // 最高位,无符号右移。
    return targets
}

fun Long.byteArray(): ByteArray {
    val buffer = ByteBuffer.allocate(8)
    buffer.putLong(0,this)
    return buffer.array()
}

fun ByteArray.hex(offset: Int = 0, readLength: Int = size): String {
    val builder = StringBuffer()
    if (this.size >= offset + readLength) {
        for (i in offset until offset + readLength) {
            builder.append(this[i].toHex())
        }
    }
    return builder.toString()
}

fun ByteArray.intHex(offset: Int = 0): String {
    return hex(offset, 4)
}

/**
 * 数组匹配位置
 * 例如 [2,3,7,8,9,8,0] 查找 [7,8]  index = 2   如果查找的是 0  返回疑似下标
 * @return Pair<Int,ByteArray>  下标 to 疑似剩余的
 */
fun ByteArray.arrayIndex(matchArray: ByteArray): Pair<Int, ByteArray?> {
    var outOfBytes: java.util.ArrayList<Byte>? = null
    forEachIndexed { index, byte ->
        var isMatch = true
        if (index + matchArray.size > size) {
            outOfBytes = arrayListOf()
        }
        for (innerIndex in 0 until Math.min(matchArray.size, size - index)) {
            if (!isMatch) {
                break
            }
            isMatch = matchArray[innerIndex] == this[index + innerIndex]
            if (isMatch) {
                outOfBytes?.add(byte)
            }
        }
        if (isMatch) {
            return index to outOfBytes?.toByteArray()
        }
    }
    return -1 to null
}

fun List<Byte>.arrayIndex(matchArray: ByteArray): Pair<Int, ByteArray?> {
    return toByteArray().arrayIndex(matchArray)
}