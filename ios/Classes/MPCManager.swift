//
//  MPCManager.swift
//  multipeer_connections
//
//  Created by NamIT on 9/3/20.
//

import Foundation
import MultipeerConnectivity

class MPCManager: NSObject {
    
    var advertiser: MCNearbyServiceAdvertiser!
    var browser: MCNearbyServiceBrowser!
    
    struct Notifications {
        static let deviceDidChangeState = Notification.Name("deviceDidChangeState")
    }
    
    static let instance = MPCManager()
    
    var localPeerID: MCPeerID!
    var enterbackgroundNotification: NSObjectProtocol!
    var devices: [Device] = []
    
    override init() {
        if let data = UserDefaults.standard.data(forKey: "peerID"), let id = NSKeyedUnarchiver.unarchiveObject(with: data) as? MCPeerID {
            self.localPeerID = id
        } else {
            let peerID = MCPeerID(displayName: UIDevice.current.name)
            let data = NSKeyedArchiver.archivedData(withRootObject: peerID)
            UserDefaults.standard.set(data, forKey: "peerID")
            self.localPeerID = peerID
        }
        super.init()
    }
    
    deinit{
        if(enterbackgroundNotification != nil){
            NotificationCenter.default.removeObserver(enterbackgroundNotification!)
        }
    }
    
    func setup(serviceType: String){
        self.advertiser = MCNearbyServiceAdvertiser(peer: localPeerID, discoveryInfo: nil, serviceType: serviceType)
        self.advertiser.delegate = self
        
        self.browser = MCNearbyServiceBrowser(peer: localPeerID, serviceType: serviceType)
        self.browser.delegate = self
    }
    
    func startAdvertisingPeer() {
        self.advertiser.startAdvertisingPeer()
        
        enterbackgroundNotification = NotificationCenter.default.addObserver(
            forName: UIApplication.didEnterBackgroundNotification,
            object: nil,
            queue: nil,
            using: {
                [weak self](notification) in
                self?.enteredBackground()
            }
        )
    }
    
    func startBrowsingForPeers() {
        self.browser.startBrowsingForPeers()
        
        enterbackgroundNotification = NotificationCenter.default.addObserver(
            forName: UIApplication.didEnterBackgroundNotification,
            object: nil,
            queue: nil,
            using: {
                [weak self](notification) in
                self?.enteredBackground()
            }
        )
    }
    
    func stopAdvertisingPeer() {
        self.advertiser.stopAdvertisingPeer()
    }
    
    func stopBrowsingForPeers() {
        self.browser.stopBrowsingForPeers()
    }
    
    func invitePeer(deviceID: String) {
        self.devices.forEach { (element) in
            element.disconnect()
        }
        
        
        let device = MPCManager.instance.device(for: deviceID)
        device?.invite(with: self.browser)
    }
    
    func uninvitePeer(deviceID: String){
        self.devices.forEach { (element) in
            if(element.deviceId == deviceID){
                element.disconnect()
            }
        }
    }
    
    func device(for deviceId: String) -> Device? {
        for device in self.devices {
            if device.deviceId == deviceId { return device }
        }
        
        return nil
    }
    
    func device(for id: MCPeerID) -> Device {
        if let device = devices.first(where: {$0.peerID == id}) {
            return device
        } else {
            let device = Device(peerID: id)
            self.devices.append(device)
            return device
        }
    }
    
    @objc func enteredBackground() {
        for device in self.devices {
            device.disconnect()
        }
    }
}

extension MPCManager: MCNearbyServiceAdvertiserDelegate {
    func advertiser(_ advertiser: MCNearbyServiceAdvertiser, didReceiveInvitationFromPeer peerID: MCPeerID, withContext context: Data?, invitationHandler: @escaping (Bool, MCSession?) -> Void) {
        
        let device = self.device(for: peerID)
        device.createSession()
        invitationHandler(true, device.session)
        //  Handle our incoming peer
    }
}

extension MPCManager: MCNearbyServiceBrowserDelegate {
    func browser(_ browser: MCNearbyServiceBrowser, foundPeer peerID: MCPeerID, withDiscoveryInfo info: [String : String]?) {
        self.device(for: peerID)
    }
    
    func browser(_ browser: MCNearbyServiceBrowser, lostPeer peerID: MCPeerID) {
        let device = self.device(for: peerID)
        device.disconnect()
    }
}
