package com.example.ui.component

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.ui.theme.*
import java.io.InputStream

@Composable
fun CameraCaptureCard(
    modifier: Modifier = Modifier,
    label: String,
    warnaPanel: Color,
    warnaBorder: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    capturedBitmap: Bitmap?,
    onCapture: (Bitmap) -> Unit,
    onFilePick: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    var showCameraDialog by remember { mutableStateOf(false) }
    var showFilePicker by remember { mutableStateOf(false) }
    
    // Permission launcher for Camera
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showCameraDialog = true
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = warnaPanel),
        border = androidx.compose.foundation.BorderStroke(2.dp, warnaBorder.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = "$label Icon",
                    tint = warnaBorder,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    label,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = warnaBorder
                )
            }
            
            // Preview Workspace
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF070B14))
                    .border(1.dp, warnaBorder.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (capturedBitmap != null) {
                    Image(
                        bitmap = capturedBitmap.asImageBitmap(),
                        contentDescription = "Captured sheet preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = "Image Placeholder Icon",
                            modifier = Modifier.size(36.dp),
                            tint = Color.White.copy(alpha = 0.2f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Belum ada foto",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Kamera Trigger Button
                Button(
                    onClick = {
                        val hasCamPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                        
                        if (hasCamPermission) {
                            showCameraDialog = true
                        } else {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = warnaBorder,
                        contentColor = if (warnaBorder == Gold) Color(0xFF050810) else Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "Launch CameraIcon",
                        modifier = Modifier.size(14.dp),
                        tint = if (warnaBorder == Gold) Color(0xFF050810) else Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Kamera", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                
                // File Picker Trigger Button
                OutlinedButton(
                    onClick = { showFilePicker = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(warnaBorder)
                    ),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Folder, contentDescription = "Pick FileIcon", modifier = Modifier.size(14.dp), tint = warnaBorder)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pilih File", fontSize = 11.sp, color = warnaBorder, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
    
    // Custom camera execution view dialog
    if (showCameraDialog) {
        CameraDialog(
            onCapture = { bitmap ->
                onCapture(bitmap)
                showCameraDialog = false
            },
            onDismiss = { showCameraDialog = false }
        )
    }
    
    // File Picker dialog contract launcher
    if (showFilePicker) {
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                try {
                    val inputStream: InputStream? = context.contentResolver.openInputStream(it)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()
                    if (bitmap != null) {
                        onFilePick(bitmap)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            showFilePicker = false
        }
        
        LaunchedEffect(Unit) {
            launcher.launch("image/*")
        }
    }
}

@Composable
fun CameraDialog(
    onCapture: (Bitmap) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.Black
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Live Viewfinder viewport
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            
                            val capture = ImageCapture.Builder()
                                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                .build()
                            
                            imageCapture = capture
                            
                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                            
                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    capture
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                        
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )
                
                // Overlay text tip
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 48.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Posisikan lembar jawaban di dalam bingkai",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Close Button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close ViewfinderDialog",
                        tint = Color.White
                    )
                }

                // Capture Button
                FloatingActionButton(
                    onClick = {
                        val capture = imageCapture
                        if (capture != null) {
                            capture.takePicture(
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageCapturedCallback() {
                                    override fun onCaptureSuccess(imageProxy: ImageProxy) {
                                        try {
                                            val buffer = imageProxy.planes[0].buffer
                                            val bytes = ByteArray(buffer.remaining())
                                            buffer.get(bytes)
                                            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                            
                                            val rotationDeg = imageProxy.imageInfo.rotationDegrees
                                            val correctedBitmap = if (rotationDeg != 0) {
                                                val mat = android.graphics.Matrix().apply { postRotate(rotationDeg.toFloat()) }
                                                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, mat, true)
                                            } else {
                                                bitmap
                                            }
                                            onCapture(correctedBitmap)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            // Fallback
                                            val blank = Bitmap.createBitmap(600, 800, Bitmap.Config.ARGB_8888)
                                            onCapture(blank)
                                        } finally {
                                            imageProxy.close()
                                        }
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        exception.printStackTrace()
                                        // Dynamic Fallback on emulation environment
                                        val blank = Bitmap.createBitmap(600, 800, Bitmap.Config.ARGB_8888)
                                        onCapture(blank)
                                    }
                                }
                            )
                        } else {
                            // Instant Fallback if Camera driver failed
                            val blank = Bitmap.createBitmap(600, 800, Bitmap.Config.ARGB_8888)
                            onCapture(blank)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 36.dp)
                        .size(68.dp),
                    shape = CircleShape,
                    containerColor = Navy
                ) {
                    Icon(
                        Icons.Default.Camera,
                        contentDescription = "Photo Click Button",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}
