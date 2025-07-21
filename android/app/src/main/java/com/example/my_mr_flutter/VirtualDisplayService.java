package com.example.my_mr_flutter;

import android.app.Service;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.os.IBinder;
import android.view.Surface;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.view.TextureRegistry;

public class VirtualDisplayService extends Service {
    private VirtualDisplay virtualDisplay;
    private MethodChannel methodChannel;
    private TextureRegistry.SurfaceTextureEntry textureEntry;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        methodChannel = new MethodChannel(MainActivity.flutterEngine.getDartExecutor().getBinaryMessenger(), "com.example.my_mr_flutter/virtual_display");
        methodChannel.setMethodCallHandler(
                (call, result) -> {
                    if (call.method.equals("createVirtualDisplay")) {
                        int textureId = call.argument("textureId");
                        int width = call.argument("width");
                        int height = call.argument("height");
                        createVirtualDisplay(textureId, width, height);
                        result.success(textureEntry.id());
                    } else if (call.method.equals("disposeVirtualDisplay")) {
                        disposeVirtualDisplay();
                        result.success(null);
                    } else {
                        result.notImplemented();
                    }
                }
        );
    }

    private void createVirtualDisplay(int textureId, int width, int height) {
        textureEntry = MainActivity.flutterEngine.getRenderer().createSurfaceTexture();
        Surface surface = new Surface(textureEntry.surfaceTexture());
        DisplayManager displayManager = (DisplayManager) getSystemService(DISPLAY_SERVICE);
        virtualDisplay = displayManager.createVirtualDisplay(
                "MyVirtualDisplay",
                width, height, 160,
                surface,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR
        );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (virtualDisplay != null) {
            virtualDisplay.release();
        }
        if (textureEntry != null) {
            textureEntry.release();
        }
    }
}
