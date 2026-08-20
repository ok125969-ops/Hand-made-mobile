package com.example.camera

import android.content.Context
import android.util.Log
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * CameraX Manager for gesture analysis and preview.
 * Handles lifecycle binding, camera switching (front/back), and background analysis execution.
 */
class CameraManager(
  private val context: Context,
  private val lifecycleOwner: LifecycleOwner,
  private val previewView: PreviewView?,
  private val analysisListener: GestureAnalysisListener
) {

  companion object {
    private const val TAG = "CameraManager"
  }

  private var cameraProvider: ProcessCameraProvider? = null
  private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
  private var frameAnalyzer: GestureFrameAnalyzer? = null

  private var isFrontCamera = true
  private var currentPerformanceMode = PerformanceMode.BALANCED
  private var currentSensitivity = 1.0f
  private var isRunning = false

  fun startCamera(
    useFrontCamera: Boolean = true,
    performanceMode: PerformanceMode = PerformanceMode.BALANCED,
    sensitivity: Float = 1.0f,
    onSuccess: () -> Unit = {},
    onError: (String) -> Unit = {}
  ) {
    this.isFrontCamera = useFrontCamera
    this.currentPerformanceMode = performanceMode
    this.currentSensitivity = sensitivity

    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    cameraProviderFuture.addListener({
      try {
        cameraProvider = cameraProviderFuture.get()
        bindCameraUseCases()
        isRunning = true
        onSuccess()
      } catch (e: Exception) {
        Log.e(TAG, "Failed to initialize CameraX", e)
        onError("Camera initialization failed: ${e.localizedMessage}")
      }
    }, ContextCompat.getMainExecutor(context))
  }

  private fun bindCameraUseCases() {
    val provider = cameraProvider ?: return
    provider.unbindAll()

    val cameraSelector = if (isFrontCamera) {
      CameraSelector.DEFAULT_FRONT_CAMERA
    } else {
      CameraSelector.DEFAULT_BACK_CAMERA
    }

    val config = PerformanceProfileManager.getConfig(currentPerformanceMode)

    // Build Preview
    val preview = Preview.Builder().build().also {
      previewView?.let { pView ->
        it.setSurfaceProvider(pView.surfaceProvider)
      }
    }

    // Build ImageAnalysis with latest frame strategy
    val imageAnalysis = ImageAnalysis.Builder()
      .setTargetResolution(Size(config.targetResolutionWidth, config.targetResolutionHeight))
      .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
      .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
      .build()

    frameAnalyzer = GestureFrameAnalyzer(
      isFrontCamera = isFrontCamera,
      performanceConfig = config,
      sensitivity = currentSensitivity,
      listener = analysisListener
    )

    imageAnalysis.setAnalyzer(cameraExecutor, frameAnalyzer!!)

    try {
      provider.bindToLifecycle(
        lifecycleOwner,
        cameraSelector,
        preview,
        imageAnalysis
      )
    } catch (e: Exception) {
      Log.e(TAG, "Use case binding failed", e)
    }
  }

  fun switchCamera() {
    isFrontCamera = !isFrontCamera
    if (isRunning) {
      bindCameraUseCases()
    }
  }

  fun updateSettings(mode: PerformanceMode, sensitivity: Float) {
    this.currentPerformanceMode = mode
    this.currentSensitivity = sensitivity
    val config = PerformanceProfileManager.getConfig(mode)
    frameAnalyzer?.updateConfig(config, sensitivity)
  }

  fun stopCamera() {
    isRunning = false
    try {
      cameraProvider?.unbindAll()
      frameAnalyzer?.reset()
    } catch (e: Exception) {
      Log.e(TAG, "Error stopping camera", e)
    }
  }

  fun release() {
    stopCamera()
    cameraExecutor.shutdown()
  }
}
