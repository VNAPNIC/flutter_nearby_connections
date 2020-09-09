part of nearby_connections;

enum SessionState { notConnected, connecting, connected }

class Device {
  String deviceID;
  String displayName;
  SessionState state = SessionState.notConnected;

  Device(this.deviceID, this.displayName, int state) {
    switch (state) {
      case 1:
        this.state = SessionState.connecting;
        break;
      case 2:
        this.state = SessionState.connected;
        break;
      default:
        this.state = SessionState.notConnected;
        break;
    }
  }

  factory Device.fromJson(json) {
    return Device(json["deviceID"], json["displayName"], json["state"]);
  }
}
