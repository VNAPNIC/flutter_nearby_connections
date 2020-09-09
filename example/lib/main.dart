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
            return InkWell(
                onTap: () {
                  _onTabItemListener(device);
                },
                child: Container(
                  margin: EdgeInsets.all(8.0),
                  child: Column(
                    children: [
                      Row(
                        children: [
                          Expanded(child: Text(device.displayName)),
                          Container(
                            margin: EdgeInsets.symmetric(horizontal: 8.0),
                            padding: EdgeInsets.all(8.0),
                            height: 35,
                            width: 100,
                            color: getStateColor(device.state),
                            child: Center(
                              child: Text(
                                getStateName(device.state),
                                style:
                                TextStyle(color: Colors.white,fontWeight: FontWeight.bold),
                              ),
                            ),
                          )
                        ],
                      ),
                      SizedBox(height: 8.0,),
                      Divider(height: 1,color: Colors.grey,)
                    ],
                  ),
                ));
          }),
    );
  }

  String getStateName(SessionState state) {
    switch (state) {
      case SessionState.notConnected:
        return "invite";
      case SessionState.connecting:
        return "inviting";
      case SessionState.connected:
        return "invited";
    }
  }

  Color getStateColor(SessionState state) {
    switch (state) {
      case SessionState.notConnected:
        return Colors.green;
      case SessionState.connecting:
        return Colors.grey;
      case SessionState.connected:
        return Colors.indigoAccent;
    }
  }

  _onTabItemListener(Device device) {
    switch (device.state) {
      case SessionState.notConnected:
        showDialog(
            context: context,
            builder: (BuildContext context) {
              return AlertDialog(
                title: Text("Device not connected!"),
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
      case SessionState.connecting:
        showDialog(
            context: context,
            builder: (BuildContext context) {
              return AlertDialog(
                title: Text("Device not connected!"),
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
    }
  }
}
