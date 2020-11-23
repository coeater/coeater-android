package com.coeater.android.customCamera;


import org.webrtc.VideoFrame;

interface CameraSession {
    void stop();

    public interface Events {
        void onCameraOpening();

        void onCameraError(CameraSession var1, String var2);

        void onCameraDisconnected(CameraSession var1);

        void onCameraClosed(CameraSession var1);

        void onFrameCaptured(CameraSession var1, VideoFrame var2);
    }

    public interface CreateSessionCallback {
        void onDone(CameraSession var1);

        void onFailure(CameraSession.FailureType var1, String var2);
    }

    public static enum FailureType {
        ERROR,
        DISCONNECTED;

        private FailureType() {
        }
    }
}
