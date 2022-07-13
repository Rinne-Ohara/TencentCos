#import "TencentCosPlugin.h"
#if __has_include(<tencent_cos/tencent_cos-Swift.h>)
#import <tencent_cos/tencent_cos-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "tencent_cos-Swift.h"
#endif

@implementation TencentCosPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftTencentCosPlugin registerWithRegistrar:registrar];
}
@end
