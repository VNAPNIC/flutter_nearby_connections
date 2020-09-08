part of nearby_connections;

typedef StateChangedCallback = Function(List<Device> devices);
typedef MessageReceivedCallback = Function(Device devices);

class StateChangedObserver extends ValueNotifier<List<Device>> {
  StateChangedObserver() : super([]);
}

class MessageReceivedObserver extends ValueNotifier<Device> {
  MessageReceivedObserver() : super(Device());
}


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

enum SessionState{
  notConnected,
  connecting,
  connected
}

class NearbyService {
  static const MethodChannel _channel =
      const MethodChannel('nearby_connections');

  final _stateChangedCallbacks = <String,StateChangedCallback>{};

  final _messageReceivedCallbacks = <String,MessageReceivedCallback>{};

  StateChangedObserver _stateChangedObserver = StateChangedObserver();
  MessageReceivedObserver _messageReceivedObserver = MessageReceivedObserver();

  NearbyService({@required String serviceType}) : assert (serviceType.length <= 15){
    _channel.invokeMethod(_initNearbyService, serviceType);

    _handleMethodCall();
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
  
  void sendMessage(String deviceID, String message){
    _channel.invokeMethod(_sendMessage, "{"
        "\"deviceID\":\"deviceID\","
        "\"message\":\"message\""
        "}");
  }

  void stateChangedSubject({@required String tag,@required StateChangedCallback callback}) {
    _stateChangedCallbacks[tag] = callback;
  }

  void messageReceivedSubject({@required String tag,@required MessageReceivedCallback callback}) {
    _messageReceivedCallbacks[tag] = callback;
  }

  void unSubjectStateChanged({@required String tag}) {
    _stateChangedCallbacks.remove(tag);
  }

  void unSubjectMessageReceived({@required String tag}) {
    _messageReceivedCallbacks.remove(tag);
  }

  // ignore: missing_return
  void _handleMethodCall() {
    _channel.setMethodCallHandler((call) {
      switch (call.method) {
        case _invokeChangeStateMethod:
//          final json = jsonDecode(call.arguments as String) as List;
//          List<Device> devies = json.map((m) => Device.fromJson(m));
          List<Device> devices = jsonDecode(call.arguments).map<Device>((dynamic device) => Device.fromJson(device)).toList();
          _stateChangedObserver.value = devices;
          break;
        case _invokeMessageReceiveMethod:
          _messageReceivedObserver.value = Device();
          break;
      }
    });

    _stateChangedObserver.addListener(() {
      _stateChangedCallbacks.forEach((key, value) {
        value(_stateChangedObserver.value);
      });
      _messageReceivedCallbacks.forEach((key, value) {
        value(Device());
      });
    });
  }
}
