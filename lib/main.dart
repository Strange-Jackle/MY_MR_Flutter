import 'dart:async';

import 'package:camera/camera.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  final cameras = await availableCameras();
  final firstCamera = cameras.first;

  runApp(
    MaterialApp(
      theme: ThemeData.dark(),
      home: MyApp(
        camera: firstCamera,
      ),
    ),
  );
}

class MyApp extends StatefulWidget {
  final CameraDescription camera;

  const MyApp({
    Key? key,
    required this.camera,
  }) : super(key: key);

  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  late CameraController _controller;
  late Future<void> _initializeControllerFuture;
  static const platform = MethodChannel('com.example.my_mr_flutter/virtual_display');
  int? _textureId;
  double _x = 0.0;
  double _y = 0.0;
  double _width = 200.0;
  double _height = 300.0;

  @override
  void initState() {
    super.initState();
    _controller = CameraController(
      widget.camera,
      ResolutionPreset.medium,
    );
    _initializeControllerFuture = _controller.initialize();
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  Future<void> _createVirtualDisplay() async {
    try {
      final int textureId = await platform.invokeMethod('createVirtualDisplay', {'width': _width.toInt(), 'height': _height.toInt()});
      setState(() {
        _textureId = textureId;
      });
    } on PlatformException catch (e) {
      print("Failed to create virtual display: '${e.message}'.");
    }
  }

  Future<void> _disposeVirtualDisplay() async {
    try {
      await platform.invokeMethod('disposeVirtualDisplay');
      setState(() {
        _textureId = null;
      });
    } on PlatformException catch (e) {
      print("Failed to dispose virtual display: '${e.message}'.");
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: FutureBuilder<void>(
        future: _initializeControllerFuture,
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.done) {
            return Stack(
              children: [
                CameraPreview(_controller),
                if (_textureId != null)
                  Positioned(
                    left: _x,
                    top: _y,
                    child: GestureDetector(
                      onPanUpdate: (details) {
                        setState(() {
                          _x += details.delta.dx;
                          _y += details.delta.dy;
                        });
                      },
                      onScaleUpdate: (details) {
                        setState(() {
                          _width *= details.scale;
                          _height *= details.scale;
                        });
                      },
                      child: SizedBox(
                        width: _width,
                        height: _height,
                        child: Texture(textureId: _textureId!),
                      ),
                    ),
                  ),
                if (_textureId != null)
                  Positioned(
                    left: _x + _width - 20,
                    top: _y - 20,
                    child: IconButton(
                      icon: Icon(Icons.close),
                      onPressed: _disposeVirtualDisplay,
                    ),
                  ),
                if (_textureId == null)
                  Center(
                    child: ElevatedButton(
                      onPressed: _createVirtualDisplay,
                      child: Text('Create Virtual Display'),
                    ),
                  ),
              ],
            );
          } else {
            return Center(child: CircularProgressIndicator());
          }
        },
      ),
    );
  }
}
