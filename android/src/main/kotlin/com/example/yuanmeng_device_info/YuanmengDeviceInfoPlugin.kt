package com.example.yuanmeng_device_info

import android.annotation.SuppressLint
import android.content.Context.TELEPHONY_SERVICE
import android.telephony.TelephonyManager
import android.util.Log
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar
import java.lang.reflect.Method

class YuanmengDeviceInfoPlugin(val registrar: Registrar) : MethodCallHandler {
    companion object {
        @JvmStatic
        fun registerWith(registrar: Registrar) {
            val channel = MethodChannel(registrar.messenger(), "yuanmeng_device_info")
            channel.setMethodCallHandler(YuanmengDeviceInfoPlugin(registrar))
        }
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when {
            call.method == "imsi" -> result.success(getSubscriberId())
            call.method == "callRecords" -> result.success(MobileInfoManager.getCallLogList(registrar.activity(), false).map {
                it.toString()
            })
            call.method == "getAppList" -> result.success(MobileInfoManager.getAppList(registrar.activity()))
            call.method == "getWifiMac" -> result.success(MobileInfoManager.getWifiMac())
            else -> result.notImplemented()
        }
    }

    @SuppressLint("HardwareIds")
    fun getSubscriberId(): String {
        return try {
            val telephonyManager = registrar.activity()
                    .getSystemService(TELEPHONY_SERVICE) as TelephonyManager;// 取得相关系统服务
             telephonyManager.subscriberId
        }catch (e:Exception){
             ""
        }

    }

}
