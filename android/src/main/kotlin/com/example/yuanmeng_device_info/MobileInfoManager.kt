package com.example.yuanmeng_device_info

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.ConnectivityManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.CallLog
import android.provider.ContactsContract
import android.telephony.TelephonyManager
import android.util.Base64
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketException
import java.util.*
import kotlin.collections.ArrayList


/**
 *  TODO 获取手机设备信息
 */
object MobileInfoManager {

    /**
     * 获取当前手机系统语言。
     *
     * @return 返回当前系统语言。例如：当前设置的是“中文-中国”，则返回“zh-CN”
     */
    fun getSystemLanguage() = Locale.getDefault().language!!

    /**
     * 获取当前系统上的语言列表(Locale列表)
     *
     * @return  语言列表
     */
    fun getSystemLanguageList(): Array<out Locale> = Locale.getAvailableLocales()!!

    /**
     * 获取当前手机系统版本号
     *
     * @return  系统版本号
     */
    fun getSystemVersion() = android.os.Build.VERSION.RELEASE

    /**
     * 获取当前手机系统dns
     *
     * @return  系统dns
     */
    fun getLocalDNS(): String {
        var cmdProcess: Process? = null
        var reader: BufferedReader? = null
        var dnsIP = ""
        try {
            cmdProcess = Runtime.getRuntime().exec("getprop net.dns1")
            reader = BufferedReader(InputStreamReader(cmdProcess!!.inputStream))
            dnsIP = reader.readLine()
            return dnsIP
        } catch (e: IOException) {
            return ""
        } finally {
            try {
                reader!!.close()
            } catch (e: IOException) {
            }
            cmdProcess!!.destroy()
        }
    }

    //内存卡容量
    fun getSDSpace(): ArrayList<Long> {
        val state = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED == state) {
            val sdcardDir = Environment.getExternalStorageDirectory()
            val sf = StatFs(sdcardDir.path)
            val blockSize = sf.blockSize.toLong()
            val blockCount = sf.blockCount.toLong()
            val availCount = sf.availableBlocks.toLong()
            Log.d("", "block大小:" + blockSize + ",block数目:" + blockCount + ",总大小:" + blockSize * blockCount / 1024 + "KB")
            Log.d("", "可用的block数目：:" + availCount + ",剩余空间:" + availCount * blockSize / 1024 + "KB")

            val total = blockSize * blockCount
            val avail = availCount * blockSize

            return arrayListOf(total, avail, total - avail)
        }
        return arrayListOf(0, 0, 0)
    }

    //读取系统内部空间
    fun getSystemCardSpace(): ArrayList<Long> {
        val root = Environment.getRootDirectory()
        val sf = StatFs(root.path)
        val blockSize = sf.blockSize.toLong()
        val blockCount = sf.blockCount.toLong()
        val availCount = sf.availableBlocks.toLong()
        Log.d("", "block大小:" + blockSize + ",block数目:" + blockCount + ",总大小:" + blockSize * blockCount / 1024 + "KB")
        Log.d("", "可用的block数目：:" + availCount + ",可用大小:" + availCount * blockSize / 1024 + "KB")

        val total = blockSize * blockCount
        val avail = availCount * blockSize

        return arrayListOf(total, total - avail, avail)
    }


    /**
     * 获取手机内存大小
     *
     * @return  手机内存大小
     */
    fun getSystemMemory(context: Activity): Long {
        val memoryInfo = ActivityManager.MemoryInfo()
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        manager.getMemoryInfo(memoryInfo)
        return memoryInfo.availMem
    }

    /**
     * 获取手机型号
     *
     * @return  手机型号
     */
    fun getSystemModel() = android.os.Build.MODEL

    /**
     * 获取手机厂商
     *
     * @return  手机厂商
     */
    fun getDeviceBrand() = android.os.Build.BRAND

    /**
     * 获取手机IMEI(需要“android.permission.READ_PHONE_STATE”权限)
     *
     * @return  手机IMEI
     */
    @SuppressLint("MissingPermission", "HardwareIds")
    fun getIMEI(ctx: Context): String {
        return (ctx.getSystemService(Activity.TELEPHONY_SERVICE) as TelephonyManager).deviceId
    }

    /**
     * 判断是否打开了特定权限
     */
    fun isOpenPermisson(context: Context, permisson: String) =
            ActivityCompat.checkSelfPermission(context, permisson) == PackageManager.PERMISSION_GRANTED


    /**
     * 打开特定权限
     */
    fun openPermisson(context: Activity, permisson: Array<String>) {
        // 如果应用之前请求过此权限但用户拒绝了请求，此方法将返回 true。
//        if (shouldShowRequestPermissionRationale(context,
//                        permisson)) {//这里可以写个对话框之类的项向用户解释为什么要申请权限，并在对话框的确认键后续再次申请权限
//            shouldShowRequestPermissionRationale(context, permisson, 100)
//            PermissionUtils.shouldShowRationale(context, 100, permisson)
//        } else {
//            //申请权限，字符串数组内是一个或多个要申请的权限，1是申请权限结果的返回参数，在onRequestPermissionsResult可以得知申请结果
//            ActivityCompat.requestPermissions(context,permisson, 100)
//        }
        ActivityCompat.requestPermissions(context, permisson, 100)
    }


    /**
     * 获取手机通讯录
     *
     * @return  通讯录列表
     */
    data class ContactsInfo(var vname: String, var number: String, var sortKey: String, var id: Int)

    fun getContactsList(context: Activity): MutableList<ContactsInfo> {
        if (!isOpenPermisson(context, Manifest.permission.READ_CONTACTS)) {
            openPermisson(context, arrayOf(Manifest.permission.READ_CONTACTS))
        }
        try {
            val list = mutableListOf<ContactsInfo>()
            val contactUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
            val cursor = context.contentResolver.query(contactUri,
                    arrayOf("display_name", "sort_key", "contact_id", "data1"),
                    null, null, "sort_key")
            Log.d("cursor", "${cursor.count}")
            while (cursor.moveToNext()) {
                ContactsInfo(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)),
                        cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).replace(" ", ""),
                        if (cursor.getString(1).substring(0, 1).toUpperCase().matches("[A-Z]".toRegex())) {
                            cursor.getString(1).substring(0, 1).toUpperCase()
                        } else "#",
                        cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID))).run {
                    list.add(this)
                }
            }
            //在android 4.0及其以上的版本中，Cursor会自动关闭，不需要用户自己关闭
            if (Build.VERSION.SDK_INT < 14)
                cursor.close()//使用完后一定要将cursor关闭，不然会造成内存泄露等问题
            return list
        } catch (e: Exception) {
            e.printStackTrace()

            return mutableListOf()
        }
    }

    /**
     * 获取手机通话记录
     *
     * @param isDesc是否降序排列（从最新的开始）
     *
     * CallLog.Calls.CACHED_FORMATTED_NUMBER      通话记录格式化号码
     * CallLog.Calls.CACHED_MATCHED_NUMBER     通话记录为格式化号码
     * CallLog.Calls.CACHED_NAME     联系人名称
     * CallLog.Calls.TYPE    通话类型
     * CallLog.Calls.DATE    通话时间(long型)
     * CallLog.Calls.DURATION     通话时长(秒为单位)
     * CallLog.Calls.GEOCODED_LOCATION    运营商地址(如：浙江杭州)
     * ----通话类型-----
     * CallLog.Calls.INCOMING_TYPE      呼入
     * CallLog.Calls.OUTGOING_TYPE      呼出
     * CallLog.Calls.MISSED_TYPE       未接
     *
     * @return  通话记录列表
     */
    data class CallRecord(var formatted_number: String = "", var matched_number: String = "",
                          var name: String = "", var type: String = "", var date: String = "",
                          var duration: String = "", var location: String = "") {
        override fun toString(): String {
            return "{\"phone_num\":\"$formatted_number\"," +
                    "\"call_type\":\"$type\"," +
                    "\"duration\":\"$duration\"," +
                    "\"date\":\"$date\"," +
                    "\"obtain_time\":\"${TimeFormatUtils.timeFormatSmsTimeLine(System.currentTimeMillis(), true)}\"" +
                    "}"
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    @SuppressLint("MissingPermission")
    fun getCallLogList(context: Activity, isDesc: Boolean): MutableList<CallRecord> {
        if (!isOpenPermisson(context, Manifest.permission.READ_CALL_LOG)) {
            openPermisson(context, arrayOf(Manifest.permission.READ_CALL_LOG))
        }
        val resolver = context.contentResolver
        // uri的写法需要查看源码JB\packages\providers\ContactsProvider\AndroidManifest.xml中内容提供者的授权
        val projection = arrayOf(
                CallLog.Calls.CACHED_FORMATTED_NUMBER,
                CallLog.Calls.CACHED_MATCHED_NUMBER,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION,
                CallLog.Calls.GEOCODED_LOCATION
        )
        val list = mutableListOf<CallRecord>()
        // 从清单文件可知该提供者是CallLogProvider，且通话记录相关操作被封装到了Calls类中
        val cursor: Cursor = resolver.query(CallLog.Calls.CONTENT_URI, projection, null,
                null, if (isDesc) "date DESC" else "") ?: return list
        try {

            while (cursor.moveToNext()) {
                val name = if (cursor.getString(2) == null) "" else cursor.getString(2)
                val record = CallRecord(cursor.getString(0) ?: "", cursor.getString(1) ?: "", name,
                        when (cursor.getInt(3)) {
                            CallLog.Calls.INCOMING_TYPE -> "来电"
                            CallLog.Calls.OUTGOING_TYPE -> "去电"
                            CallLog.Calls.MISSED_TYPE -> "未接"
                            else -> {
                                ""
                            }
                        }, TimeFormatUtils.timeFormatSmsTimeLine(cursor.getLong(4), true),
                        TimeFormatUtils.FormatDuration(cursor.getLong(5)),
                        cursor.getString(6) ?: "")
                list.add(record)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            //在android 4.0及其以上的版本中，Cursor会自动关闭，不需要用户自己关闭
            if (Build.VERSION.SDK_INT < 14)
                cursor.close()
        }
        return list
    }


    //选择通讯录的联系人
    fun fetchContantsInfo(activity: Activity, intent: Intent): MutableList<String> {
        if (!isOpenPermisson(activity, Manifest.permission.READ_CONTACTS)) {
            openPermisson(activity, arrayOf(Manifest.permission.READ_CONTACTS))
        }
        try {
            val reContentResolverol = activity.contentResolver
            val contactData = intent.data
            val cursor = activity.managedQuery(contactData, null, null, null, null)
            cursor.moveToFirst()
            val username = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
            val contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
            val phone = reContentResolverol.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null)
            var usernumber = ""
            while (phone!!.moveToNext()) {
                usernumber = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            }

            //在android 4.0及其以上的版本中，Cursor会自动关闭，不需要用户自己关闭
            if (Build.VERSION.SDK_INT < 14) {
                try {
                    phone.close()
                    cursor?.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return mutableListOf(username, usernumber)
        } catch (e: Exception) {
            return mutableListOf()
        }
    }

    fun getUUIDString(mContext: Context): String? {
        var uuid = UUID.randomUUID().toString()
        uuid = Base64.encodeToString(uuid.toByteArray(), Base64.DEFAULT)
        return uuid
    }


    fun getPhoneNumber(mContext: Context): String {
        val phoneMgr = mContext
                .getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ""
        } else phoneMgr.line1Number
    }

    fun getDeviceID(mContext: Context): String {
        val tm = mContext
                .getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ""
        } else tm.deviceId
    }

    fun getMacAddress(mContext: Context): String? {
        val wifi = mContext.applicationContext
                .getSystemService(Context.WIFI_SERVICE) as WifiManager
        val info = wifi.connectionInfo
        var address: String? = info.macAddress
        if (address != null && address.isNotEmpty()) {
            address = address.replace(":", "")
        }
        return address
    }

    fun getAppList(mContext: Context): String {
        val pm = mContext.packageManager
        // Return a List of all packages that are installed on the device.
        val packages = pm.getInstalledPackages(0)

        return packages.mapNotNull {
            // 判断系统/非系统应用
            if (it.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0)
            // 非系统应用
            {
                mapOf<String, String>(
                        "\"version_code\"" to "\"it.versionCode.toString()\"",
                        "\"package_name\"" to "\"it.packageName\"",
                        "\"app_name\"" to "\"it.packageName\"",
                        "\"obtain_time\"" to "\"TimeFormatUtils.timeFormatSmsTimeLine(System.currentTimeMillis(), true)\"",
                        "\"appVersion\"" to "\"it.versionName\""
                ).toString().replace("=",":")

            } else {
                // 系统应用
                null
            }
        }.toString()

    }

    fun getWifiMac():String {
        try {
            val all = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (nif in all) {
                if (!nif.name.equals("wlan0", ignoreCase = true)) continue

                val macBytes = nif.hardwareAddress ?: return ""

                val res1 = StringBuilder()
                for (b in macBytes) {
                    res1.append(String.format("%02X:", b))
                }

                if (res1.length > 0) {
                    res1.deleteCharAt(res1.length - 1)
                }
                return res1.toString()
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return ""
    }
}