package com.example.ui.component

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.*

// ==========================================
// DATA CLASSES
// ==========================================
data class EsaiResult(
    val nomor: Int,
    val kunciJawaban: String,
    val jawabanSiswa: String,
    val benar: Boolean,
    val confidence: Float,
    val ocrConfidence: Float,
    val semanticSimilarity: Float,
    val garbageDetected: Boolean,
    val garbageReason: String,
    val feedbackSiswa: String,
    val feedbackGuru: String,
    val jawabanCrop: Bitmap? = null
)

data class UraianResult(
    val nomor: Int,
    val kunciJawaban: String,
    val jawabanSiswa: String,
    val benar: Boolean,
    val confidence: Float,
    val ocrConfidence: Float,
    val semanticSimilarity: Float,
    val garbageDetected: Boolean,
    val garbageReason: String,
    val feedbackSiswa: String,
    val feedbackGuru: String,
    val jawabanCrop: Bitmap? = null
)

// ==========================================
// ESAI TAB
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EsaiTab(
    esaiResults: List<EsaiResult>,
    modifier: Modifier = Modifier,
    onEditJawaban: ((Int) -> Unit)? = null,
    onValidasiAI: ((Int) -> Unit)? = null
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Yang Benar", "Yang Perlu Review")
    
    // Filter berdasarkan tab
    val filteredResults = when (selectedTabIndex) {
        0 -> esaiResults.filter { it.benar && !it.garbageDetected }
        1 -> esaiResults.filter { !it.benar || it.garbageDetected || it.confidence < 0.7f }
        else -> esaiResults
    }
    
    Column(modifier = modifier) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Edit,
                contentDescription = null,
                tint = Gold,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Soal Esai",
                fontSize = 18.sp,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            // Badge count
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = NavyContainer
            ) {
                Text(
                    text = "${esaiResults.size} soal",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    color = Gold
                )
            }
        }
        
        // Tabs: "Yang Benar" | "Yang Perlu Review"
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Surface,
            contentColor = Gold,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = Gold
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (index == 0) Icons.Default.CheckCircle 
                                else Icons.Default.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (selectedTabIndex == index) Gold else TextSecondary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                title,
                                color = if (selectedTabIndex == index) TextPrimary else TextSecondary,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                )
            }
        }
        
        // Daftar soal esai
        if (filteredResults.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Tidak ada soal di tab ini",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredResults) { result ->
                    EsaiCard(
                        result = result,
                        onEditJawaban = { onEditJawaban?.invoke(result.nomor) },
                        onValidasiAI = { onValidasiAI?.invoke(result.nomor) }
                    )
                }
            }
        }
    }
}

// ==========================================
// ESAI CARD
// ==========================================
@Composable
fun EsaiCard(
    result: EsaiResult,
    onEditJawaban: (() -> Unit)? = null,
    onValidasiAI: (() -> Unit)? = null
) {
    val statusColor = when {
        result.garbageDetected -> Error
        result.benar && result.confidence > 0.85f -> Success
        result.benar -> Success
        !result.benar -> Error
        else -> Warning
    }
    
    val statusIcon = when {
        result.garbageDetected -> "🗑️"
        result.benar && result.confidence > 0.85f -> "✅"
        result.benar -> "✔️"
        result.confidence < 0.6f -> "⚠️"
        else -> "❌"
    }
    
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header soal
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Nomor & Status
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "$statusIcon Isian No. ${result.nomor}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                
                Spacer(Modifier.weight(1f))
                
                // Confidence badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = when {
                        result.confidence > 0.85f -> Success.copy(alpha = 0.15f)
                        result.confidence > 0.6f -> Warning.copy(alpha = 0.15f)
                        else -> Error.copy(alpha = 0.15f)
                    }
                ) {
                    Text(
                        text = "Conf: ${(result.confidence * 100).toInt()}%",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = when {
                            result.confidence > 0.85f -> Success
                            result.confidence > 0.6f -> Warning
                            else -> Error
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Divider
            HorizontalDivider(color = SurfaceVariant)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Kunci & Jawaban
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Kunci
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Key,
                        null,
                        tint = Gold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Kunci:", fontSize = 13.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        result.kunciJawaban,
                        fontSize = 13.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Jawaban
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Default.Person,
                        null,
                        tint = if (result.benar) Success else Error,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Jawaban:", fontSize = 13.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        result.jawabanSiswa,
                        fontSize = 13.sp,
                        color = if (result.benar) Success else Error,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 5,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Garbage Detection atau Quality Visual
            if (result.garbageDetected) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = ErrorLight
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            null,
                            tint = Error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "GARBAGE DETECTED",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Error
                            )
                            Text(
                                result.garbageReason,
                                fontSize = 11.sp,
                                color = TextPrimary
                            )
                        }
                    }
                }
            } else {
                // Quality indicators
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Background)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        QualityRow(
                            label = "OCR Tulisan Tangan",
                            value = result.ocrConfidence,
                            color = if (result.ocrConfidence > 0.7f) Success else Warning
                        )
                        QualityRow(
                            label = "Kecocokan Semantik",
                            value = result.semanticSimilarity,
                            color = if (result.semanticSimilarity > 0.8f) Success else Warning
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Crop Image (jika ada)
            result.jawabanCrop?.let { bitmap ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.1f))
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = bitmap,
                            contentDescription = "Crop jawaban",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                        // Label crop
                        Surface(
                            modifier = Modifier.align(Alignment.TopStart),
                            shape = RoundedCornerShape(bottomEnd = 8.dp),
                            color = DarkBg.copy(alpha = 0.8f)
                        ) {
                            Text(
                                "📷 Crop Area Jawaban",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 10.sp,
                                color = TextPrimary
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // Feedback
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Feedback Siswa
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = SuccessLight.copy(alpha = 0.3f)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("💬", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            result.feedbackSiswa,
                            fontSize = 12.sp,
                            color = TextPrimary,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                // Feedback Guru
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = NavyContainer.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("👨‍🏫", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            result.feedbackGuru,
                            fontSize = 12.sp,
                            color = TextPrimary,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Edit Button
                OutlinedButton(
                    onClick = { onEditJawaban?.invoke() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Gold
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Gold)
                ) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit", fontSize = 13.sp)
                }
                
                // Validasi AI Button
                Button(
                    onClick = { onValidasiAI?.invoke() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Gold,
                        contentColor = Color(0xFF050810)
                    )
                ) {
                    Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Validasi AI", fontSize = 13.sp)
                }
            }
        }
    }
}

// ==========================================
// URAIAN TAB
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UraianTab(
    uraianResults: List<UraianResult>,
    modifier: Modifier = Modifier,
    onEditJawaban: ((Int) -> Unit)? = null,
    onValidasiAI: ((Int) -> Unit)? = null
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Yang Benar", "Yang Perlu Review")
    
    val filteredResults = when (selectedTabIndex) {
        0 -> uraianResults.filter { it.benar && !it.garbageDetected }
        1 -> uraianResults.filter { !it.benar || it.garbageDetected || it.confidence < 0.7f }
        else -> uraianResults
    }
    
    Column(modifier = modifier) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Assignment, // Use core Assignment icon instead of Article to avoid dependency issues
                contentDescription = null,
                tint = Gold,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Soal Uraian",
                fontSize = 18.sp,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = GoldContainer
            ) {
                Text(
                    text = "${uraianResults.size} soal",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    color = Gold
                )
            }
        }
        
        // Tabs
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Surface,
            contentColor = Gold,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = Gold
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (index == 0) Icons.Default.CheckCircle 
                                else Icons.Default.Warning,
                                null,
                                modifier = Modifier.size(16.dp),
                                tint = if (selectedTabIndex == index) Gold else TextSecondary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                title,
                                color = if (selectedTabIndex == index) TextPrimary else TextSecondary,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                )
            }
        }
        
        // Daftar soal uraian
        if (filteredResults.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Tidak ada soal di tab ini", color = TextSecondary, fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredResults) { result ->
                    UraianCard(
                        result = result,
                        onEditJawaban = { onEditJawaban?.invoke(result.nomor) },
                        onValidasiAI = { onValidasiAI?.invoke(result.nomor) }
                    )
                }
            }
        }
    }
}

// ==========================================
// URAIAN CARD
// ==========================================
@Composable
fun UraianCard(
    result: UraianResult,
    onEditJawaban: (() -> Unit)? = null,
    onValidasiAI: (() -> Unit)? = null
) {
    val statusColor = when {
        result.garbageDetected -> Error
        result.benar && result.confidence > 0.85f -> Success
        result.benar -> Success
        !result.benar -> Error
        else -> Warning
    }
    
    val statusIcon = when {
        result.garbageDetected -> "🗑️"
        result.benar && result.confidence > 0.85f -> "✅"
        result.benar -> "✔️"
        result.confidence < 0.6f -> "⚠️"
        else -> "❌"
    }
    
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "$statusIcon Uraian No. ${result.nomor}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                
                Spacer(Modifier.weight(1f))
                
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = when {
                        result.confidence > 0.85f -> Success.copy(alpha = 0.15f)
                        result.confidence > 0.6f -> Warning.copy(alpha = 0.15f)
                        else -> Error.copy(alpha = 0.15f)
                    }
                ) {
                    Text(
                        text = "Conf: ${(result.confidence * 100).toInt()}%",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = when {
                            result.confidence > 0.85f -> Success
                            result.confidence > 0.6f -> Warning
                            else -> Error
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = SurfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))
            
            // Kunci & Jawaban
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Key, null, tint = Gold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Kunci:", fontSize = 13.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(result.kunciJawaban, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                }
                
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Person, null, tint = if (result.benar) Success else Error, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Jawaban:", fontSize = 13.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(result.jawabanSiswa, fontSize = 13.sp, color = if (result.benar) Success else Error, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            // Garbage Detection atau Quality Visual
            if (result.garbageDetected) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = ErrorLight
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            null,
                            tint = Error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "GARBAGE DETECTED",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Error
                            )
                            Text(
                                result.garbageReason,
                                fontSize = 11.sp,
                                color = TextPrimary
                            )
                        }
                    }
                }
            } else {
                // Quality indicators
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Background)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        QualityRow(
                            label = "OCR Tulisan Tangan",
                            value = result.ocrConfidence,
                            color = if (result.ocrConfidence > 0.7f) Success else Warning
                        )
                        QualityRow(
                            label = "Kecocokan Semantik",
                            value = result.semanticSimilarity,
                            color = if (result.semanticSimilarity > 0.8f) Success else Warning
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Crop Image (jika ada)
            result.jawabanCrop?.let { bitmap ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.1f))
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = bitmap,
                            contentDescription = "Crop jawaban",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                        // Label crop
                        Surface(
                            modifier = Modifier.align(Alignment.TopStart),
                            shape = RoundedCornerShape(bottomEnd = 8.dp),
                            color = DarkBg.copy(alpha = 0.8f)
                        ) {
                            Text(
                                "📷 Crop Area Jawaban",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 10.sp,
                                color = TextPrimary
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // Feedback
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Feedback Siswa
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = SuccessLight.copy(alpha = 0.3f)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("💬", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            result.feedbackSiswa,
                            fontSize = 12.sp,
                            color = TextPrimary,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                // Feedback Guru
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = NavyContainer.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("👨‍🏫", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            result.feedbackGuru,
                            fontSize = 12.sp,
                            color = TextPrimary,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Edit Button
                OutlinedButton(
                    onClick = { onEditJawaban?.invoke() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Gold
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Gold)
                ) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit", fontSize = 13.sp)
                }
                
                // Validasi AI Button
                Button(
                    onClick = { onValidasiAI?.invoke() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Gold,
                        contentColor = Color(0xFF050810)
                    )
                ) {
                    Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Validasi AI", fontSize = 13.sp)
                }
            }
        }
    }
}

// ==========================================
// QUALITY ROW COMPONENT
// ==========================================
@Composable
fun QualityRow(
    label: String,
    value: Float,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            fontSize = 12.sp,
            color = TextSecondary,
            modifier = Modifier.weight(1f)
        )
        
        // Progress bar
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color.LightGray.copy(alpha = 0.1f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(value)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            "${(value * 100).toInt()}%",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}
