#import "NearbyConnectionsPlugin.h"
#if __has_include(<nearby_connections/nearby_connections-Swift.h>)
#import <nearby_connections/nearby_connections-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "nearby_connections-Swift.h"
#endif

@implementation NearbyConnectionsPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftNearbyConnectionsPlugin registerWithRegistrar:registrar];
}
@end
