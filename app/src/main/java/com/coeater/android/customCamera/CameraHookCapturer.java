package com.coeater.android.customCamera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.camera2.CameraManager;
import android.media.MediaRecorder;

import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.Logging;
import org.webrtc.SurfaceTextureHelper;

import javax.annotation.Nullable;

public class CameraHookCapturer extends CameraCapturer {
    private final Context context;
    @Nullable
    private final CameraManager cameraManager;
    private final HookHandler hookHandler;

    @SuppressLint("WrongConstant")
    public CameraHookCapturer(Context context, String cameraName, CameraEventsHandler eventsHandler, HookHandler hookHandler) {
        super(cameraName, eventsHandler, new Camera2Enumerator(context));
        this.context = context;
        this.cameraManager = (CameraManager)context.getSystemService("camera");
        this.hookHandler = hookHandler;
    }

    protected void createCameraSession(CameraSession.CreateSessionCallback createSessionCallback, CameraSession.Events events, Context applicationContext, SurfaceTextureHelper surfaceTextureHelper, MediaRecorder mediaRecoder, String cameraName, int width, int height, int framerate) {
        CameraHookSession.create(this.hookHandler, createSessionCallback, events, applicationContext, this.cameraManager, surfaceTextureHelper, mediaRecoder, cameraName, width, height, framerate);
    }
}
