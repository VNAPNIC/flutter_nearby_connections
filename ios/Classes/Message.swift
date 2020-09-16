//
//  Message.swift
//  multipeer_connections
//
//  Created by NamIT on 9/3/20.
//

import Foundation
import SwiftyJSON

struct Message: Codable {
    let body: String
}

extension Device {
    func send(text: String) throws {
        try self.session?.send(text.data(using: .utf8) ?? Data(), toPeers: [self.peerID], with: .reliable)
    }
    
    func send(json: JSON) throws {
        try self.session?.send(json.rawData(), toPeers: [self.peerID], with: .reliable)
    }
    
    func send(data: Data) throws {
        try self.session?.send(data, toPeers: [self.peerID], with: .reliable)
    }
}

struct ReceivedResponse {
    var deviceID: String?
    var message: MessageReponse
    
    init(json: JSON) {
        deviceID = json["deviceID"].string
        message = MessageReponse(json: json["message"])
    }
}

struct MessageReponse {
    var items: [MessageResponseItem] = []
    var subTotal: NSNumber?
    var rounding: NSNumber?
    var total: NSNumber?
    
    init(json: JSON) {
        items = json["items"].array?.compactMap({return MessageResponseItem(json: $0)}) ?? []
        subTotal = json["subTotal"].number
        rounding = json["rounding"].number
        total = json["total"].number
    }
}

struct MessageResponseItem {
    var quantity: NSNumber?
    var itemCollection: MessageItemCollection?
    
    init(json: JSON) {
        quantity = json["quantity"].number
        itemCollection = MessageItemCollection(json: json["itemCollection"])
    }
}

struct MessageItemCollection {
    var name: String?
    var status: MessageItemCollectionStatus?
    var imageUrl: String?
    var price: NSNumber?
    
    init(json: JSON) {
        name = json["name"].string
        status = MessageItemCollectionStatus(rawValue: json["status"].stringValue)
        imageUrl = json["imageUrl"].string
        price = json["price"].number
    }
}

enum MessageItemCollectionStatus: String {
    case active = "ACTIVE"
}

