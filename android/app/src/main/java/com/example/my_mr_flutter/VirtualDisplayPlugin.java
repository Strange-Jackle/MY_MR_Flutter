package com.example.my_mr_flutter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.view.Surface;
import androidx.annotation.NonNull;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.view.TextureRegistry;

public class VirtualDisplayPlugin implements FlutterPlugin, ActivityAware, MethodChannel.MethodCallHandler {
    private static final String CHANNEL = "com.example.my_mr_flutter/virtual_display";
    private MethodChannel methodChannel;
    private Context context;
    private Activity activity;
    private VirtualDisplay virtualDisplay;
    private TextureRegistry.SurfaceTextureEntry textureEntry;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        context = binding.getApplicationContext();
        methodChannel = new MethodChannel(binding.getBinaryMessenger(), CHANNEL);
        methodChannel.setMethodCallHandler(this);
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        methodChannel.setMethodCallHandler(null);
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        activity = null;
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
    }

    @Override
    public void onDetachedFromActivity() {
        activity = null;
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
        if (call.method.equals("createVirtualDisplay")) {
            int width = call.argument("width");
            int height = call.argument("height");
            createVirtualDisplay(width, height);
            result.success(textureEntry.id());
        } else if (call.method.equals("disposeVirtualDisplay")) {
            disposeVirtualDisplay();
            result.success(null);
        } else {
            result.notImplemented();
        }
    }

    private void createVirtualDisplay(int width, int height) {
        textureEntry = ((io.flutter.embedding.engine.FlutterEngine) context).getRenderer().createSurfaceTexture();
        Surface surface = new Surface(textureEntry.surfaceTexture());
        DisplayManager displayManager = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
        virtualDisplay = displayManager.createVirtualDisplay(
                "MyVirtualDisplay",
                width, height, 160,
                surface,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR
        );
    }

    private void disposeVirtualDisplay() {
        if (virtualDisplay != null) {
            virtualDisplay.release();
        }
        if (textureEntry != null) {
            textureEntry.release();
        }
    }
}
