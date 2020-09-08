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
      return MaterialPageRoute(builder: (_) => SecondRoute());
    case 'cds':
      return MaterialPageRoute(builder: (_) => DevicesListScreen());
    default:
      return MaterialPageRoute(
          builder: (_) => Scaffold(
            body: Center(
                child: Text('No route defined for ${settings.name}')),
          ));
  }
}
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
              onTap: () {Navigator.pushNamed(context, 'pos');},
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
              onTap: () {Navigator.pushNamed(context, 'cds');},
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
  final nearbyService = NearbyService(serviceType: 'connection');

  @override
  void initState() {
    super.initState();
    nearbyService.startAdvertisingPeer();
    nearbyService.startBrowsingForPeers();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      body: Container(),
    );
  }

  Widget _item(Device device) {
    return Container(
      color: Colors.grey,
    );
  }
}

class SecondRoute extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(
        child: Text('Go back!'),
      ),
    );
  }
}