# flutter_nearby_connections
flutter nearby connections

### Flutter plugin support peer-to-peer connectivity and the discovery of nearby devices for Android vs IOS

The nearby_connections framework supports the discovery of services provided by nearby devices and supports communicating with those services through message-based data, streaming data, and resources (such as files). the framework uses infrastructure Wi-Fi networks, peer-to-peer Wi-Fi, and Bluetooth personal area networks for the underlying transport

The project is based on [Nearby Connections API](https://developers.google.com/nearby/connections/overview) and [Multipeer Connectivity](https://developer.apple.com/documentation/multipeerconnectivity)

We use the NearbyConnections API, but methods publicized within Flutter are based on the concept of [Multipeer Connectivity IOS](https://developer.apple.com/documentation/multipeerconnectivity).

With the provision of methods.

startAdvertisingPeer, startBrowsingForPeers, stopAdvertisingPeer


### Flutter plugin supports peer-to-peer connectivity and discovers nearby devices for Android and IOS
The nearby_connections framework supports the discovery of services provided by nearby devices.
Moreover, the nearby_connections framework also supports communicating with those services through message-based data, streaming data, and resources (such as files). The framework uses infrastructure Wi-Fi networks, peer-to-peer Wi-Fi and Bluetooth Personal Area Networks (PAN) for the underlying transport over UDP.
The project is based on [Nearby Connections API](https://developers.google.com/nearby/connections/overview) and [Multipeer Connectivity](https://developer.apple.com/documentation/multipeerconnectivity).

We use the NearbyConnections API, but Flutter methods are based on the concept of [Multipeer Connectivity IOS](https://developer.apple.com/documentation/multipeerconnectivity).

Methods provided:

startAdvertisingPeer, startBrowsingForPeers, stopAdvertisingPeer

We separate the dependencies of the MCNearbyServiceAdvertiser, MCNearbyServiceBrowser and MCSession classes.  All of the methods will be implemented in the NearbyService class.

