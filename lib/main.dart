import 'dart:async';
import 'dart:math';
import 'package:flutter/material.dart';
import 'package:camera/camera.dart';
import 'package:sensors_plus/sensors_plus.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:android_intent_plus/android_intent.dart';
import 'package:flutter/services.dart';


late List<CameraDescription> cameras;

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  cameras = await availableCameras();
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Mixed Reality Camera',
      theme: ThemeData.dark(),
      home: VRViewPage(),
      debugShowCheckedModeBanner: false,
    );
  }
}

class VRViewPage extends StatefulWidget {
  @override
  _VRViewPageState createState() => _VRViewPageState();
}

class _VRViewPageState extends State<VRViewPage> {
  late CameraController _controller;
  double _rotateX = 0;
  double _rotateY = 0;

  @override
  void initState() {
    super.initState();
    SystemChrome.setEnabledSystemUIMode(SystemUiMode.immersiveSticky);
    initCamera();
    listenGyroscope();
  }


  Future<void> initCamera() async {
    await Permission.camera.request();
    _controller = CameraController(
      cameras.firstWhere((camera) =>
      camera.lensDirection == CameraLensDirection.back),
      ResolutionPreset.medium,
      enableAudio: false,
    );
    await _controller.initialize();
    if (mounted) setState(() {});
  }

  void listenGyroscope() {
    gyroscopeEvents.listen((GyroscopeEvent event) {
      setState(() {
        _rotateX += event.y * 0.01;
        _rotateY += event.x * 0.01;
      });
    });
  }

  void openOtherApp() {
    final intent = AndroidIntent(
      action: 'android.intent.action.MAIN',
      category: 'android.intent.category.LAUNCHER',
      package: 'com.android.settings', // Change this to the app you want
      flags: <int>[268435456], // FLAG_ACTIVITY_NEW_TASK only
    );
    intent.launch();
  }


  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  Widget buildVRPreview(BuildContext context) {
    if (!_controller.value.isInitialized)
      return Center(child: CircularProgressIndicator());

    final screenSize = MediaQuery
        .of(context)
        .size;

    return SizedBox(
      width: screenSize.width,
      height: screenSize.height,
      child: Row(
        children: [
          Expanded(child: Transform(
            transform: Matrix4.identity()
              ..rotateX(_rotateX)
              ..rotateY(_rotateY),
            alignment: Alignment.center,
            child: CameraPreview(_controller),
          )),
          Expanded(child: Transform(
            transform: Matrix4.identity()
              ..rotateX(_rotateX)
              ..rotateY(_rotateY),
            alignment: Alignment.center,
            child: CameraPreview(_controller),
          )),
        ],
      ),
    );
  }


  @override
  Widget build(BuildContext context) {
    return Material(
      child: Stack(
        children: [
          buildVRPreview(context),
          Positioned(
            bottom: 30,
            right: 30,
            child: GestureDetector(
              onTap: openOtherApp,
              child: Container(
                padding: EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: Colors.deepPurple,
                  shape: BoxShape.circle,
                ),
                child: Icon(Icons.open_in_new, color: Colors.white),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
