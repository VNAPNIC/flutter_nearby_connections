/// The class [Message] data transmitted between two peers.
/// it will contain [Message.deviceID] and [Message.message]
class Message {
  
  /// Sender's ID
  final String deviceID;

  /// Data will be kept here, in text type
  final String message;

  Message(this.deviceID, this.message);

  factory Message.fromJson(json) {
    return Message(json["deviceID"], json["message"]);
  }
}