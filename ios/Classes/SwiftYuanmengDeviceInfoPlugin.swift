import Flutter
import UIKit
import AdSupport

public class SwiftYuanmengDeviceInfoPlugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "yuanmeng_device_info", binaryMessenger: registrar.messenger())
    let instance = SwiftYuanmengDeviceInfoPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    if(call.method == "idfa"){
        result(ASIdentifierManager.shared().advertisingIdentifier.uuidString);
    }else if(call.method == "idfv"){
        result(UIDevice.current.identifierForVendor?.uuidString);
    }
    result("iOS " + UIDevice.current.systemVersion)
  }
}
