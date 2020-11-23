package com.coeater.android.customCamera

import android.content.Context
import android.hardware.camera2.params.Face
import android.widget.TextView

class HookHandler {
    public lateinit var canvas: TextView
    public lateinit var context: Context
    constructor (context: Context, canvas: TextView) {
        this.context = context
        this.canvas = canvas
    }

    public fun handleCapture(face: Face) {
        drawFaceRect(face)
    }
    private fun drawFaceRect(face: Face) {
       canvas.text = face.leftEyePosition.toString() + " , " + face.rightEyePosition.toString()
    }
}