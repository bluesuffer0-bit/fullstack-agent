package com.fullstackagent.android.vision

import android.annotation.SuppressLint
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions

@SuppressLint("UnsafeOptInUsageError")
@Composable fun CameraGestureView(onGesture: (String) -> Unit) {
    val context = LocalContext.current; val owner = LocalLifecycleOwner.current
    AndroidView(modifier=Modifier.fillMaxSize(), factory={ PreviewView(it) }) { view ->
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            val provider=future.get(); val preview=Preview.Builder().build().also { it.surfaceProvider=view.surfaceProvider }
            val detector=PoseDetection.getClient(PoseDetectorOptions.Builder().setDetectorMode(PoseDetectorOptions.STREAM_MODE).build())
            val analysis=ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
            var last=0L
            analysis.setAnalyzer(ContextCompat.getMainExecutor(context)) { proxy ->
                val media=proxy.image
                if(media==null) proxy.close() else detector.process(InputImage.fromMediaImage(media, proxy.imageInfo.rotationDegrees)).addOnSuccessListener { pose ->
                    val lw=pose.getPoseLandmark(15); val rw=pose.getPoseLandmark(16); val ls=pose.getPoseLandmark(11); val rs=pose.getPoseLandmark(12)
                    val now=System.currentTimeMillis(); if(now-last>1200 && lw!=null && rw!=null && ls!=null && rs!=null) {
                        when { lw.position.y < ls.position.y && rw.position.y < rs.position.y -> "Both hands raised"; rw.position.y < rs.position.y -> "Right hand raised"; lw.position.y < ls.position.y -> "Left hand raised"; else -> null }?.let { last=now; onGesture(it) }
                    }
                }.addOnCompleteListener { proxy.close() }
            }
            provider.unbindAll(); provider.bindToLifecycle(owner, CameraSelector.DEFAULT_FRONT_CAMERA, preview, analysis)
        }, ContextCompat.getMainExecutor(context))
    }
}
