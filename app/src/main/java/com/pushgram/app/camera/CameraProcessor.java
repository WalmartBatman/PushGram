package com.pushgram.app.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraProcessor {

    private static final String TAG = "CameraProcessor";
    private final Context context;
    private final LifecycleOwner lifecycleOwner;
    private final PreviewView previewView;
    private final PushUpAnalyzer.PushUpListener listener;
    private PoseDetector poseDetector;
    private ExecutorService cameraExecutor;
    private ProcessCameraProvider cameraProvider;

    public CameraProcessor(Context ctx, LifecycleOwner owner,
                            PreviewView preview, PushUpAnalyzer.PushUpListener listener) {
        this.context = ctx;
        this.lifecycleOwner = owner;
        this.previewView = preview;
        this.listener = listener;
    }

    public void start() {
        cameraExecutor = Executors.newSingleThreadExecutor();
        PoseDetectorOptions opts = new PoseDetectorOptions.Builder()
                .setDetectorMode(PoseDetectorOptions.STREAM_MODE).build();
        poseDetector = PoseDetection.getClient(opts);

        ListenableFuture<ProcessCameraProvider> future =
                ProcessCameraProvider.getInstance(context);
        future.addListener(() -> {
            try {
                cameraProvider = future.get();
                bindCamera();
            } catch (Exception e) { Log.e(TAG, "Camera init failed", e); }
        }, ContextCompat.getMainExecutor(context));
    }

    private void bindCamera() {
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        PushUpAnalyzer analyzer = new PushUpAnalyzer(listener);
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
        imageAnalysis.setAnalyzer(cameraExecutor, ip -> processFrame(ip, analyzer));
        CameraSelector selector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        try {
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview, imageAnalysis);
        } catch (Exception e) { Log.e(TAG, "Bind failed", e); }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void processFrame(androidx.camera.core.ImageProxy ip, PushUpAnalyzer analyzer) {
        if (ip.getImage() == null) { ip.close(); return; }
        InputImage img = InputImage.fromMediaImage(ip.getImage(),
                ip.getImageInfo().getRotationDegrees());
        poseDetector.process(img)
                .addOnSuccessListener(analyzer::analyze)
                .addOnFailureListener(e -> Log.w(TAG, "Pose fail", e))
                .addOnCompleteListener(t -> ip.close());
    }

    public void stop() {
        if (cameraProvider != null) cameraProvider.unbindAll();
        if (cameraExecutor != null) cameraExecutor.shutdown();
        if (poseDetector != null) try { poseDetector.close(); } catch (Exception ignored) {}
    }
}
