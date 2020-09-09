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
    case uninvitedPeer = "uninvited_peer"
    
    case sendMessage = "send_message"
}

public class SwiftNearbyConnectionsPlugin: NSObject, FlutterPlugin {
    
    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: "nearby_connections", binaryMessenger: registrar.messenger())
        let instance = SwiftNearbyConnectionsPlugin(channel: channel)
        registrar.addMethodCallDelegate(instance, channel: channel)
    }
    
    let channel: FlutterMethodChannel
    
    struct DeviceJson {
        var deviceID:String
        var displayName:String
        var state:Int
        
        func toStringAnyObject() -> [String: Any] {
            return [
                "deviceID": deviceID,
                "displayName": displayName,
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
        let devices = MPCManager.instance.devices.compactMap({return DeviceJson(deviceID: $0.deviceId, displayName: $0.peerID.displayName, state: $0.state.rawValue)})
        channel.invokeMethod(INVOKE_CHANGE_STATE_METHOD, arguments: JSON(devices.compactMap({return $0.toStringAnyObject()})).rawString())
    }
    
    @objc func messageReceived() {
        let devices = MPCManager.instance.devices.compactMap({return MessageJson(deviceID: $0.deviceId, message: $0.lastMessageReceived?.body ?? "")})
        channel.invokeMethod(INVOKE_MESSAGE_RECEIVE_METHOD, arguments: JSON(devices.compactMap({return $0.toStringAnyObject()})).rawString())
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
        case .uninvitedPeer:
            let deviceID:String? = call.arguments as? String ?? nil
            if(deviceID != nil){
                MPCManager.instance.invitePeer(deviceID: deviceID!)
            }
        case .sendMessage:
            let jsonData: String? = call.arguments as? String ?? nil
            if(jsonData != nil){
                var dictonary:NSDictionary?
                if let data = jsonData!.data(using: String.Encoding.utf8) {
                    do {
                        dictonary = try JSONSerialization.jsonObject(with: data, options: []) as? [String:AnyObject] as NSDictionary?
                        if dictonary != nil{
                            try MPCManager.instance.device(for: dictonary?["deviceID"] as! String)?.send(text: dictonary?["message"] as! String)
                        }
                    } catch let error as NSError {
                        print(error)
                    }
                }
            }
        default:
            break
        }
    }
}
