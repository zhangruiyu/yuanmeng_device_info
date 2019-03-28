package com.example.yuanmeng_device_info

import java.text.SimpleDateFormat
import java.util.*

object TimeFormatUtils {

    //    val ONE_DAY_MILLIS = 86400000
//    val HALF_DAY_MILLIS = 43200000
//    val TEN_SECOND_MILLIS = 10000
//    val ONE_HOUR_MILLIS = 3600000
    private var mTodayZero: Date? = null
    private var mYesterdayZero: Date? = null
//    private var mCurTime: Long = 0L

    fun getTimeString(date: Long, format: String): String {
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        val dt = Date(date)
        return sdf.format(dt)
    }

    fun getYear() = SimpleDateFormat("yyyy").format(Date())

    fun getMonth() = SimpleDateFormat("MM").format(Date())

    fun getDay() = SimpleDateFormat("dd").format(Date())

    /***
     * 获取指定日后 后 dayAddNum 天的 日期
     * @param day  日期，格式为String："2013-9-3";
     * @param dayAddNum 增加天数 格式为int;
     * @return
     */
    fun getDateStr(dayAddNum: Long, format: String): String {
        val newDate2 = Date(Date().time + dayAddNum * 24 * 60 * 60 * 1000)
        val simpleDateFormat = SimpleDateFormat(format)
        val dateOk = simpleDateFormat.format(newDate2)
        return dateOk
    }

    fun stringToLong(strTime: String, formatType: String): Long {
        val date = stringToDate(strTime, formatType)
        return if (date == null) {
            0L
        } else {
            dateToLong(date)
        }
    }

    fun stringToDate(strTime: String, formatType: String): Date? {
        val formatter = SimpleDateFormat(formatType, Locale.getDefault())
        var date: Date? = null
        date = formatter.parse(strTime)
        return date
    }

    fun dateToLong(date: Date): Long {
        return date.time
    }

    //将时间至零
    fun getTodayZero(): Date {
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return Date(c.timeInMillis)
    }

    //格式化时间
    fun timeFormatSmsTimeLine(time: Long, showYear: Boolean): String {
        val value = Date(time)
        return if (showYear) {
            getTimeString(time, "yyyy年M月d日 HH:mm")
        } else if (value.before(mYesterdayZero)) {
            getTimeString(time, "M月d日 HH:mm:ss")
        } else {
            if (value.before(mTodayZero)) String.format("%s %s", "昨天", getTimeString(time, "HH:mm:ss"))
            else getTimeString(time, "HH:mm:ss")
        }
    }

    //计算时长
    fun FormatDuration(duration: Long): String {
        if (duration < 60L) {
            return Math.max(1L, duration).toString() + "秒"
        } else {
            val minute = duration / 60L
            val second = duration % 60L
            val result = StringBuffer()
            result.append(minute.toString() + "分")
            if (second > 0L) {
                result.append(second.toString() + "秒")
            }

            return result.toString()
        }
    }

    fun long2date(time: Long): Date {
        return Date(time)
    }


    private fun getYesterdayZero(time: Date): Date {
        return Date(time.time - 86400000L)
    }

    fun getNextDayOf24(timeMillis: Long): Date {
        val cal = Calendar.getInstance()
        cal.time = Date(timeMillis)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.add(Calendar.DAY_OF_MONTH, 1)
        return cal.time
    }


    fun formatTime(date: Long): String? {
        val dt = Date(date)
        val hour = dt.hours
        return if (hour in 1..22) {
            if (hour in 1..6) "凌晨" else null
        } else {
            "深夜"
        }
    }

    fun formatWeiBoTime(time: Long): String {
        val duration = System.currentTimeMillis() - time
        if (duration <= 3600000L) {
            return (duration / 60000L).toString() + "分钟前"
        } else {
            val value = Date(time)
            if (duration > 3600000L && value.after(mTodayZero)) {
                return (duration / 3600000L).toString() + "小时前"
            } else if (value.before(mTodayZero) && value.after(mYesterdayZero)) {
                return "昨天"
            } else {
                val beforeYestday = getYesterdayZero(mYesterdayZero!!)
                if (value.before(mYesterdayZero) && value.after(beforeYestday)) {
                    return "前天"
                } else {
                    val duration_day = duration / 86400000L
                    if (duration_day < 15L) {
                        return duration_day.toString() + "天前"
                    } else if (duration_day in 15L..29) {
                        return "半个月前"
                    } else {
                        val duration_month = duration_day / 30L
                        return if (duration_month < 6L) {
                            duration_month.toString() + "个月前"
                        } else if (duration_month in 6L..11) {
                            "半年前"
                        } else {
                            val duration_year = duration_month / 12L
                            duration_year.toString() + "年前"
                        }
                    }
                }
            }
        }
    }
}