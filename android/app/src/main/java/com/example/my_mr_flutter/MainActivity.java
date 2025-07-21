package com.example.my_mr_flutter;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;

public class MainActivity extends FlutterActivity {
    public static FlutterEngine flutterEngine;
    private static final String CHANNEL = "com.example.my_mr_flutter/virtual_display";
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1;

    @Override
    public void configureFlutterEngine(FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);
        MainActivity.flutterEngine = flutterEngine;
        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
                .setMethodCallHandler(
                        (call, result) -> {
                            if (call.method.equals("startVirtualDisplay")) {
                                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                                        != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(
                                            this,
                                            new String[]{Manifest.permission.CAMERA},
                                            CAMERA_PERMISSION_REQUEST_CODE
                                    );
                                } else {
                                    startVirtualDisplayService();
                                }
                                result.success(null);
                            } else {
                                result.notImplemented();
                            }
                        }
                );
    }

    private void startVirtualDisplayService() {
        Intent intent = new Intent(this, VirtualDisplayService.class);
        startService(intent);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults
    ) {
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startVirtualDisplayService();
            }
        }
    }
}
