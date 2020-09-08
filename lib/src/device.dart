part of nearby_connections;

class Device{
  String deviceID;
  String displayName;
  SessionState state = SessionState.notConnected;
  String message;

  Device();

  factory Device.fromJson(json){
   return Device()
    ..deviceID = json["deviceID"]
       ..displayName = json["displayName"];
 }
}