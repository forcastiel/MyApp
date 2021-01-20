package com.shrek.klib.extension

import java.text.SimpleDateFormat
import java.util.*

/**
 * 日期的扩展
 * @author Shrek
 * @date:  2016-08-05
 */
enum class DateOptUnit {
    YEAR,MONTH,DATE,HOUR,MINUTE,SECOND;
    
    fun parseType():Int{
        var value = Calendar.DATE
        when(this){
            YEAR -> value = Calendar.DATE
            MONTH -> value = Calendar.MONTH
            DATE ->  value = Calendar.DATE
            HOUR -> value = Calendar.HOUR
            MINUTE -> value = Calendar.MINUTE
            SECOND -> value = Calendar.SECOND
        }
        return value
    }
}

//今天最后一秒
fun Any.lastOfToday(): Long {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.SECOND, 59)
    calendar.set(Calendar.MINUTE, 59)
    calendar.set(Calendar.HOUR_OF_DAY, 23)
    return calendar.timeInMillis
}

//日期操作对象
data class DateOperator(val unit :DateOptUnit,val value: Int)

fun Any.year(value:Int):DateOperator {
    return DateOperator(DateOptUnit.YEAR,value)
}

fun Any.month(value:Int):DateOperator {
    return DateOperator(DateOptUnit.MONTH,value)
}

fun Any.day(value:Int):DateOperator {
    return DateOperator(DateOptUnit.DATE,value)
}

fun hour(value:Int):DateOperator {
    return DateOperator(DateOptUnit.HOUR,value)
}

//日期差值对象
data class DateDivergence(val unit :DateOptUnit,val diffDate: Date)

fun Any.monthDiff(diffDate: Date):DateDivergence {
    return DateDivergence(DateOptUnit.MONTH,diffDate)
}

fun Any.yearDiff(diffDate: Date):DateDivergence {
    return DateDivergence(DateOptUnit.YEAR,diffDate)
}

fun Any.dayDiff(diffDate: Date):DateDivergence {
    return DateDivergence(DateOptUnit.DATE,diffDate)
}

fun Any.hourDiff(diffDate: Date):DateDivergence {
    return DateDivergence(DateOptUnit.HOUR,diffDate)
}

fun Any.minuteDiff(diffDate: Date):DateDivergence {
    return DateDivergence(DateOptUnit.MINUTE,diffDate)
}

fun Any.secondDiff(diffDate: Date):DateDivergence {
    return DateDivergence(DateOptUnit.SECOND,diffDate)
}

/**
 * date+1
 * 往后的几天
 */
operator fun Date.plus(nextVal:Int):Date{
    val calendar = GregorianCalendar()
    calendar.time = this
    calendar.add(Calendar.DATE, nextVal)
   return calendar.time
}

/**
 * date-1
 */
operator fun Date.minus(nextVal:Int):Date{
    val calendar = GregorianCalendar()
    calendar.time = this
    calendar.add(Calendar.DATE, nextVal*-1)
    return calendar.time
}


/**
 * date+year(3)
 * 往后的几天
 */
operator fun Date.plus(nextVal:DateOperator):Date{
    val calendar = GregorianCalendar()
    calendar.time = this
    calendar.add(nextVal.unit.parseType(), nextVal.value)
    return calendar.time
}

/**
 * date-month(4)
 */
operator fun Date.minus(nextVal:DateOperator):Date{
    val calendar = GregorianCalendar()
    calendar.time = this
    calendar.add(nextVal.unit.parseType(), nextVal.value*-1)
    return calendar.time
}

/**
 * 求两个日期的间隔
 */
operator fun Date.minus(diffDate:DateDivergence):Long{
    val compareDate = diffDate.diffDate
    if (diffDate.unit == DateOptUnit.DATE) {
        return ((time - compareDate.time) / (1000*60*60*24)).toLong()
    } else if (diffDate.unit == DateOptUnit.MONTH) {
        return ((get(0)-compareDate[0])*12 + get(1)-compareDate[1]).toLong()
    } else if (diffDate.unit == DateOptUnit.YEAR) {
        return (get(0)-compareDate[0]).toLong()
    } else if (diffDate.unit == DateOptUnit.HOUR) {
        return ((time - compareDate.time) / (1000*60*60)).toLong()
    } else if (diffDate.unit == DateOptUnit.MINUTE) {
        return ((time - compareDate.time) / (1000*60)).toLong()
    } else if (diffDate.unit == DateOptUnit.SECOND) {
        return ((time - compareDate.time) / 1000).toLong()
    }
    return 0
}

/**
 * 得到月末
 */
operator fun Date.inc():Date {
    val calendar = GregorianCalendar()
    calendar.time = this
    calendar.add(Calendar.MONTH, 1);
    calendar.set(Calendar.DAY_OF_MONTH, 0);
    return calendar.time
}

/**
 * 得到月初
 */
operator fun Date.dec():Date {
    val calendar = GregorianCalendar()
    calendar.time = this
    calendar.set(Calendar.DAY_OF_MONTH, 1);
    return calendar.time
}

/**
 * 取 年月日时分秒 0 - 5
 * 例如 2015-12-21 22:15:56
 * date[0]:2015  date[1]:12 date[2]:21 
 */
operator fun Date.get(position:Int):Int {
    val calendar = GregorianCalendar()
    calendar.time = this
    
    var value = 0
    when(position) {
        0 -> value = calendar.get(Calendar.YEAR)
        1 -> value = calendar.get(Calendar.MONTH)+1
        2 -> value = calendar.get(Calendar.DATE)
        3 -> value = calendar.get(Calendar.HOUR_OF_DAY)
        4 -> value = calendar.get(Calendar.MINUTE)
        5 -> value = calendar.get(Calendar.SECOND)
    }
    return value
}

/**
 * 比较2个日期
 * if(date1 > date2) {
 * }
 */
operator fun Date.compareTo(compareDate : Date):Int {
    return (time - compareDate.time).toInt()
}

operator fun Date.div(unit:DateOptUnit):Date {
    val calendar = Calendar.getInstance()
    calendar.time = this
    when(unit){
        DateOptUnit.DATE -> {
            calendar.set(calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),calendar.get(Calendar.DATE),0,0,0)
        }
        DateOptUnit.MONTH -> {
            calendar.set(calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),1,0,0,0)
        } 
        DateOptUnit.YEAR ->  {
            calendar.set(calendar.get(Calendar.YEAR),0,0,0,0,0)
        }
    }
    return calendar.time
}

/**
 * 日期转化为字符串
 */
fun Date.stringFormat(formatType:String):String{
    return SimpleDateFormat(formatType).format(this)
}

/**得到中文描述的星期*/
fun Date.chineseWeek(): String {
    val weekInfoArray = arrayOf("星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六")
    val calendar = Calendar.getInstance()
    calendar.time = this
    val weekIndex = calendar.get(Calendar.DAY_OF_WEEK) - 1
    return weekInfoArray[weekIndex]
}