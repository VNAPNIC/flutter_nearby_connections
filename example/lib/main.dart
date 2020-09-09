import 'dart:async';

import 'package:flutter/material.dart';
import 'package:nearby_connections/nearby_connections.dart';

void main() {
  runApp(MyApp());
}

Route<dynamic> generateRoute(RouteSettings settings) {
  switch (settings.name) {
    case '/':
      return MaterialPageRoute(builder: (_) => Home());
    case 'pos':
      return MaterialPageRoute(
          builder: (_) => DevicesListScreen(type: deviceType));
    case 'cds':
      return MaterialPageRoute(
          builder: (_) => DevicesListScreen(type: deviceType));
    default:
      return MaterialPageRoute(
          builder: (_) => Scaffold(
                body: Center(
                    child: Text('No route defined for ${settings.name}')),
              ));
  }
}

String deviceType;

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      onGenerateRoute: generateRoute,
      initialRoute: '/',
    );
  }
}

class Home extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Column(
        children: [
          Expanded(
            child: InkWell(
              onTap: () {
                Navigator.pushNamed(context, 'pos');
                deviceType = "pos";
              },
              child: Container(
                color: Colors.red,
                child: Center(
                    child: Text(
                  'POS',
                  style: TextStyle(color: Colors.white, fontSize: 40),
                )),
              ),
            ),
          ),
          Expanded(
            child: InkWell(
              onTap: () {
                Navigator.pushNamed(context, 'cds');
                deviceType = "cds";
              },
              child: Container(
                color: Colors.green,
                child: Center(
                    child: Text(
                  'CDS',
                  style: TextStyle(color: Colors.white, fontSize: 40),
                )),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class DevicesListScreen extends StatefulWidget {
  const DevicesListScreen({this.type});

  final String type;

  @override
  _DevicesListScreenState createState() => _DevicesListScreenState();
}

class _DevicesListScreenState extends State<DevicesListScreen> {
  List<Device> devices = [];
  final nearbyService = NearbyService(serviceType: 'mp-connection');
  StreamSubscription subscription;

  @override
  void initState() {
    super.initState();
    subscription = nearbyService.stateChangedSubscription(callback: (device) {
      setState(() {
        devices.clear();
        devices.addAll(device);
      });
    });
  }

  @override
  void dispose() {
    subscription?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.type.toUpperCase()),
        actions: [
          IconButton(
            icon: Icon(Icons.cast_connected),
            onPressed: () {
              if (deviceType == 'pos') {
                nearbyService.startBrowsingForPeers();
              } else {
                nearbyService.startAdvertisingPeer();
                nearbyService.startBrowsingForPeers();
              }
            },
          )
        ],
      ),
      backgroundColor: Colors.white,
      body: ListView.builder(
          itemCount: devices.length,
          itemBuilder: (context, index) {
            final device = devices[index];
            return ListTile(
              title: Text(device.displayName),
              subtitle: Text('State: $device.state'),
              onTap: _onTabItemListener(device),
            );
          }),
    );
  }

  _onTabItemListener(Device device) {
    switch (device.state) {
      case SessionState.notConnected:
        showDialog(
            context: context,
            builder: (BuildContext context) {
              return AlertDialog(
                title: Text("Connect to this device?"),
                actions: [
                  FlatButton(
                    child: Text("Connect"),
                    onPressed: () {
                      nearbyService.inviteDevice(deviceID: device.deviceID);
                    },
                  ),
                  FlatButton(
                    child: Text("Cancel"),
                    onPressed: () {
                      Navigator.of(context).maybePop();
                    },
                  )
                ],
              );
            });
        break;

      case SessionState.connected:
        showDialog(
            context: context,
            builder: (BuildContext context) {
              return AlertDialog(
                title: Text("Send message"),
                actions: [
                  FlatButton(
                    child: Text("Send"),
                    onPressed: () {
                      nearbyService.inviteDevice(deviceID: device.deviceID);
                    },
                  ),
                  FlatButton(
                    child: Text("Cancel"),
                    onPressed: () {
                      Navigator.of(context).pop();
                    },
                  )
                ],
              );
            });
        break;
    }
  }
}
