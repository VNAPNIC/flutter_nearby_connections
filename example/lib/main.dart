import 'dart:async';

import 'package:flutter/material.dart';
import 'package:nearby_connections/nearby_connections.dart';
import 'package:fluttertoast/fluttertoast.dart';

void main() {
  runApp(MyApp());
}

Route<dynamic> generateRoute(RouteSettings settings) {
  switch (settings.name) {
    case '/':
      return MaterialPageRoute(builder: (_) => Home());
    case 'browser':
      return MaterialPageRoute(
          builder: (_) => DevicesListScreen(deviceType: DeviceType.browser));
    case 'advertiser':
      return MaterialPageRoute(
          builder: (_) => DevicesListScreen(deviceType: DeviceType.advertiser));
    default:
      return MaterialPageRoute(
          builder: (_) => Scaffold(
                body: Center(
                    child: Text('No route defined for ${settings.name}')),
              ));
  }
}

DeviceType deviceType;

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
                Navigator.pushNamed(context, 'browser');
                deviceType = DeviceType.browser;
              },
              child: Container(
                color: Colors.red,
                child: Center(
                    child: Text(
                  'BROWSER',
                  style: TextStyle(color: Colors.white, fontSize: 40),
                )),
              ),
            ),
          ),
          Expanded(
            child: InkWell(
              onTap: () {
                Navigator.pushNamed(context, 'advertiser');
                deviceType = DeviceType.advertiser;
              },
              child: Container(
                color: Colors.green,
                child: Center(
                    child: Text(
                  'ADVERTISER',
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

enum DeviceType {
  advertiser,
  browser
}

class DevicesListScreen extends StatefulWidget {
  const DevicesListScreen({this.deviceType});

  final DeviceType deviceType;

  @override
  _DevicesListScreenState createState() => _DevicesListScreenState();
}

class _DevicesListScreenState extends State<DevicesListScreen> {
  List<Device> devices = [];
  List<Device> connectedDevices = [];
  final nearbyService = NearbyService(serviceType: 'mp-connection');
  StreamSubscription subscription;
  StreamSubscription receivedDataSubscription;

  @override
  void initState() {
    super.initState();

    subscription = nearbyService.stateChangedSubscription(callback: (devicesList) {
      setState(() {
        devices.clear();
        devices.addAll(devicesList);
        connectedDevices.clear();
        connectedDevices.addAll(devicesList.where((d) => d.state == SessionState.connected).toList());
      });
    });

    receivedDataSubscription = nearbyService.dataReceivedSubscription(callback: (data) {
      Fluttertoast.showToast(msg: "Device ID: ${data.deviceID} , message: ${data.message}");
    });

    WidgetsBinding.instance.addPostFrameCallback((timeStamp) {
      if (deviceType == 'browser') {
        nearbyService.startBrowsingForPeers();
      } else {
        nearbyService.startAdvertisingPeer();
        nearbyService.startBrowsingForPeers();
      }
    });
  }

  @override
  void dispose() {
    subscription?.cancel();
    receivedDataSubscription?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.deviceType.toString().substring(11).toUpperCase()),
      ),
      backgroundColor: Colors.white,
      body: ListView.builder(
          itemCount: getItemCount(),
          itemBuilder: (context, index) {
            final device = deviceType == DeviceType.advertiser? connectedDevices[index] : devices[index];
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
        return "connected";
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
                title: Text("Connect to device?"),
                actions: [
                  FlatButton(
                    child: Text("Cancel"),
                    onPressed: () {
                      Navigator.of(context).maybePop();
                    },
                  ),
                  FlatButton(
                    child: Text("Connect"),
                    onPressed: () {
                      nearbyService.inviteDevice(deviceID: device.deviceID);
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
              final myController = TextEditingController();
              return AlertDialog(
                title: Text("Send message"),
                content: TextField(controller: myController),
                actions: [
                  FlatButton(
                    child: Text("Cancel"),
                    onPressed: () {
                      Navigator.of(context).pop();
                    },
                  ),
                  FlatButton(
                    child: Text("Send"),
                    onPressed: () {
                      nearbyService.sendMessage(device.deviceID, myController.text);
                      myController.text = '';
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

  int getItemCount()  {
    if(deviceType == DeviceType.advertiser) {
      return connectedDevices.length;
    } else {
      return devices.length;
    }
  }
}
