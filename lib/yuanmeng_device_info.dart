import 'dart:async';
import 'dart:io';

import 'package:flutter/services.dart';
import 'package:device_info/device_info.dart';
import 'package:yuanmeng_device_info/ios_model.dart';

class YuanmengDeviceInfo {
  static DeviceInfoPlugin deviceInfo = DeviceInfoPlugin();
  static AndroidDeviceInfo androidInfo;
  static IosDeviceInfo iosInfo;

  static Future<YuanmengDeviceInfo> init() async {
    if (Platform.isAndroid) {
      androidInfo = await deviceInfo.androidInfo;
    } else {
      iosInfo = await deviceInfo.iosInfo;
    }
    return YuanmengDeviceInfo();
  }

  static const MethodChannel _channel =
      const MethodChannel('yuanmeng_device_info');

  static void get platformVersion async {
    DeviceInfoPlugin deviceInfo = DeviceInfoPlugin();

    if (Platform.isIOS) {
      IosDeviceInfo iosInfo = await deviceInfo.iosInfo;
      print('Running on ${iosInfo}');
    } else {
      AndroidDeviceInfo androidInfo = await deviceInfo.androidInfo;
      print('Running on ${androidInfo}'); // e.g. "Moto G (4)"
    }
  }

  static String get platform {
    return Platform.operatingSystem;
  }

  /* //手机品牌
  String get brand {
    return Platform.isIOS ? "iphone" : androidInfo.brand;
  }
*/
  //手机型号
  String get model {
    return Platform.isIOS
        ? iosModel[iosInfo.utsname.machine]
        : androidInfo.model + androidInfo.brand;
  }

  //是否是模拟器
  String get isPhysicalDevice {
    return (Platform.isIOS
            ? iosInfo.isPhysicalDevice
            : androidInfo.isPhysicalDevice)
        ? "1"
        : "0";
  }

  //idfa(ios only)
  Future<String> get idfa async {
    return await _channel.invokeMethod<String>("idfa");
  }

  //idfv(ios only)
  String get idfv {
    return iosInfo.identifierForVendor;
  }

  //安卓唯一标识  seriaNumber
  String get unique_id {
    return androidInfo.androidId;
  }

  //安卓唯一标识
  Future<String> get imsi async {
    return await _channel.invokeMethod<String>("imsi");
  }
/*
  //安卓通话记录
  Future<List<dynamic>> get callRecords async {
    return await _channel.invokeMethod<List<dynamic>>("callRecords");
  }*/

  //安卓getAppList
  Future<String> get getAppList async {
    return (await _channel.invokeMethod<String>("getAppList")).toString();
  }

  //安卓 getWifiMac
  Future<String> get getWifiMac async {
    return (await _channel.invokeMethod<String>("getWifiMac")).toString();
  }
}
