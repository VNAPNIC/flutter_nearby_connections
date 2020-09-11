#import "FlutterNearbyConnectionsPlugin.h"
#if __has_include(<flutter_nearby_connections/flutter_nearby_connections-Swift.h>)
#import <flutter_nearby_connections/flutter_nearby_connections-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "flutter_nearby_connections-Swift.h"
#endif

@implementation FlutterNearbyConnectionsPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftFlutterNearbyConnectionsPlugin registerWithRegistrar:registrar];
}
@end
