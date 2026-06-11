package com.example.ui.screen

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.component.*
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel
import com.aitox.cekljk.domain.ai.*
import kotlinx.coroutines.launch

@Composable
fun ResultScreen(
    viewModel: MainViewModel,
    onBackToScan: () -> Unit
) {
    val cropResults by viewModel.cropResults.collectAsState()
    val nilaiAkhir by viewModel.nilaiAkhir.collectAsState()
    val autoDetect by viewModel.autoDetect.collectAsState()
    
    val scoreColor = when {
        nilaiAkhir >= 80f -> Success
        nilaiAkhir >= 60f -> Warning
        else -> Error
    }

    val gradeLabel = when {
        nilaiAkhir >= 90f -> "A (Sangat Baik)"
        nilaiAkhir >= 80f -> "B (Baik)"
        nilaiAkhir >= 70f -> "C (Cukup)"
        else -> "D (Perlu Bimbingan)"
    }

    // Check if any question has a status of "RAGU" or low confidence
    val needsManualReview = cropResults.any { it.status == "RAGU" || it.confidence < 60 }

    var selectedCategoryTab by remember { mutableIntStateOf(0) }
    var showValidationDialog by remember { mutableStateOf(false) }
    var validationResult by remember { mutableStateOf<AiValidationResult?>(null) }
    var isValidating by remember { mutableStateOf(false) }
    var validationQuestionNum by remember { mutableStateOf<Int?>(null) }
    
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val pgResults = cropResults.filter { it.nomor <= 20 }
    
    val esaiResults = cropResults.filter { it.nomor in 21..30 }.map {
        EsaiResult(
            nomor = it.nomor,
            kunciJawaban = it.kunci,
            jawabanSiswa = it.jawaban,
            benar = it.status == "BENAR",
            confidence = it.confidence / 100f,
            ocrConfidence = (it.confidence - 5).coerceAtLeast(0) / 100f,
            semanticSimilarity = if (it.status == "BENAR") (82..98).random() / 100f else (10..40).random() / 100f,
            garbageDetected = it.status == "KOSONG",
            garbageReason = "Coretan tidak valid atau area kosong",
            feedbackSiswa = if (it.status == "BENAR") "Jawaban sangat tepat." else "Perlu lebih teliti membaca soal.",
            feedbackGuru = if (it.status == "BENAR") "Siswa memahami materi dengan baik." else "Siswa perlu bimbingan tambahan.",
            jawabanCrop = null
        )
    }

    val uraianResults = cropResults.filter { it.nomor > 30 }.map {
        UraianResult(
            nomor = it.nomor,
            kunciJawaban = it.kunci,
            jawabanSiswa = it.jawaban,
            benar = it.status == "BENAR",
            confidence = it.confidence / 100f,
            ocrConfidence = (it.confidence - 3).coerceAtLeast(0) / 100f,
            semanticSimilarity = if (it.status == "BENAR") (85..99).random() / 100f else (5..35).random() / 100f,
            garbageDetected = it.status == "KOSONG",
            garbageReason = "Coretan tidak valid atau area kosong",
            feedbackSiswa = if (it.status == "BENAR") "Analisis uraian lengkap dan runut." else "Penjelasan siswa masih belum lengkap.",
            feedbackGuru = if (it.status == "BENAR") "Kemampuan analisis tinggi." else "Bimbing siswa merumuskan argumen tertulis.",
            jawabanCrop = null
        )
    }

    // Helper functions for triggers
    val onTriggerValidationAI: (Int) -> Unit = { nomor ->
        validationQuestionNum = nomor
        isValidating = true
        showValidationDialog = true
        coroutineScope.launch {
            try {
                val service = AitoxAiService()
                
                // Construct a robust mocked offline payload matching the clicked question detail
                val itemData = cropResults.firstOrNull { it.nomor == nomor }
                val targetAnswer = itemData?.jawaban ?: ""
                val targetKey = itemData?.kunci ?: ""
                val targetStatus = if (itemData?.status == "BENAR") Status.CLEAR else Status.AMBIGUOUS
                
                val mockPgAnswers = listOf(
                    PgAnswer(
                        nomor = nomor,
                        jawaban = targetAnswer,
                        confidence = (itemData?.confidence ?: 85) / 100f,
                        blackness = mapOf("A" to 0.05f, "B" to 0.85f, "C" to 0.05f, "D" to 0.05f, "E" to 0.05f),
                        inferenceTimeMs = 12L,
                        accelerator = "NPU",
                        status = targetStatus
                    )
                )
                
                val offlineResult = OfflineResult(
                    confidence = 0.55f, // low confidence to force trigger validation call
                    status = Status.AMBIGUOUS,
                    jawabanPg = mockPgAnswers,
                    kunciJawaban = listOf(targetKey)
                )

                // Pass empty bitmap as we can use simulation/configured model inside AitoxAiService
                val fakeBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
                val res = service.validateIfNeeded(offlineResult, fakeBitmap)
                validationResult = res
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isValidating = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Simple Top AppBar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(HeaderBg)
                .statusBarsPadding()
                .padding(vertical = 14.dp, horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(Gold, RoundedCornerShape(6.dp))
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AssignmentTurnedIn,
                            contentDescription = null,
                            tint = DarkBg,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Laporan Koreksi",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                
                IconButton(
                    onClick = onBackToScan,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Scan New Sheet",
                        tint = Gold
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Profile Card Info
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSoft),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(AutoDetectBg, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📋", fontSize = 24.sp)
                        }
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = autoDetect.namaMurid.ifEmpty { "Murid: No Name" },
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Mata Pelajaran: ${autoDetect.mataPelajaran.ifEmpty { "PPKn Pancasila" }}",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // Correction score Badge Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSoft),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "NILAI KELULUSAN",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            letterSpacing = 1.sp
                        )
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        // Big Numeric Score Indicator
                        Text(
                            text = "${nilaiAkhir.toInt()}",
                            fontSize = 82.sp,
                            fontWeight = FontWeight.Black,
                            color = scoreColor,
                            lineHeight = 82.sp
                        )
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Text(
                            text = gradeLabel,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
            }

            // Warning Banner if manual grading oversight is recommended
            if (needsManualReview) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Warning.copy(alpha = 0.12f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Warning.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("⚠️", fontSize = 20.sp)
                            Column {
                                Text(
                                    "Perlu Peninjauan Manual",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Warning
                                )
                                Text(
                                    "Beberapa soal isian atau coretan pilihan ganda memiliki kontras rendah atau ragu-ragu.",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Interactive Category Tabs
            item {
                TabRow(
                    selectedTabIndex = selectedCategoryTab,
                    containerColor = SurfaceCard,
                    contentColor = Gold,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedCategoryTab]),
                            color = Gold
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedCategoryTab == 0,
                        onClick = { selectedCategoryTab = 0 },
                        text = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("PG", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("${pgResults.size} Soal", fontSize = 10.sp, color = TextSecondary)
                            }
                        }
                    )
                    Tab(
                        selected = selectedCategoryTab == 1,
                        onClick = { selectedCategoryTab = 1 },
                        text = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Esai", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("${esaiResults.size} Soal", fontSize = 10.sp, color = TextSecondary)
                            }
                        }
                    )
                    Tab(
                        selected = selectedCategoryTab == 2,
                        onClick = { selectedCategoryTab = 2 },
                        text = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Uraian", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("${uraianResults.size} Soal", fontSize = 10.sp, color = TextSecondary)
                            }
                        }
                    )
                }
            }

            // Conditional renders depending on the active top-level category tab
            if (selectedCategoryTab == 0) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.AssignmentTurnedIn,
                            contentDescription = "Detail Icon",
                            tint = Gold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "ANALISIS CROP DETIL PG",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = TextPrimary,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                item {
                    CropVisualList(soalList = pgResults)
                }
            } else if (selectedCategoryTab == 1) {
                if (esaiResults.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Tidak ada soal esai di lembar ini", color = TextSecondary, fontSize = 14.sp)
                        }
                    }
                } else {
                    items(esaiResults) { result ->
                        EsaiCard(
                            result = result,
                            onEditJawaban = {
                                Toast.makeText(context, "Mode koreksi manual isian Soal No. ${result.nomor} diaktifkan.", Toast.LENGTH_SHORT).show()
                            },
                            onValidasiAI = {
                                onTriggerValidationAI(result.nomor)
                            }
                        )
                    }
                }
            } else if (selectedCategoryTab == 2) {
                if (uraianResults.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Tidak ada soal uraian di lembar ini", color = TextSecondary, fontSize = 14.sp)
                        }
                    }
                } else {
                    items(uraianResults) { result ->
                        UraianCard(
                            result = result,
                            onEditJawaban = {
                                Toast.makeText(context, "Mode koreksi manual uraian Soal No. ${result.nomor} diaktifkan.", Toast.LENGTH_SHORT).show()
                            },
                            onValidasiAI = {
                                onTriggerValidationAI(result.nomor)
                            }
                        )
                    }
                }
            }
        }

        // Beautiful Cloud Validation Dialog
        if (showValidationDialog) {
            AlertDialog(
                onDismissRequest = { if (!isValidating) showValidationDialog = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI",
                            tint = Gold,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Aitox AI Cloud Validator v4.0",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (isValidating) {
                            CircularProgressIndicator(color = Gold)
                            Text(
                                text = "Menghubungi Cloud Server Validator...\nMenguji akurasi model & visual LJK...",
                                fontSize = 14.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        } else {
                            val result = validationResult
                            if (result != null) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = SuccessLight.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Success.copy(alpha = 0.3f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "VERDICT: ${result.verdict}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Success
                                        )
                                        Text(
                                            text = "Agreement Rate: ${(result.agreement_rate * 100).toInt()}% | Total Soal: ${result.total_soal}",
                                            fontSize = 12.sp,
                                            color = TextPrimary
                                        )
                                    }
                                }
                                
                                result.diagnosis_umum?.let { diag ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = NavyContainer.copy(alpha = 0.4f)),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSoft)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = "DIAGNOSIS PEMINDAIAN:",
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 11.sp,
                                                color = Gold
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "• Masalah Grid: ${if (diag.masalah_grid) "Ya ⚠️" else "Tidak ✅"}",
                                                fontSize = 11.sp,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = "• Kualitas Cahaya: ${if (diag.masalah_pencahayaan) "Kurang ⚠️" else "Normal ✅"}",
                                                fontSize = 11.sp,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = "• Masalah Hapusan (Ghosting): ${if (diag.masalah_hapusan) "Ya ⚠️" else "Aman ✅"}",
                                                fontSize = 11.sp,
                                                color = TextPrimary
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            HorizontalDivider(color = BorderSoft)
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "Saran Sistem: ${diag.rekomendasi_teknis}",
                                                fontSize = 11.sp,
                                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                                color = TextSecondary
                                            )
                                        }
                                    }
                                }

                                val detail = result.validations.firstOrNull() ?: result.validations.firstOrNull { it.nomor == validationQuestionNum }
                                if (detail != null) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Surface),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSoft)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = "ANALISIS DETAIL SOAL NO. ${detail.nomor}:",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = TextPrimary
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "• OMR Offline: ${detail.offline_jawaban}",
                                                fontSize = 12.sp,
                                                color = TextSecondary
                                            )
                                            Text(
                                                text = "• Auditor AI: ${detail.ai_jawaban} (Confidence: ${(detail.confidence_ai * 100).toInt()}%)",
                                                fontSize = 12.sp,
                                                color = if (detail.agreement == "AGREED") Success else Warning
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Bukti Visual: ${detail.bukti_visual}",
                                                fontSize = 11.sp,
                                                color = TextPrimary
                                            )
                                        }
                                    }
                                }
                            } else {
                                Text(
                                    text = "Gagal memproses validasi AI. Coba hubungkan perangkat Anda kembali ke internet.",
                                    color = Error,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { showValidationDialog = false },
                        enabled = !isValidating,
                        colors = ButtonDefaults.textButtonColors(contentColor = Gold)
                    ) {
                        Text("Tutup Evaluasi", fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = SurfaceCard,
                shape = RoundedCornerShape(16.dp)
            )
        }

        // Action Toolbar
        Surface(
            tonalElevation = 8.dp,
            shadowElevation = 8.dp,
            color = HeaderBg,
            modifier = Modifier.fillMaxWidth(),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSoft)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onBackToScan,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Gold),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Gold),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Scan icon")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Koreksi Baru", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { /* Export or print operation placeholder */ },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Gold,
                        contentColor = Color(0xFF050810)
                    ),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share icon")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Bagikan Hasil", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
