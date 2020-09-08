//
//  Message.swift
//  multipeer_connections
//
//  Created by NamIT on 9/3/20.
//

import Foundation

struct Message: Codable {
    let body: String
}

extension Device {
    func send(text: String) throws {
        let message = Message(body: text)
        let payload = try JSONEncoder().encode(message)
        try self.session?.send(payload, toPeers: [self.peerID], with: .reliable)
    }
}
