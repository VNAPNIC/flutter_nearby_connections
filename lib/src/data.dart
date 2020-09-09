/// The class [Data] data transmitted between two peers.
/// it will contain [Data.deviceID] and [Data.message]
class Data {
  /// Sender's ID
  final String deviceID;
  /// Data will be kept here, in text type
  final String message;

  Data(this.deviceID, this.message);

  factory Data.fromJson(json) {
    return Data(json["deviceID"], json["message"]);
  }
}