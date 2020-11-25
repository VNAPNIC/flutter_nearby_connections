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
    
    var currentReceivedDevice: Device?
    
    let channel: FlutterMethodChannel
    
    struct DeviceJson {
        var deviceId:String
        var deviceName:String
        var state:Int
        
        func toStringAnyObject() -> [String: Any] {
            return [
                "deviceId": deviceId,
                "deviceName": deviceName,
                "state": state
            ]
        }
    }
    
    struct MessageJson {
        var deviceId:String
        var message:String
        
        func toStringAnyObject() -> [String: Any] {
            return [
                "deviceId": deviceId,
                "message": message
            ]
        }
    }
    
    @objc func stateChanged(){
        let devices = MPCManager.instance.devices.compactMap({return DeviceJson(deviceId: $0.peerID.displayName, deviceName: $0.peerID.displayName, state: $0.state.rawValue)})
        channel.invokeMethod(INVOKE_CHANGE_STATE_METHOD, arguments: JSON(devices.compactMap({return $0.toStringAnyObject()})).rawString())
    }
    
    @objc func messageReceived(notification: Notification) {
        do {
            if let data = notification.userInfo?["data"] as? Data, let stringData = JSON(data).rawString() {
                let dict = convertToDictionary(text: stringData)
                self.channel.invokeMethod(INVOKE_MESSAGE_RECEIVE_METHOD, arguments: dict)
            }
        } catch let e {
            print(e.localizedDescription)
        }
    }
    
    func convertToDictionary(text: String) -> [String: Any]? {
        if let data = text.data(using: .utf8) {
            do {
                return try JSONSerialization.jsonObject(with: data, options: []) as? [String: Any]
            } catch {
                print(error.localizedDescription)
            }
        }
        return nil
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
            guard let data = call.arguments as? Dictionary<String, AnyObject> else {
                result(false)
                return
                
            }
            let serviceType:String = data["serviceType"] as? String ?? SERVICE_TYPE
            var deviceName:String = data["deviceName"] as? String ?? ""
            if (deviceName.isEmpty){
                deviceName =  UIDevice.current.name
            }
               
            MPCManager.instance.setup(serviceType: serviceType, deviceName: deviceName)
            currentReceivedDevice = Device(peerID: MPCManager.instance.localPeerID)
            result(true)
        case .startAdvertisingPeer:
            MPCManager.instance.startAdvertisingPeer()
            result(true)
        case .startBrowsingForPeers:
            MPCManager.instance.startBrowsingForPeers()
            result(true)
        case .stopAdvertisingPeer:
            MPCManager.instance.stopAdvertisingPeer()
            result(true)
        case .stopBrowsingForPeers:
            MPCManager.instance.stopBrowsingForPeers()
            result(true)
        case .invitePeer:
            guard let data = call.arguments as? Dictionary<String, AnyObject> else {
                result(false)
                return
                
            }
            guard let deviceId: String = data["deviceId"] as? String else {
                result(false)
                return
            }
            MPCManager.instance.invitePeer(deviceID: deviceId)
            result(true)

        case .disconnectPeer:
            guard let data = call.arguments as? Dictionary<String, AnyObject> else {
                result(false)
                return
                
            }
            let deviceId:String? = data["deviceId"] as? String ?? nil
            if (deviceId != nil) {
                MPCManager.instance.disconnectPeer(deviceID: deviceId!)
                result(true)
            } else {
                result(false)
            }
        case .sendMessage:
            guard let dict = call.arguments as? Dictionary<String, AnyObject> else {
                result(false)
                return
            }
            do {
                let jsonData = try JSONSerialization.data(withJSONObject: dict, options: .prettyPrinted)
                if let device = MPCManager.instance.findDevice(for: dict["deviceId"] as! String) {
                    currentReceivedDevice = device
                    try device.send(data: jsonData)
                    result(true)
                    return
                }
            } catch let error as NSError {
                print(error)
            }
            result(false)
        default:
            result(FlutterMethodNotImplemented)
            return
        }
    }
    
}
