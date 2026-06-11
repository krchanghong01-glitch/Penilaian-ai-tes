package com.example.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.component.CropVisualList
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel

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

            // Crop details Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                        "ANALISIS CROP DETIL SOAL",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = TextPrimary,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Lists rendering of crop detail cards
            item {
                CropVisualList(soalList = cropResults)
            }
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
