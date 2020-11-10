import Flutter
import UIKit
import MultipeerConnectivity
import SwiftyJSON

let SERVICE_TYPE = "ioscreator-chat"
let INVOKE_CHANGE_STATE_METHOD = "invoke_change_state_method"
let INVOKE_MESSAGE_RECEIVE_METHOD = "invoke_message_receive_method"

enum MethodCall: String {
    case initNearbyService = "init_nearby_service"
    case startAdvertisingPeer = "start_advertising_peer"
    case startBrowsingForPeers = "start_browsing_for_peers"
    
    case stopAdvertisingPeer = "stop_advertising_peer"
    case stopBrowsingForPeers = "stop_browsing_for_peers"
    
    case invitePeer = "invite_peer"
    case disconnectPeer = "disconnect_peer"
    
    case sendMessage = "send_message"
}

public class SwiftFlutterNearbyConnectionsPlugin: NSObject, FlutterPlugin {
    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: "flutter_nearby_connections", binaryMessenger: registrar.messenger())
        let instance = SwiftFlutterNearbyConnectionsPlugin(channel: channel)
        registrar.addMethodCallDelegate(instance, channel: channel)
    }
    
    var currentReceivedDevice: Device? = Device(peerID: MPCManager.instance.localPeerID)
    
    let channel: FlutterMethodChannel
    
    struct DeviceJson {
        var deviceID:String
        var deviceName:String
        var state:Int
        
        func toStringAnyObject() -> [String: Any] {
            return [
                "deviceID": deviceID,
                "deviceName": deviceName,
                "state": state
            ]
        }
    }
    
    struct MessageJson {
        var deviceID:String
        var message:String
        
        func toStringAnyObject() -> [String: Any] {
            return [
                "deviceID": deviceID,
                "message": message
            ]
        }
    }
    
    @objc func stateChanged(){
        let devices = MPCManager.instance.devices.compactMap({return DeviceJson(deviceID: $0.deviceId, deviceName: $0.peerID.displayName, state: $0.state.rawValue)})
        channel.invokeMethod(INVOKE_CHANGE_STATE_METHOD, arguments: JSON(devices.compactMap({return $0.toStringAnyObject()})).rawString())
    }
    
    @objc func messageReceived(notification: Notification) {
        do {
            if let data = notification.userInfo?["data"] as? Data, let stringData = JSON(data).rawString() {
                self.channel.invokeMethod(INVOKE_MESSAGE_RECEIVE_METHOD,
                                          arguments: stringData)
            }
        } catch let e {
            print(e.localizedDescription)
        }
    }
    
    public init(channel:FlutterMethodChannel) {
        self.channel = channel
        super.init()
        
        NotificationCenter.default.addObserver(self, selector: #selector(stateChanged), name: MPCManager.Notifications.deviceDidChangeState, object: nil)
        
        NotificationCenter.default.addObserver(self, selector: #selector(messageReceived), name: Device.messageReceivedNotification, object: nil)
        
        MPCManager.instance.deviceDidChange = {[weak self] in
            self?.stateChanged()
        }
    }
    
    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        switch MethodCall(rawValue: call.method) {
        case .initNearbyService:
            let serviceType:String = call.arguments as? String ?? SERVICE_TYPE
            MPCManager.instance.setup(serviceType: serviceType)
        case .startAdvertisingPeer:
            MPCManager.instance.startAdvertisingPeer()
        case .startBrowsingForPeers:
            MPCManager.instance.startBrowsingForPeers()
        case .stopAdvertisingPeer:
            MPCManager.instance.stopAdvertisingPeer()
        case .stopBrowsingForPeers:
            MPCManager.instance.stopBrowsingForPeers()
        case .invitePeer:
            let deviceID:String? = call.arguments as? String ?? nil
            if(deviceID != nil){
                MPCManager.instance.invitePeer(deviceID: deviceID!)
            }
        case .disconnectPeer:
            let deviceID:String? = call.arguments as? String ?? nil
            if(deviceID != nil){
                MPCManager.instance.disconnectPeer(deviceID: deviceID!)
            }
        case .sendMessage:
            guard let jsonData = call.arguments as? String, let data = jsonData.data(using: String.Encoding.utf8) else {fatalError()}
            do {
                let json = JSON(data)
                if let device = MPCManager.instance.device(for: json["device_id"].stringValue) {
                    currentReceivedDevice = device
                    try device.send(data: data)
                }
            } catch let error as NSError {
                print(error)
            }
        default:
            return
        }
    }
    
}
