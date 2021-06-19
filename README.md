# flutter_nearby_connections
flutter nearby connections

#### Plugin: [https://pub.dev/packages/flutter_nearby_connections](https://pub.dev/packages/flutter_nearby_connections)

### Flutter plugin supports peer-to-peer connectivity and discovers nearby devices for Android and IOS
The flutter_nearby_connections plugin supports the discovery of services provided by nearby devices.
Moreover, the flutter_nearby_connections plugin also supports communicating with those services through message-based data, streaming data, and resources (such as files). The framework uses infrastructure Wi-Fi networks, peer-to-peer Wi-Fi and Bluetooth Personal Area Networks (PAN) for the underlying transport over UDP.
The project is based on 

#### Android

[Nearby Connections API](https://developers.google.com/nearby/connections/overview) (Bluetooth & hotspot) Support [Strategy](https://pub.dev/documentation/flutter_nearby_connections/latest/flutter_nearby_connections/Strategy-class.html): ***Strategy.P2P_CLUSTER***, ***Strategy.P2P_STAR***, ***Strategy.P2P_POINT_TO_POINT***

[Wi-Fi P2P](https://developer.android.com/guide/topics/connectivity/wifip2p) (only wifi hotspot no internet) Support [Strategy](https://pub.dev/documentation/flutter_nearby_connections/latest/flutter_nearby_connections/Strategy-class.html): ***Strategy.Wi_Fi_P2P***

#### IOS

[Multipeer Connectivity](https://developer.apple.com/documentation/multipeerconnectivity)

We use the NearbyConnections API, but Flutter methods are based on the concept of [Multipeer Connectivity IOS](https://developer.apple.com/documentation/multipeerconnectivity).

Methods provided:

startAdvertisingPeer, startBrowsingForPeers, stopAdvertisingPeer

We separate the dependencies of the MCNearbyServiceAdvertiser, MCNearbyServiceBrowser and MCSession classes.  All of the methods will be implemented in the NearbyService class.

### Noted

* ##### Android doesn't support emulator only support real devices

* ##### On iOS 14, need to define in Info.plist

``` xml
    <key>NSBonjourServices</key>
    <array>
        <string>_{YOUR_SERVICE_TYPE}._tcp</string>
    </array>
    <key>UIRequiresPersistentWiFi</key>
    <true/>
    <key>NSBluetoothAlwaysUsageDescription</key>
    <string>{YOUR_DESCRIPTION}</string>
```

in this case, YOUR_SERVICE_TYPE is 'mp-connection' (you can define it)

``` dart
nearbyService.init(
        serviceType: 'mp-connection',
        strategy: Strategy.P2P_CLUSTER,
```

#### Test on IOS device

![The example app running in IOS](https://github.com/VNAPNIC/flutter_nearby_connections/blob/master/screen.gif?raw=true)

#### Test on Android device

![The example app running in Android](https://github.com/VNAPNIC/flutter_nearby_connections/blob/master/android-screen.gif?raw=true)

## Visitors Count
<img height="30px" src = "https://profile-counter.glitch.me/vnapnic/count.svg" alt ="Loading">
