package com.coeater.android.customCamera;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//
import android.content.Context;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import java.util.Arrays;
import javax.annotation.Nullable;

import org.webrtc.CameraEnumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.CameraVideoCapturer.CameraEventsHandler;
import org.webrtc.CameraVideoCapturer.CameraStatistics;
import org.webrtc.CameraVideoCapturer.CameraSwitchHandler;
import org.webrtc.CameraVideoCapturer.MediaRecorderHandler;
import org.webrtc.Logging;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.VideoCapturer.CapturerObserver;
import org.webrtc.VideoFrame;

abstract class CameraCapturer implements CameraVideoCapturer {
    private static final String TAG = "CameraCapturer";
    private static final int MAX_OPEN_CAMERA_ATTEMPTS = 3;
    private static final int OPEN_CAMERA_DELAY_MS = 500;
    private static final int OPEN_CAMERA_TIMEOUT = 10000;
    private final CameraEnumerator cameraEnumerator;
    @Nullable
    private final CameraEventsHandler eventsHandler;
    private final Handler uiThreadHandler;
    @Nullable
    private final CameraSession.CreateSessionCallback createSessionCallback = new CameraSession.CreateSessionCallback() {
        public void onDone(CameraSession session) {
            CameraCapturer.this.checkIsOnCameraThread();
            Logging.d("CameraCapturer", "Create session done. Switch state: " + CameraCapturer.this.switchState + ". MediaRecorder state: " + CameraCapturer.this.mediaRecorderState);
            CameraCapturer.this.uiThreadHandler.removeCallbacks(CameraCapturer.this.openCameraTimeoutRunnable);
            synchronized(CameraCapturer.this.stateLock) {
                CameraCapturer.this.capturerObserver.onCapturerStarted(true);
                CameraCapturer.this.sessionOpening = false;
                CameraCapturer.this.currentSession = session;
                CameraCapturer.this.cameraStatistics = new CameraStatistics(CameraCapturer.this.surfaceHelper, CameraCapturer.this.eventsHandler);
                CameraCapturer.this.firstFrameObserved = false;
                CameraCapturer.this.stateLock.notifyAll();
                if (CameraCapturer.this.switchState == CameraCapturer.SwitchState.IN_PROGRESS) {
                    if (CameraCapturer.this.switchEventsHandler != null) {
                        CameraCapturer.this.switchEventsHandler.onCameraSwitchDone(CameraCapturer.this.cameraEnumerator.isFrontFacing(CameraCapturer.this.cameraName));
                        CameraCapturer.this.switchEventsHandler = null;
                    }

                    CameraCapturer.this.switchState = CameraCapturer.SwitchState.IDLE;
                } else if (CameraCapturer.this.switchState == CameraCapturer.SwitchState.PENDING) {
                    CameraCapturer.this.switchState = CameraCapturer.SwitchState.IDLE;
                    CameraCapturer.this.switchCameraInternal(CameraCapturer.this.switchEventsHandler);
                }

                if (CameraCapturer.this.mediaRecorderState == CameraCapturer.MediaRecorderState.IDLE_TO_ACTIVE || CameraCapturer.this.mediaRecorderState == CameraCapturer.MediaRecorderState.ACTIVE_TO_IDLE) {
                    if (CameraCapturer.this.mediaRecorderEventsHandler != null) {
                        CameraCapturer.this.mediaRecorderEventsHandler.onMediaRecorderSuccess();
                        CameraCapturer.this.mediaRecorderEventsHandler = null;
                    }

                    if (CameraCapturer.this.mediaRecorderState == CameraCapturer.MediaRecorderState.IDLE_TO_ACTIVE) {
                        CameraCapturer.this.mediaRecorderState = CameraCapturer.MediaRecorderState.ACTIVE;
                    } else {
                        CameraCapturer.this.mediaRecorderState = CameraCapturer.MediaRecorderState.IDLE;
                    }
                }

            }
        }

        public void onFailure(CameraSession.FailureType failureType, String error) {
            CameraCapturer.this.checkIsOnCameraThread();
            CameraCapturer.this.uiThreadHandler.removeCallbacks(CameraCapturer.this.openCameraTimeoutRunnable);
            synchronized(CameraCapturer.this.stateLock) {
                CameraCapturer.this.capturerObserver.onCapturerStarted(false);
                CameraCapturer.this.openAttemptsRemaining--;
                if (CameraCapturer.this.openAttemptsRemaining <= 0) {
                    Logging.w("CameraCapturer", "Opening camera failed, passing: " + error);
                    CameraCapturer.this.sessionOpening = false;
                    CameraCapturer.this.stateLock.notifyAll();
                    if (CameraCapturer.this.switchState != CameraCapturer.SwitchState.IDLE) {
                        if (CameraCapturer.this.switchEventsHandler != null) {
                            CameraCapturer.this.switchEventsHandler.onCameraSwitchError(error);
                            CameraCapturer.this.switchEventsHandler = null;
                        }

                        CameraCapturer.this.switchState = CameraCapturer.SwitchState.IDLE;
                    }

                    if (CameraCapturer.this.mediaRecorderState != CameraCapturer.MediaRecorderState.IDLE) {
                        if (CameraCapturer.this.mediaRecorderEventsHandler != null) {
                            CameraCapturer.this.mediaRecorderEventsHandler.onMediaRecorderError(error);
                            CameraCapturer.this.mediaRecorderEventsHandler = null;
                        }

                        CameraCapturer.this.mediaRecorderState = CameraCapturer.MediaRecorderState.IDLE;
                    }

                    if (failureType == CameraSession.FailureType.DISCONNECTED) {
                        CameraCapturer.this.eventsHandler.onCameraDisconnected();
                    } else {
                        CameraCapturer.this.eventsHandler.onCameraError(error);
                    }
                } else {
                    Logging.w("CameraCapturer", "Opening camera failed, retry: " + error);
                    CameraCapturer.this.createSessionInternal(500, (MediaRecorder)null);
                }

            }
        }
    };
    @Nullable
    private final CameraSession.Events cameraSessionEventsHandler = new CameraSession.Events() {
        public void onCameraOpening() {
            CameraCapturer.this.checkIsOnCameraThread();
            synchronized(CameraCapturer.this.stateLock) {
                if (CameraCapturer.this.currentSession != null) {
                    Logging.w("CameraCapturer", "onCameraOpening while session was open.");
                } else {
                    CameraCapturer.this.eventsHandler.onCameraOpening(CameraCapturer.this.cameraName);
                }
            }
        }

        public void onCameraError(CameraSession session, String error) {
            CameraCapturer.this.checkIsOnCameraThread();
            synchronized(CameraCapturer.this.stateLock) {
                if (session != CameraCapturer.this.currentSession) {
                    Logging.w("CameraCapturer", "onCameraError from another session: " + error);
                } else {
                    CameraCapturer.this.eventsHandler.onCameraError(error);
                    CameraCapturer.this.stopCapture();
                }
            }
        }

        public void onCameraDisconnected(CameraSession session) {
            CameraCapturer.this.checkIsOnCameraThread();
            synchronized(CameraCapturer.this.stateLock) {
                if (session != CameraCapturer.this.currentSession) {
                    Logging.w("CameraCapturer", "onCameraDisconnected from another session.");
                } else {
                    CameraCapturer.this.eventsHandler.onCameraDisconnected();
                    CameraCapturer.this.stopCapture();
                }
            }
        }

        public void onCameraClosed(CameraSession session) {
            CameraCapturer.this.checkIsOnCameraThread();
            synchronized(CameraCapturer.this.stateLock) {
                if (session != CameraCapturer.this.currentSession && CameraCapturer.this.currentSession != null) {
                    Logging.d("CameraCapturer", "onCameraClosed from another session.");
                } else {
                    CameraCapturer.this.eventsHandler.onCameraClosed();
                }
            }
        }

        public void onFrameCaptured(CameraSession session, VideoFrame frame) {
            CameraCapturer.this.checkIsOnCameraThread();
            synchronized(CameraCapturer.this.stateLock) {
                if (session != CameraCapturer.this.currentSession) {
                    Logging.w("CameraCapturer", "onTextureFrameCaptured from another session.");
                } else {
                    if (!CameraCapturer.this.firstFrameObserved) {
                        CameraCapturer.this.eventsHandler.onFirstFrameAvailable();
                        CameraCapturer.this.firstFrameObserved = true;
                    }

                    CameraCapturer.this.cameraStatistics.addFrame();
                    CameraCapturer.this.capturerObserver.onFrameCaptured(frame);
                }
            }
        }
    };
    private final Runnable openCameraTimeoutRunnable = new Runnable() {
        public void run() {
            CameraCapturer.this.eventsHandler.onCameraError("Camera failed to start within timeout.");
        }
    };
    @Nullable
    private Handler cameraThreadHandler;
    private Context applicationContext;
    private CapturerObserver capturerObserver;
    @Nullable
    private SurfaceTextureHelper surfaceHelper;
    private final Object stateLock = new Object();
    private boolean sessionOpening;
    @Nullable
    private CameraSession currentSession;
    private String cameraName;
    private int width;
    private int height;
    private int framerate;
    private int openAttemptsRemaining;
    private CameraCapturer.SwitchState switchState;
    @Nullable
    private CameraSwitchHandler switchEventsHandler;
    @Nullable
    private CameraStatistics cameraStatistics;
    private boolean firstFrameObserved;
    private CameraCapturer.MediaRecorderState mediaRecorderState;
    @Nullable
    private MediaRecorderHandler mediaRecorderEventsHandler;

    public CameraCapturer(String cameraName, @Nullable CameraEventsHandler eventsHandler, CameraEnumerator cameraEnumerator) {
        this.switchState = CameraCapturer.SwitchState.IDLE;
        this.mediaRecorderState = CameraCapturer.MediaRecorderState.IDLE;
        if (eventsHandler == null) {
            eventsHandler = new CameraEventsHandler() {
                public void onCameraError(String errorDescription) {
                }

                public void onCameraDisconnected() {
                }

                public void onCameraFreezed(String errorDescription) {
                }

                public void onCameraOpening(String cameraName) {
                }

                public void onFirstFrameAvailable() {
                }

                public void onCameraClosed() {
                }
            };
        }

        this.eventsHandler = eventsHandler;
        this.cameraEnumerator = cameraEnumerator;
        this.cameraName = cameraName;
        this.uiThreadHandler = new Handler(Looper.getMainLooper());
        String[] deviceNames = cameraEnumerator.getDeviceNames();
        if (deviceNames.length == 0) {
            throw new RuntimeException("No cameras attached.");
        } else if (!Arrays.asList(deviceNames).contains(this.cameraName)) {
            throw new IllegalArgumentException("Camera name " + this.cameraName + " does not match any known camera device.");
        }
    }

    public void initialize(@Nullable SurfaceTextureHelper surfaceTextureHelper, Context applicationContext, CapturerObserver capturerObserver) {
        this.applicationContext = applicationContext;
        this.capturerObserver = capturerObserver;
        this.surfaceHelper = surfaceTextureHelper;
        this.cameraThreadHandler = surfaceTextureHelper == null ? null : surfaceTextureHelper.getHandler();
    }

    public void startCapture(int width, int height, int framerate) {
        Logging.d("CameraCapturer", "startCapture: " + width + "x" + height + "@" + framerate);
        if (this.applicationContext == null) {
            throw new RuntimeException("CameraCapturer must be initialized before calling startCapture.");
        } else {
            synchronized(this.stateLock) {
                if (!this.sessionOpening && this.currentSession == null) {
                    this.width = width;
                    this.height = height;
                    this.framerate = framerate;
                    this.sessionOpening = true;
                    this.openAttemptsRemaining = 3;
                    this.createSessionInternal(0, (MediaRecorder)null);
                } else {
                    Logging.w("CameraCapturer", "Session already open");
                }
            }
        }
    }

    private void createSessionInternal(int delayMs, final MediaRecorder mediaRecorder) {
        this.uiThreadHandler.postDelayed(this.openCameraTimeoutRunnable, (long)(delayMs + 10000));
        this.cameraThreadHandler.postDelayed(new Runnable() {
            public void run() {
                CameraCapturer.this.createCameraSession(CameraCapturer.this.createSessionCallback, CameraCapturer.this.cameraSessionEventsHandler, CameraCapturer.this.applicationContext, CameraCapturer.this.surfaceHelper, mediaRecorder, CameraCapturer.this.cameraName, CameraCapturer.this.width, CameraCapturer.this.height, CameraCapturer.this.framerate);
            }
        }, (long)delayMs);
    }

    public void stopCapture() {
        Logging.d("CameraCapturer", "Stop capture");
        synchronized(this.stateLock) {
            while(this.sessionOpening) {
                Logging.d("CameraCapturer", "Stop capture: Waiting for session to open");

                try {
                    this.stateLock.wait();
                } catch (InterruptedException var4) {
                    Logging.w("CameraCapturer", "Stop capture interrupted while waiting for the session to open.");
                    Thread.currentThread().interrupt();
                    return;
                }
            }

            if (this.currentSession != null) {
                Logging.d("CameraCapturer", "Stop capture: Nulling session");
                this.cameraStatistics.release();
                this.cameraStatistics = null;
                final CameraSession oldSession = this.currentSession;
                this.cameraThreadHandler.post(new Runnable() {
                    public void run() {
                        oldSession.stop();
                    }
                });
                this.currentSession = null;
                this.capturerObserver.onCapturerStopped();
            } else {
                Logging.d("CameraCapturer", "Stop capture: No session open");
            }
        }

        Logging.d("CameraCapturer", "Stop capture done");
    }

    public void changeCaptureFormat(int width, int height, int framerate) {
        Logging.d("CameraCapturer", "changeCaptureFormat: " + width + "x" + height + "@" + framerate);
        synchronized(this.stateLock) {
            this.stopCapture();
            this.startCapture(width, height, framerate);
        }
    }

    public void dispose() {
        Logging.d("CameraCapturer", "dispose");
        this.stopCapture();
    }

    public void switchCamera(final CameraSwitchHandler switchEventsHandler) {
        Logging.d("CameraCapturer", "switchCamera");
        this.cameraThreadHandler.post(new Runnable() {
            public void run() {
                CameraCapturer.this.switchCameraInternal(switchEventsHandler);
            }
        });
    }

    public void addMediaRecorderToCamera(final MediaRecorder mediaRecorder, final MediaRecorderHandler mediaRecoderEventsHandler) {
        Logging.d("CameraCapturer", "addMediaRecorderToCamera");
        this.cameraThreadHandler.post(new Runnable() {
            public void run() {
                CameraCapturer.this.updateMediaRecorderInternal(mediaRecorder, mediaRecoderEventsHandler);
            }
        });
    }

    public void removeMediaRecorderFromCamera(final MediaRecorderHandler mediaRecoderEventsHandler) {
        Logging.d("CameraCapturer", "removeMediaRecorderFromCamera");
        this.cameraThreadHandler.post(new Runnable() {
            public void run() {
                CameraCapturer.this.updateMediaRecorderInternal((MediaRecorder)null, mediaRecoderEventsHandler);
            }
        });
    }

    public boolean isScreencast() {
        return false;
    }

    public void printStackTrace() {
        Thread cameraThread = null;
        if (this.cameraThreadHandler != null) {
            cameraThread = this.cameraThreadHandler.getLooper().getThread();
        }

        if (cameraThread != null) {
            StackTraceElement[] cameraStackTrace = cameraThread.getStackTrace();
            if (cameraStackTrace.length > 0) {
                Logging.d("CameraCapturer", "CameraCapturer stack trace:");
                StackTraceElement[] var3 = cameraStackTrace;
                int var4 = cameraStackTrace.length;

                for(int var5 = 0; var5 < var4; ++var5) {
                    StackTraceElement traceElem = var3[var5];
                    Logging.d("CameraCapturer", traceElem.toString());
                }
            }
        }

    }

    private void reportCameraSwitchError(String error, @Nullable CameraSwitchHandler switchEventsHandler) {
        Logging.e("CameraCapturer", error);
        if (switchEventsHandler != null) {
            switchEventsHandler.onCameraSwitchError(error);
        }

    }

    private void switchCameraInternal(@Nullable CameraSwitchHandler switchEventsHandler) {
        Logging.d("CameraCapturer", "switchCamera internal");
        String[] deviceNames = this.cameraEnumerator.getDeviceNames();
        if (deviceNames.length < 2) {
            if (switchEventsHandler != null) {
                switchEventsHandler.onCameraSwitchError("No camera to switch to.");
            }

        } else {
            synchronized(this.stateLock) {
                if (this.switchState != CameraCapturer.SwitchState.IDLE) {
                    this.reportCameraSwitchError("Camera switch already in progress.", switchEventsHandler);
                    return;
                }

                if (this.mediaRecorderState != CameraCapturer.MediaRecorderState.IDLE) {
                    this.reportCameraSwitchError("switchCamera: media recording is active", switchEventsHandler);
                    return;
                }

                if (!this.sessionOpening && this.currentSession == null) {
                    this.reportCameraSwitchError("switchCamera: camera is not running.", switchEventsHandler);
                    return;
                }

                this.switchEventsHandler = switchEventsHandler;
                if (this.sessionOpening) {
                    this.switchState = CameraCapturer.SwitchState.PENDING;
                    return;
                }

                this.switchState = CameraCapturer.SwitchState.IN_PROGRESS;
                Logging.d("CameraCapturer", "switchCamera: Stopping session");
                this.cameraStatistics.release();
                this.cameraStatistics = null;
                final CameraSession oldSession = this.currentSession;
                this.cameraThreadHandler.post(new Runnable() {
                    public void run() {
                        oldSession.stop();
                    }
                });
                this.currentSession = null;
                int cameraNameIndex = Arrays.asList(deviceNames).indexOf(this.cameraName);
                this.cameraName = deviceNames[(cameraNameIndex + 1) % deviceNames.length];
                this.sessionOpening = true;
                this.openAttemptsRemaining = 1;
                this.createSessionInternal(0, (MediaRecorder)null);
            }

            Logging.d("CameraCapturer", "switchCamera done");
        }
    }

    private void reportUpdateMediaRecorderError(String error, @Nullable MediaRecorderHandler mediaRecoderEventsHandler) {
        this.checkIsOnCameraThread();
        Logging.e("CameraCapturer", error);
        if (mediaRecoderEventsHandler != null) {
            mediaRecoderEventsHandler.onMediaRecorderError(error);
        }

    }

    private void updateMediaRecorderInternal(@Nullable MediaRecorder mediaRecorder, MediaRecorderHandler mediaRecoderEventsHandler) {
        this.checkIsOnCameraThread();
        boolean addMediaRecorder = mediaRecorder != null;
        Logging.d("CameraCapturer", "updateMediaRecoderInternal internal. State: " + this.mediaRecorderState + ". Switch state: " + this.switchState + ". Add MediaRecorder: " + addMediaRecorder);
        synchronized(this.stateLock) {
            if (addMediaRecorder && this.mediaRecorderState != CameraCapturer.MediaRecorderState.IDLE || !addMediaRecorder && this.mediaRecorderState != CameraCapturer.MediaRecorderState.ACTIVE) {
                this.reportUpdateMediaRecorderError("Incorrect state for MediaRecorder update.", mediaRecoderEventsHandler);
                return;
            }

            if (this.switchState != CameraCapturer.SwitchState.IDLE) {
                this.reportUpdateMediaRecorderError("MediaRecorder update while camera is switching.", mediaRecoderEventsHandler);
                return;
            }

            if (this.currentSession == null) {
                this.reportUpdateMediaRecorderError("MediaRecorder update while camera is closed.", mediaRecoderEventsHandler);
                return;
            }

            if (this.sessionOpening) {
                this.reportUpdateMediaRecorderError("MediaRecorder update while camera is still opening.", mediaRecoderEventsHandler);
                return;
            }

            this.mediaRecorderEventsHandler = mediaRecoderEventsHandler;
            this.mediaRecorderState = addMediaRecorder ? CameraCapturer.MediaRecorderState.IDLE_TO_ACTIVE : CameraCapturer.MediaRecorderState.ACTIVE_TO_IDLE;
            Logging.d("CameraCapturer", "updateMediaRecoder: Stopping session");
            this.cameraStatistics.release();
            this.cameraStatistics = null;
            final CameraSession oldSession = this.currentSession;
            this.cameraThreadHandler.post(new Runnable() {
                public void run() {
                    oldSession.stop();
                }
            });
            this.currentSession = null;
            this.sessionOpening = true;
            this.openAttemptsRemaining = 1;
            this.createSessionInternal(0, mediaRecorder);
        }

        Logging.d("CameraCapturer", "updateMediaRecoderInternal done");
    }

    private void checkIsOnCameraThread() {
        if (Thread.currentThread() != this.cameraThreadHandler.getLooper().getThread()) {
            Logging.e("CameraCapturer", "Check is on camera thread failed.");
            throw new RuntimeException("Not on camera thread.");
        }
    }

    protected String getCameraName() {
        synchronized(this.stateLock) {
            return this.cameraName;
        }
    }

    protected abstract void createCameraSession(CameraSession.CreateSessionCallback var1, CameraSession.Events var2, Context var3, SurfaceTextureHelper var4, MediaRecorder var5, String var6, int var7, int var8, int var9);

    static enum MediaRecorderState {
        IDLE,
        IDLE_TO_ACTIVE,
        ACTIVE_TO_IDLE,
        ACTIVE;

        private MediaRecorderState() {
        }
    }

    static enum SwitchState {
        IDLE,
        PENDING,
        IN_PROGRESS;

        private SwitchState() {
        }
    }
}
