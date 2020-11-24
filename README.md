# flutter_nearby_connections
flutter nearby connections

#### Plugin: [https://pub.dev/packages/flutter_nearby_connections](https://pub.dev/packages/flutter_nearby_connections)

### Flutter plugin supports peer-to-peer connectivity and discovers nearby devices for Android and IOS

#### Android doesn't support emulator only support real devices

The flutter_nearby_connections plugin supports the discovery of services provided by nearby devices.
Moreover, the flutter_nearby_connections plugin also supports communicating with those services through message-based data, streaming data, and resources (such as files). The framework uses infrastructure Wi-Fi networks, peer-to-peer Wi-Fi and Bluetooth Personal Area Networks (PAN) for the underlying transport over UDP.
The project is based on [Nearby Connections API](https://developers.google.com/nearby/connections/overview) and [Multipeer Connectivity](https://developer.apple.com/documentation/multipeerconnectivity).

We use the NearbyConnections API, but Flutter methods are based on the concept of [Multipeer Connectivity IOS](https://developer.apple.com/documentation/multipeerconnectivity).

Methods provided:

startAdvertisingPeer, startBrowsingForPeers, stopAdvertisingPeer

We separate the dependencies of the MCNearbyServiceAdvertiser, MCNearbyServiceBrowser and MCSession classes.  All of the methods will be implemented in the NearbyService class.

#### Test on IOS device

![The example app running in IOS](https://github.com/VNAPNIC/flutter_nearby_connections/blob/master/screen.gif?raw=true)

