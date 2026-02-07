
package com.example.wsinventario.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScannerActivity : ComponentActivity() {

    private lateinit var cameraExecutor: ExecutorService

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            setContent { ScannerView() }
        } else {
            Toast.makeText(this, "Permissão de câmera necessária", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()
        checkCameraPermission()
    }

    private fun checkCameraPermission() {
        when (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)) {
            PackageManager.PERMISSION_GRANTED -> setContent { ScannerView() }
            else -> requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    @Composable
    fun ScannerView() {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        val previewView = remember { PreviewView(context) }

        // O LaunchedEffect garante que a câmera só seja configurada quando a UI estiver pronta.
        LaunchedEffect(Unit) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                bindCameraUseCases(cameraProvider, previewView, lifecycleOwner, context)
            }, ContextCompat.getMainExecutor(context))
        }

        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView({ previewView }, modifier = Modifier.fillMaxSize())
        }
    }

    private fun bindCameraUseCases(
        cameraProvider: ProcessCameraProvider,
        previewView: PreviewView,
        lifecycleOwner: androidx.lifecycle.LifecycleOwner,
        context: Context
    ) {
        // Configuração da pré-visualização da câmera
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        // Configura o scanner do ML Kit para todos os formatos de código de barras
        val scanner = BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                .build()
        )

        // Configura a análise de imagem frame a frame
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor) { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                        scanner.process(image)
                            .addOnSuccessListener { barcodes ->
                                if (barcodes.isNotEmpty()) {
                                    barcodes.first().rawValue?.let { barcode ->
                                        // Para a câmera para evitar múltiplas detecções
                                        cameraExecutor.shutdown()
                                        // Retorna o resultado para a MainActivity
                                        val resultIntent = Intent().apply {
                                            putExtra("BARCODE_RESULT", barcode)
                                        }
                                        setResult(Activity.RESULT_OK, resultIntent)
                                        finish()
                                    }
                                }
                            }
                            .addOnCompleteListener { 
                                imageProxy.close()
                            }
                    }
                }
            }

        // Conecta tudo ao ciclo de vida da câmera
        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageAnalysis
            )
        } catch (exc: Exception) {
            Log.e("ScannerActivity", "Falha ao iniciar a câmera", exc)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!cameraExecutor.isShutdown) {
            cameraExecutor.shutdown()
        }
    }
}
