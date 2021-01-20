package com.shrek.klib.colligate
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
operator fun <E> Collection<E>.get(position:Int):E? {
    var boVal : E? = null
    if(position < size) {
        forEachIndexed { index, bo ->
            if (index == position) {
                boVal = bo
            }
        }
    }
    return boVal
}