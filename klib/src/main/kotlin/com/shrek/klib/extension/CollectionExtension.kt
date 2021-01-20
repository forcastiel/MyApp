package com.shrek.klib.extension

/**
 * @author Shrek
 * @date:  2016-12-14
 */
operator fun <E> List<E>.get(position:Int):E? {
    if(position < size) {
        return getOrNull(position)
    }
    return null
}

operator fun <E> Set<E>.get(position:Int):E? {
    if(position < size) {
        return get(position)
    }
    return null
}