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
    private var backgroundTaskID: UIBackgroundTaskIdentifier = .invalid
    var devices: [Device] = [] {
        didSet {
            deviceDidChange?()
        }
    }
    
    var deviceDidChange: (() -> Void)?
    
    //    override init() {
    //        if let data = UserDefaults.standard.data(forKey: "peerID"), let id = NSKeyedUnarchiver.unarchiveObject(with: data) as? MCPeerID {
    //            self.localPeerID = id
    //        } else {
    //            let peerID = MCPeerID(displayName: UIDevice.current.name)
    //            let data = NSKeyedArchiver.archivedData(withRootObject: peerID)
    //            UserDefaults.standard.set(data, forKey: "peerID")
    //            self.localPeerID = peerID
    //        }
    //        super.init()
    //    }
    
    deinit{
        if let taskEnterBackground = enterbackgroundNotification {
            NotificationCenter.default.removeObserver(taskEnterBackground)
        }
    }
    
    func setup(serviceType: String, deviceName: String) {
        if let data = UserDefaults.standard.data(forKey: deviceName), let id = NSKeyedUnarchiver.unarchiveObject(with: data) as? MCPeerID {
            self.localPeerID = id
        } else {
            let peerID = MCPeerID(displayName: deviceName)
            let data = NSKeyedArchiver.archivedData(withRootObject: peerID)
            UserDefaults.standard.set(data, forKey: deviceName)
            self.localPeerID = peerID
        }
        
        self.advertiser = MCNearbyServiceAdvertiser(peer: localPeerID, discoveryInfo: nil, serviceType: serviceType)
        self.advertiser.delegate = self
        
        self.browser = MCNearbyServiceBrowser(peer: localPeerID, serviceType: serviceType)
        self.browser.delegate = self
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
    
    func startAdvertisingPeer() {
        self.advertiser.startAdvertisingPeer()
        
    }
    
    func startBrowsingForPeers() {
        self.browser.startBrowsingForPeers()
    }
    
    func stopAdvertisingPeer() {
        self.advertiser.stopAdvertisingPeer()
    }
    
    func stopBrowsingForPeers() {
        for device in self.devices {
            device.disconnect()
        }
        self.browser.stopBrowsingForPeers()
    }
    
    func invitePeer(deviceID: String) {
        do {
            let device = MPCManager.instance.findDevice(for: deviceID)
            if(device?.state == MCSessionState.notConnected){
                device?.invite(with: self.browser)
            }
        } catch let error {
            print(error.localizedDescription)
        }
    }
    
    func disconnectPeer(deviceID: String){
        let device = MPCManager.instance.findDevice(for: deviceID)
        device?.disconnect()
    }
    
    func addNewDevice(for id: MCPeerID) -> Device {
        devices = devices.filter{$0.peerID.displayName != id.displayName}
        let device = Device(peerID: id)
        self.devices.append(device)
        return device
    }
    
    func findDevice(for deviceId: String) -> Device? {
        for device in self.devices {
            if device.peerID.displayName == deviceId { return device }
        }
        return nil
    }
    
    func findDevice(for id: MCPeerID) -> Device? {
        if let device = devices.first(where: {$0.peerID == id}) {
            return device
        }
        return nil
    }
    
    @objc func enteredBackground() {
        for device in self.devices {
            device.disconnect()
        }
        DispatchQueue.global().async {[weak self] in
            guard let `self` = self else {return}
              // Request the task assertion and save the ID.
              self.backgroundTaskID = UIApplication.shared.beginBackgroundTask (withName: "Finish Network Tasks") {
                 // End the task if time expires.
                 UIApplication.shared.endBackgroundTask(self.backgroundTaskID)
                self.backgroundTaskID = .invalid
              }
                    
              // Send the data synchronously.
            self.devices = []
                    
              // End the task assertion.
              UIApplication.shared.endBackgroundTask(self.backgroundTaskID)
            self.backgroundTaskID = .invalid
           }
    }
}

extension MPCManager: MCNearbyServiceAdvertiserDelegate {
    func advertiser(_ advertiser: MCNearbyServiceAdvertiser, didReceiveInvitationFromPeer peerID: MCPeerID, withContext context: Data?, invitationHandler: @escaping (Bool, MCSession?) -> Void) {
        let device = self.addNewDevice(for: peerID)
        device.createSession()
        invitationHandler(true, device.session)
        //  Handle our incoming peer
    }
}

extension MPCManager: MCNearbyServiceBrowserDelegate {
    func browser(_ browser: MCNearbyServiceBrowser, foundPeer peerID: MCPeerID, withDiscoveryInfo info: [String : String]?) {
        // found peer, create a device with this peerID
        addNewDevice(for: peerID)
    }
    
    func browser(_ browser: MCNearbyServiceBrowser, lostPeer peerID: MCPeerID) {
        // lost peer, disconnect and remove the device with this peerID
        let device = self.findDevice(for: peerID)
        devices = devices.filter{$0.peerID.displayName != peerID.displayName}
        device?.disconnect()
    }
    
    
}
