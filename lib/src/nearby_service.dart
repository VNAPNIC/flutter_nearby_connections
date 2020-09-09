part of nearby_connections;

const _initNearbyService = 'init_nearby_service';
const _startAdvertisingPeer = 'start_advertising_peer';
const _stopAdvertisingPeer = 'stop_advertising_peer';
const _startBrowsingForPeers = 'start_browsing_for_peers';
const _stopBrowsingForPeers = 'stop_browsing_for_peers';
const _invitePeer = 'invite_peer';
const _uninvitedPeer = 'uninvited_peer';
const _sendMessage = 'send_message';
const _invokeChangeStateMethod = "invoke_change_state_method";
const _invokeMessageReceiveMethod = "invoke_message_receive_method";

typedef StateChangedCallback = Function(List<Device> arguments);
typedef DataReceivedCallback = Function(Data data);

class NearbyService {
  static const MethodChannel _channel =
      const MethodChannel('nearby_connections');

  final _stateChangedController = StreamController<List<Device>>.broadcast();

  Stream<List<Device>> get _stateChangedStream =>
      _stateChangedController.stream;

  final _dataReceivedController = StreamController<Data>.broadcast();

  Stream<Data> get _dataReceivedStream => _dataReceivedController.stream;

  NearbyService({@required String serviceType})
      : assert(serviceType.length <= 15) {
    _channel.invokeMethod(_initNearbyService, serviceType);
    // ignore: missing_return
    _channel.setMethodCallHandler((call) {
      switch (call.method) {
        case _invokeChangeStateMethod:
          List<Device> devices = jsonDecode(call.arguments)
              .map<Device>((dynamic device) => Device.fromJson(device))
              .toList();
          _stateChangedController.add(devices);
          break;
        case _invokeMessageReceiveMethod:
          Data data = Data.fromJson(jsonDecode(call.arguments));
          _dataReceivedController.add(data);
          break;
      }
    });
  }

  void startAdvertisingPeer() {
    _channel.invokeMethod(_startAdvertisingPeer);
  }

  void startBrowsingForPeers() {
    _channel.invokeMethod(_startBrowsingForPeers);
  }

  void stopAdvertisingPeer() {
    _channel.invokeMethod(_stopAdvertisingPeer);
  }

  void stopBrowsingForPeers() {
    _channel.invokeMethod(_stopBrowsingForPeers);
  }

  void inviteDevice({@required String deviceID}) {
    _channel.invokeMethod(_invitePeer, deviceID);
  }

  void uninvitedDevice({@required String deviceID}) {
    _channel.invokeMethod(_uninvitedPeer, deviceID);
  }

  void sendMessage(String deviceID, String argument) {
    _channel.invokeMethod(
        _sendMessage,
        "{"
        "\"deviceID\":\"deviceID\","
        "\"message\":\"message\""
        "}");
  }

  StreamSubscription stateChangedSubscription(
          {@required StateChangedCallback callback}) =>
      _stateChangedStream.listen(callback);

  StreamSubscription dataReceivedSubscription(
          {@required DataReceivedCallback callback}) =>
      _dataReceivedStream.listen(callback);
}
