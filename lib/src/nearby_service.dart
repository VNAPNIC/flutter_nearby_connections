part of nearby_connections;

typedef StateChangedCallback = Function(Device device);
typedef MessageReceivedCallback = Function(Device device);

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

  List<StateChangedCallback> _stateChangedCallbacks = [];
  List<MessageReceivedCallback> _messageReceivedCallbacks = [];

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

  void stateChangedSubject(StateChangedCallback callback) {
    _stateChangedCallbacks.add((device) => callback);
  }

  void messageReceivedSubject(MessageReceivedCallback callback) {
    _stateChangedCallbacks.add((device) => callback);
  }

  void unSubjectMessageReceived(MessageReceivedCallback callback) {
    _messageReceivedCallbacks.remove(callback);
  }

  void unSubjectStateChanged(StateChangedCallback callback) {
    _stateChangedCallbacks.remove(callback);
  }

  // ignore: missing_return
  void _handleMethodCall() {
    _channel.setMethodCallHandler((call) {
      switch (call.method) {
        case _invokeChangeStateMethod:
          _stateChangedCallbacks.forEach((element) { element.call(Device()); });
          break;
        case _invokeMessageReceiveMethod:
          _messageReceivedCallbacks.forEach((element) { element.call(Device()); });
          break;
      }
    });
  }
}
