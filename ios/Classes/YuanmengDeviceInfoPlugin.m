#import "YuanmengDeviceInfoPlugin.h"
#import <yuanmeng_device_info/yuanmeng_device_info-Swift.h>

@implementation YuanmengDeviceInfoPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftYuanmengDeviceInfoPlugin registerWithRegistrar:registrar];
}
@end
