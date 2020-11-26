import Foundation
import MultipeerConnectivity
import SwiftyJSON

class Device: NSObject {
    let peerID: MCPeerID
    var session: MCSession?
    var state = MCSessionState.notConnected
    var lastMessageReceived: Message?
    
    static let messageReceivedNotification = Notification.Name("DeviceDidReceiveMessage")
    
    init(peerID: MCPeerID) {
        self.peerID = peerID
        super.init()
    }
    
    func createSession() {
        if self.session != nil { return }
        self.session = MCSession(peer: MPCManager.instance.localPeerID, securityIdentity: nil, encryptionPreference: .required)
        self.session?.delegate = self
    }
    
    func disconnect() {
        self.session?.disconnect()
        self.session = nil
        NotificationCenter.default.post(name: MPCManager.Notifications.deviceDidChangeState, object: self)
    }
    
    func invite(with browser: MCNearbyServiceBrowser) {
        if (self.state == MCSessionState.notConnected) {
            self.createSession()
            if let session = session {
                browser.invitePeer(self.peerID, to: session, withContext: nil, timeout: 10)
            }
        }
    }
}

extension Device: MCSessionDelegate {
    public func session(_ session: MCSession, peer peerID: MCPeerID, didChange state: MCSessionState) {
        self.state = state
        NotificationCenter.default.post(name: MPCManager.Notifications.deviceDidChangeState, object: nil)
    }
    
    public func session(_ session: MCSession, didReceive data: Data, fromPeer peerID: MCPeerID) {
        NotificationCenter.default.post(name: Device.messageReceivedNotification, object: nil, userInfo: ["from": peerID, "data": data])
    }
    
    public func session(_ session: MCSession, didReceive stream: InputStream, withName streamName: String, fromPeer peerID: MCPeerID) { }
    
    public func session(_ session: MCSession, didStartReceivingResourceWithName resourceName: String, fromPeer peerID: MCPeerID, with progress: Progress) { }
    
    public func session(_ session: MCSession, didFinishReceivingResourceWithName resourceName: String, fromPeer peerID: MCPeerID, at localURL: URL?, withError error: Error?) { }
    
}
