part of flutter_nearby_connections;

/// Nearby Connections supports different Strategies for advertising and discovery. The best Strategy to use depends on the use case.
/// only for Android OS
enum Strategy {
  /// [P2P_CLUSTER] is a peer-to-peer strategy that supports an M-to-N, or cluster-shaped, connection topology. In other words, this enables connecting amorphous clusters of devices within radio range (~100m), where each device can both initiate outgoing connections to M other devices and accept incoming connections from N other devices.
  ///
  /// This is the default strategy, equivalent to calling the deprecated Connections API methods with no Strategy parameter.
  ///
  /// This strategy is more flexible in its topology constraints than [P2P_STAR], but results in lower bandwidth connections. It is good for use cases with smaller payloads that require a more mesh-like experience, such as multiplayer gaming.
  ///
  /// Permissions
  /// In order to use this Strategy, Location must be turned on and the app must have the following permissions declared:
  ///
  /// [BLUETOOTH]
  /// [BLUETOOTH_ADMIN]
  /// [ACCESS_WIFI_STATE]
  /// [CHANGE_WIFI_STATE]
  /// [ACCESS_COARSE_LOCATION]
  /// Additionally, on devices running Q (and onwards), [FINE_LOCATION] is required in place of [COARSE] location.
  ///
  /// [ACCESS_FINE_LOCATION]
  P2P_CLUSTER,

  /// [P2P_STAR] is a peer-to-peer strategy that supports a 1-to-N, or star-shaped, connection topology. In other words, this enables connecting devices within radio range (~100m) in a star shape, where each device can, at any given time, play the role of either a hub (where it can accept incoming connections from N other devices), or a spoke (where it can initiate an outgoing connection to a single hub), but not both.
  ///
  /// This strategy lends itself best to situations where there is one device advertising, and N devices which discover the advertiser, though you may still advertise and discover simultaneously if required.
  ///
  /// This strategy is more strict in its topology constraints than [P2P_CLUSTER], but results in higher bandwidth connections. It is good for high-bandwidth use cases such as sharing a video to a group of friends.
  ///
  /// Permissions
  /// In order to use this Strategy, Location must be turned on and the app must have the following permissions declared:
  ///
  /// [BLUETOOTH]
  /// [BLUETOOTH_ADMIN]
  /// [ACCESS_WIFI_STATE]
  /// [CHANGE_WIFI_STATE]
  /// [ACCESS_COARSE_LOCATION]
  /// Additionally, on devices running Q (and onwards), [FINE_LOCATION] is required in place of [COARSE] location.
  ///
  /// [ACCESS_FINE_LOCATION]
  P2P_STAR,

  /// [P2P_POINT_TO_POINT] is a peer-to-peer strategy that supports a 1-to-1 connection topology. In other words, this enables connecting devices within radio range (~100m) with the highest possible throughput, but does not allow for more than a single connection at a time.
  ///
  /// This strategy lends itself best to situations where transferring data is more important than the flexibility of maintaining multiple connections.
  ///
  /// This strategy is more strict in its topology constraints than [P2P_STAR], but results in higher bandwidth connections. It is good for high-bandwidth use cases such as sharing a large video to another device.
  ///
  /// Permissions
  /// In order to use this Strategy, Location must be turned on and the app must have the following permissions declared:
  ///
  /// [BLUETOOTH]
  /// [BLUETOOTH_ADMIN]
  /// [ACCESS_WIFI_STATE]
  /// [CHANGE_WIFI_STATE]
  /// [ACCESS_COARSE_LOCATION]
  /// Additionally, on devices running Q (and onwards), [FINE_LOCATION] is required in place of [COARSE] location.
  ///
  /// [ACCESS_FINE_LOCATION]
  P2P_POINT_TO_POINT,

  /// (P2P) allows Android 4.0 (API level 14) and higher devices with the appropriate hardware to connect directly to each other via Wi-Fi without an intermediate access point.
  /// Using these APIs, you can discover and connect to other devices when each device supports Wi-Fi P2P, then communicate over a speedy connection across distances much longer than a Bluetooth connection.
  /// This is useful for applications that share data among users, such as a multiplayer game or a photo sharing application.
  Wi_Fi_P2P,
}
