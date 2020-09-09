class Data {
  final String deviceID;
  final String message;

  Data(this.deviceID, this.message);

  factory Data.fromJson(json) {
    return Data(json["deviceID"], json["message"]);
  }
}
