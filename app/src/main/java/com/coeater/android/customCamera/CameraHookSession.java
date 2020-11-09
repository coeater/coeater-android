//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.coeater.android.customCamera;
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CameraCaptureSession.CaptureCallback;
import android.hardware.camera2.CameraCaptureSession.StateCallback;
import android.hardware.camera2.CaptureRequest.Builder;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.Face;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.util.Range;
import android.view.Surface;
import android.view.WindowManager;

import androidx.core.app.ActivityCompat;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;
import org.webrtc.CameraEnumerationAndroid;
import org.webrtc.CameraEnumerationAndroid.CaptureFormat;
import org.webrtc.CameraEnumerationAndroid.CaptureFormat.FramerateRange;
import org.webrtc.Logging;
import org.webrtc.RendererCommon;
import org.webrtc.Size;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceTextureHelper.OnTextureFrameAvailableListener;
import org.webrtc.VideoFrame;
import org.webrtc.VideoFrame.Buffer;

class CameraHookSession implements CameraSession {
    private static final String TAG = "CameraHookSession";
    private static final Histogram camera2StartTimeMsHistogram = Histogram.createCounts("WebRTC.Android.Camera2.StartTimeMs", 1, 10000, 50);
    private static final Histogram camera2StopTimeMsHistogram = Histogram.createCounts("WebRTC.Android.Camera2.StopTimeMs", 1, 10000, 50);
    private static final Histogram camera2ResolutionHistogram;
    private final Handler cameraThreadHandler;
    private final CreateSessionCallback callback;
    private final Events events;
    private final Context applicationContext;
    private final CameraManager cameraManager;
    private final SurfaceTextureHelper surfaceTextureHelper;
    @Nullable
    private final Surface mediaRecorderSurface;
    private final String cameraId;
    private final int width;
    private final int height;
    private final int framerate;
    private CameraCharacteristics cameraCharacteristics;
    private int cameraOrientation;
    private boolean isCameraFrontFacing;
    private int fpsUnitFactor;
    private CaptureFormat captureFormat;
    @Nullable
    private CameraDevice cameraDevice;
    @Nullable
    private Surface surface;
    @Nullable
    private CameraCaptureSession captureSession;
    private CameraHookSession.SessionState state;
    private boolean firstFrameReported;
    private final long constructionTimeNs;
    private HookHandler hookHandler;
    static final ArrayList<Size> COMMON_RESOLUTIONS = new ArrayList(Arrays.asList(new Size(160, 120), new Size(240, 160), new Size(320, 240), new Size(400, 240), new Size(480, 320), new Size(640, 360), new Size(640, 480), new Size(768, 480), new Size(854, 480), new Size(800, 600), new Size(960, 540), new Size(960, 640), new Size(1024, 576), new Size(1024, 600), new Size(1280, 720), new Size(1280, 1024), new Size(1920, 1080), new Size(1920, 1440), new Size(2560, 1440), new Size(3840, 2160)));

    public static void create(HookHandler hookHandler, CreateSessionCallback callback, Events events, Context applicationContext, CameraManager cameraManager, SurfaceTextureHelper surfaceTextureHelper, MediaRecorder mediaRecorder, String cameraId, int width, int height, int framerate) {
        new CameraHookSession(hookHandler, callback, events, applicationContext, cameraManager, surfaceTextureHelper, mediaRecorder, cameraId, width, height, framerate);
    }

    private CameraHookSession(HookHandler hookHandler, CreateSessionCallback callback, Events events, Context applicationContext, CameraManager cameraManager, SurfaceTextureHelper surfaceTextureHelper, @Nullable MediaRecorder mediaRecorder, String cameraId, int width, int height, int framerate) {
        this.hookHandler = hookHandler;
        this.state = CameraHookSession.SessionState.RUNNING;
        this.firstFrameReported = false;
        Logging.d("CameraHookSession", "Create new camera2 session on camera " + cameraId);
        this.constructionTimeNs = System.nanoTime();
        this.cameraThreadHandler = new Handler();
        this.callback = callback;
        this.events = events;
        this.applicationContext = applicationContext;
        this.cameraManager = cameraManager;
        this.surfaceTextureHelper = surfaceTextureHelper;
        this.mediaRecorderSurface = mediaRecorder != null ? mediaRecorder.getSurface() : null;
        this.cameraId = cameraId;
        this.width = width;
        this.height = height;
        this.framerate = framerate;
        this.start();
    }

    private void start() {
        this.checkIsOnCameraThread();
        Logging.d("CameraHookSession", "start");

        try {
            this.cameraCharacteristics = this.cameraManager.getCameraCharacteristics(this.cameraId);
        } catch (CameraAccessException var2) {
            this.reportError("getCameraCharacteristics(): " + var2.getMessage());
            return;
        }

        this.cameraOrientation = (Integer) this.cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        this.isCameraFrontFacing = (Integer) this.cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == 0;
        this.findCaptureFormat();
        this.openCamera();
    }

    static int getFpsUnitFactor(Range<Integer>[] fpsRanges) {
        if (fpsRanges.length == 0) {
            return 1000;
        } else {
            return (Integer) fpsRanges[0].getUpper() < 1000 ? 1000 : 1;
        }
    }

    static List<FramerateRange> convertFramerates(Range<Integer>[] arrayRanges, int unitFactor) {
        List<FramerateRange> ranges = new ArrayList();
        Range[] var3 = arrayRanges;
        int var4 = arrayRanges.length;

        for (int var5 = 0; var5 < var4; ++var5) {
            Range<Integer> range = var3[var5];
            ranges.add(new FramerateRange((Integer) range.getLower() * unitFactor, (Integer) range.getUpper() * unitFactor));
        }

        return ranges;
    }

    private static List<Size> convertSizes(android.util.Size[] cameraSizes) {
        List<Size> sizes = new ArrayList();
        android.util.Size[] var2 = cameraSizes;
        int var3 = cameraSizes.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            android.util.Size size = var2[var4];
            sizes.add(new Size(size.getWidth(), size.getHeight()));
        }

        return sizes;
    }

    static List<Size> getSupportedSizes(CameraCharacteristics cameraCharacteristics) {
        StreamConfigurationMap streamMap = (StreamConfigurationMap) cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        int supportLevel = (Integer) cameraCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
        android.util.Size[] nativeSizes = streamMap.getOutputSizes(SurfaceTexture.class);
        List<Size> sizes = convertSizes(nativeSizes);
        if (Build.VERSION.SDK_INT < 22 && supportLevel == 2) {
            Rect activeArraySize = (Rect) cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
            ArrayList<Size> filteredSizes = new ArrayList();
            Iterator var7 = sizes.iterator();

            while (var7.hasNext()) {
                Size size = (Size) var7.next();
                if (activeArraySize.width() * size.height == activeArraySize.height() * size.width) {
                    filteredSizes.add(size);
                }
            }

            return filteredSizes;
        } else {
            return sizes;
        }
    }

    static void reportCameraResolution(Histogram Histogram, Size resolution) {
        int index = COMMON_RESOLUTIONS.indexOf(resolution);
        Histogram.addSample(index + 1);
    }


    private void findCaptureFormat() {
        this.checkIsOnCameraThread();
        Range<Integer>[] fpsRanges = (Range[]) this.cameraCharacteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
        this.fpsUnitFactor = getFpsUnitFactor(fpsRanges);
        List<FramerateRange> framerateRanges = convertFramerates(fpsRanges, this.fpsUnitFactor);
        List<Size> sizes = getSupportedSizes(this.cameraCharacteristics);
        Logging.d("CameraHookSession", "Available preview sizes: " + sizes);
        Logging.d("CameraHookSession", "Available fps ranges: " + framerateRanges);
        if (!framerateRanges.isEmpty() && !sizes.isEmpty()) {
            FramerateRange bestFpsRange = CameraEnumerationAndroid.getClosestSupportedFramerateRange(framerateRanges, this.framerate);
            Size bestSize = CameraEnumerationAndroid.getClosestSupportedSize(sizes, this.width, this.height);
            reportCameraResolution(camera2ResolutionHistogram, bestSize);
            this.captureFormat = new CaptureFormat(bestSize.width, bestSize.height, bestFpsRange);
            Logging.d("CameraHookSession", "Using capture format: " + this.captureFormat);
        } else {
            this.reportError("No supported capture formats.");
        }
    }

    private void openCamera() {
        this.checkIsOnCameraThread();
        Logging.d("CameraHookSession", "Opening camera " + this.cameraId);
        this.events.onCameraOpening();

        try {
            if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                this.reportError("Failed to open camera: ");
            }
            this.cameraManager.openCamera(this.cameraId, new CameraHookSession.CameraStateCallback(), this.cameraThreadHandler);
        } catch (CameraAccessException var2) {
            this.reportError("Failed to open camera: " + var2);
        }
    }

    public void stop() {
        Logging.d("CameraHookSession", "Stop camera2 session on camera " + this.cameraId);
        this.checkIsOnCameraThread();
        if (this.state != CameraHookSession.SessionState.STOPPED) {
            long stopStartTime = System.nanoTime();
            this.state = CameraHookSession.SessionState.STOPPED;
            this.stopInternal();
            int stopTimeMs = (int)TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - stopStartTime);
            camera2StopTimeMsHistogram.addSample(stopTimeMs);
        }

    }

    private void stopInternal() {
        Logging.d("CameraHookSession", "Stop internal");
        this.checkIsOnCameraThread();
        this.surfaceTextureHelper.stopListening();
        if (this.captureSession != null) {
            this.captureSession.close();
            this.captureSession = null;
        }

        if (this.surface != null) {
            this.surface.release();
            this.surface = null;
        }

        if (this.cameraDevice != null) {
            this.cameraDevice.close();
            this.cameraDevice = null;
        }

        Logging.d("CameraHookSession", "Stop done");
    }

    private void reportError(String error) {
        this.checkIsOnCameraThread();
        Logging.e("CameraHookSession", "Error: " + error);
        boolean startFailure = this.captureSession == null && this.state != CameraHookSession.SessionState.STOPPED;
        this.state = CameraHookSession.SessionState.STOPPED;
        this.stopInternal();
        if (startFailure) {
            this.callback.onFailure(FailureType.ERROR, error);
        } else {
            this.events.onCameraError(this, error);
        }

    }

    private int getDeviceOrientation() {
        @SuppressLint("WrongConstant") WindowManager wm = (WindowManager)this.applicationContext.getSystemService("window");
        short orientation;
        switch(wm.getDefaultDisplay().getRotation()) {
            case 0:
            default:
                orientation = 0;
                break;
            case 1:
                orientation = 90;
                break;
            case 2:
                orientation = 180;
                break;
            case 3:
                orientation = 270;
        }

        return orientation;
    }

    private int getFrameOrientation() {
        int rotation = this.getDeviceOrientation();
        if (!this.isCameraFrontFacing) {
            rotation = 360 - rotation;
        }

        return (this.cameraOrientation + rotation) % 360;
    }

    private void checkIsOnCameraThread() {
        if (Thread.currentThread() != this.cameraThreadHandler.getLooper().getThread()) {
            throw new IllegalStateException("Wrong thread");
        }
    }

    static {
        camera2ResolutionHistogram = Histogram.createEnumeration("WebRTC.Android.Camera2.Resolution", COMMON_RESOLUTIONS.size());
    }

    private class CameraCaptureCallback extends CaptureCallback {
        private CameraCaptureCallback() {

        }
        private void process(CaptureResult result) {
            Integer mode = result.get(CaptureResult.STATISTICS_FACE_DETECT_MODE);
            Face[] faces = result.get(CaptureResult.STATISTICS_FACES);
            Integer max = cameraCharacteristics.get(CameraCharacteristics.STATISTICS_INFO_MAX_FACE_COUNT);
            if(faces != null && mode != null) {
                Logging.e("tag", "max-count : "+max+", faces : " + faces.length + " , mode : " + mode );
                for(Face f: faces)
                    CameraHookSession.this.hookHandler.handleCapture(f);

            }
        }

        @Override
        public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request,
                                       CaptureResult result) {
            Logging.d("SUCCESS CameraHookSession", "Capture progress : " + result);
            process(result);
        }
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request,
                                       TotalCaptureResult result) {
            Logging.d("SUCCESS CameraHookSession", "Capture success: " + result);
            process(result);
        }

        public void onCaptureFailed(@NotNull CameraCaptureSession session, @NotNull CaptureRequest request, @NotNull CaptureFailure failure) {
            Logging.d("CameraHookSession", "Capture failed: " + failure);
        }
    }

    private class CaptureSessionCallback extends StateCallback {
        private CaptureSessionCallback() {
        }

        public void onConfigureFailed(CameraCaptureSession session) {
            CameraHookSession.this.checkIsOnCameraThread();
            session.close();
            CameraHookSession.this.reportError("Failed to configure capture session.");
        }

        public void onConfigured(CameraCaptureSession session) {
            CameraHookSession.this.checkIsOnCameraThread();
            Logging.d("CameraHookSession", "Camera capture session configured.");
            CameraHookSession.this.captureSession = session;

            try {
                @SuppressLint("WrongConstant") Builder captureRequestBuilder = CameraHookSession.this.cameraDevice.createCaptureRequest(1);
                captureRequestBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, new Range(CameraHookSession.this.captureFormat.framerate.min / CameraHookSession.this.fpsUnitFactor, CameraHookSession.this.captureFormat.framerate.max / CameraHookSession.this.fpsUnitFactor));
                captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, 1);
                captureRequestBuilder.set(CaptureRequest.CONTROL_AE_LOCK, false);
                captureRequestBuilder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, CameraMetadata.STATISTICS_FACE_DETECT_MODE_FULL);
                this.chooseStabilizationMode(captureRequestBuilder);
                this.chooseFocusMode(captureRequestBuilder);
                captureRequestBuilder.addTarget(CameraHookSession.this.surface);
                if (CameraHookSession.this.mediaRecorderSurface != null) {
                    Logging.d("CameraHookSession", "Add MediaRecorder surface to CaptureRequest.Builder");
                    captureRequestBuilder.addTarget(CameraHookSession.this.mediaRecorderSurface);
                }

                session.setRepeatingRequest(captureRequestBuilder.build(), new CameraHookSession.CameraCaptureCallback(), CameraHookSession.this.cameraThreadHandler);
            } catch (CameraAccessException var3) {
                CameraHookSession.this.reportError("Failed to start capture request. " + var3);
                return;
            }

            CameraHookSession.this.surfaceTextureHelper.startListening(new OnTextureFrameAvailableListener() {
                public void onTextureFrameAvailable(int oesTextureId, float[] transformMatrix, long timestampNs) {
                    CameraHookSession.this.checkIsOnCameraThread();
                    if (CameraHookSession.this.state != CameraHookSession.SessionState.RUNNING) {
                        Logging.d("CameraHookSession", "Texture frame captured but camera is no longer running.");
                        CameraHookSession.this.surfaceTextureHelper.returnTextureFrame();
                    } else {
                        int rotation;
                        if (!CameraHookSession.this.firstFrameReported) {
                            CameraHookSession.this.firstFrameReported = true;
                            rotation = (int)TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - CameraHookSession.this.constructionTimeNs);
                            CameraHookSession.camera2StartTimeMsHistogram.addSample(rotation);
                        }

                        rotation = CameraHookSession.this.getFrameOrientation();
                        if (CameraHookSession.this.isCameraFrontFacing) {
                            transformMatrix = RendererCommon.multiplyMatrices(transformMatrix, RendererCommon.horizontalFlipMatrix());
                        }

                        transformMatrix = RendererCommon.rotateTextureMatrix(transformMatrix, (float)(-CameraHookSession.this.cameraOrientation));
                        Buffer buffer = CameraHookSession.this.surfaceTextureHelper.createTextureBuffer(CameraHookSession.this.captureFormat.width, CameraHookSession.this.captureFormat.height, RendererCommon.convertMatrixToAndroidGraphicsMatrix(transformMatrix));
                        VideoFrame frame = new VideoFrame(buffer, rotation, timestampNs);
                        CameraHookSession.this.events.onFrameCaptured(CameraHookSession.this, frame);
                        frame.release();
                    }
                }
            });
            Logging.d("CameraHookSession", "Camera device successfully started.");
            CameraHookSession.this.callback.onDone(CameraHookSession.this);
        }

        private void chooseStabilizationMode(Builder captureRequestBuilder) {
            int[] availableOpticalStabilization = (int[]) CameraHookSession.this.cameraCharacteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION);
            int[] availableVideoStabilization;
            int var5;
            int mode;
            if (availableOpticalStabilization != null) {
                availableVideoStabilization = availableOpticalStabilization;
                int var4 = availableOpticalStabilization.length;

                for(var5 = 0; var5 < var4; ++var5) {
                    mode = availableVideoStabilization[var5];
                    if (mode == 1) {
                        captureRequestBuilder.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, 1);
                        captureRequestBuilder.set(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE, 0);
                        Logging.d("CameraHookSession", "Using optical stabilization.");
                        return;
                    }
                }
            }

            availableVideoStabilization = (int[]) CameraHookSession.this.cameraCharacteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES);
            int[] var8 = availableVideoStabilization;
            var5 = availableVideoStabilization.length;

            for(mode = 0; mode < var5; ++mode) {
                int modex = var8[mode];
                if (modex == 1) {
                    captureRequestBuilder.set(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE, 1);
                    captureRequestBuilder.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, 0);
                    Logging.d("CameraHookSession", "Using video stabilization.");
                    return;
                }
            }

            Logging.d("CameraHookSession", "Stabilization not available.");
        }

        private void chooseFocusMode(Builder captureRequestBuilder) {
            int[] availableFocusModes = (int[]) CameraHookSession.this.cameraCharacteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);
            int[] var3 = availableFocusModes;
            int var4 = availableFocusModes.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                int mode = var3[var5];
                if (mode == 3) {
                    captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, 3);
                    Logging.d("CameraHookSession", "Using continuous video auto-focus.");
                    return;
                }
            }

            Logging.d("CameraHookSession", "Auto-focus is not available.");
        }
    }

    private class CameraStateCallback extends android.hardware.camera2.CameraDevice.StateCallback {
        private CameraStateCallback() {
        }

        private String getErrorDescription(int errorCode) {
            switch(errorCode) {
                case 1:
                    return "Camera device is in use already.";
                case 2:
                    return "Camera device could not be opened because there are too many other open camera devices.";
                case 3:
                    return "Camera device could not be opened due to a device policy.";
                case 4:
                    return "Camera device has encountered a fatal error.";
                case 5:
                    return "Camera service has encountered a fatal error.";
                default:
                    return "Unknown camera error: " + errorCode;
            }
        }

        public void onDisconnected(CameraDevice camera) {
            CameraHookSession.this.checkIsOnCameraThread();
            boolean startFailure = CameraHookSession.this.captureSession == null && CameraHookSession.this.state != CameraHookSession.SessionState.STOPPED;
            CameraHookSession.this.state = CameraHookSession.SessionState.STOPPED;
            CameraHookSession.this.stopInternal();
            if (startFailure) {
                CameraHookSession.this.callback.onFailure(FailureType.DISCONNECTED, "Camera disconnected / evicted.");
            } else {
                CameraHookSession.this.events.onCameraDisconnected(CameraHookSession.this);
            }

        }

        public void onError(CameraDevice camera, int errorCode) {
            CameraHookSession.this.checkIsOnCameraThread();
            CameraHookSession.this.reportError(this.getErrorDescription(errorCode));
        }

        public void onOpened(CameraDevice camera) {
            CameraHookSession.this.checkIsOnCameraThread();
            Logging.d("CameraHookSession", "Camera opened.");
            CameraHookSession.this.cameraDevice = camera;
            SurfaceTexture surfaceTexture = CameraHookSession.this.surfaceTextureHelper.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(CameraHookSession.this.captureFormat.width, CameraHookSession.this.captureFormat.height);
            CameraHookSession.this.surface = new Surface(surfaceTexture);
            List<Surface> surfaces = new ArrayList();
            surfaces.add(CameraHookSession.this.surface);
            if (CameraHookSession.this.mediaRecorderSurface != null) {
                Logging.d("CameraHookSession", "Add MediaRecorder surface to capture session.");
                surfaces.add(CameraHookSession.this.mediaRecorderSurface);
            }

            try {
                camera.createCaptureSession(surfaces, CameraHookSession.this.new CaptureSessionCallback(), CameraHookSession.this.cameraThreadHandler);
            } catch (CameraAccessException var5) {
                CameraHookSession.this.reportError("Failed to create capture session. " + var5);
            }
        }

        public void onClosed(CameraDevice camera) {
            CameraHookSession.this.checkIsOnCameraThread();
            Logging.d("CameraHookSession", "Camera device closed.");
            CameraHookSession.this.events.onCameraClosed(CameraHookSession.this);
        }
    }

    private static enum SessionState {
        RUNNING,
        STOPPED;

        private SessionState() {
        }
    }
}
