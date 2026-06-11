package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraEnhance
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.component.AutoDetectBar
import com.example.ui.component.SplitPanel
import com.example.ui.screen.ResultScreen
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val viewModel: MainViewModel = viewModel()
        CekSoalApp(viewModel)
      }
    }
  }
}

enum class ActiveScreen {
  SCAN,
  RESULT
}

@Composable
fun CekSoalApp(viewModel: MainViewModel) {
  var activeScreen by remember { mutableStateOf(ActiveScreen.SCAN) }
  val isProcessing by viewModel.isProcessing.collectAsState()
  val isSimulation by viewModel.isSimulation.collectAsState()
  val errorMessage by viewModel.errorMessage.collectAsState()
  val muridBitmap by viewModel.muridBitmap.collectAsState()
  val cropResults by viewModel.cropResults.collectAsState()

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(DarkBg)
  ) {
    when (activeScreen) {
      ActiveScreen.SCAN -> {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header Top Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HeaderBg)
                    .padding(vertical = 16.dp, horizontal = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Custom Adaptive Mini Icon matching UI HTML
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(Gold, RoundedCornerShape(6.dp))
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = DarkBg,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ATOX ",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "CEK SOAL",
                                color = Gold,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Sistem Koreksi Lembar Jawaban Ujian",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Light
                        )
                    }
                    
                    if (isSimulation) {
                        Surface(
                            color = Gold.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Gold)
                        ) {
                            Text(
                                "MOCK MODE",
                                color = Gold,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Auto Detect Bar displaying profile context
                AutoDetectBar(viewModel = viewModel)

                // Description Instruction Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSoft)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("💡", fontSize = 24.sp)
                        Column {
                            Text(
                                "Cara Koreksi Cepat:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = TextPrimary
                            )
                            Text(
                                "Foto kunci jawaban guru (kiri) & lembar jawaban murid (kanan). Klik Proses Koreksi untuk membandingkan otomatis.",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }

                // Split Panel
                SplitPanel(viewModel = viewModel)
                
                // Show API warning message if any
                errorMessage?.let { msg ->
                    Surface(
                        color = Color(0x33EF4444),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66EF4444)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = msg,
                            color = TextPrimary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }

            // Floating footer correction action trigger
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                color = HeaderBg,
                modifier = Modifier.fillMaxWidth(),
                border = androidx.compose.foundation.BorderStroke(width = 1.dp, color = BorderSoft)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.prosesKoreksi()
                        },
                        enabled = muridBitmap != null && !isProcessing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Gold,
                            contentColor = Color(0xFF050810),
                            disabledContainerColor = Color(0x1BFFFFFF),
                            disabledContentColor = Color(0x44FFFFFF)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CameraEnhance,
                                contentDescription = "Process grading icon"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "PROSES KOREKSI",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }
        
        // Progress overlay indicator during AI detection
        if (isProcessing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkBg.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSoft),
                    modifier = Modifier.width(280.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = Gold,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Mempersiapkan Analisis AI...",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Mengekstrak lembar jawaban, mengevaluasi saringan bubble & tulisan tangan secara real-time.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }
        
        // Listen for successful correction generation and forward automatically to ResultScreen
        LaunchedEffect(cropResults) {
            if (cropResults.isNotEmpty() && !isProcessing) {
                activeScreen = ActiveScreen.RESULT
            }
        }
      }

      ActiveScreen.RESULT -> {
        ResultScreen(
            viewModel = viewModel,
            onBackToScan = {
                // Clear state list back to scan screen to start afresh
                viewModel.resetKoreksi()
                activeScreen = ActiveScreen.SCAN
            }
        )
      }
    }
  }
}
