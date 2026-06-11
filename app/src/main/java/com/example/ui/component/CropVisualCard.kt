package com.example.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.CropSoalData

@Composable
fun CropVisualCard(soal: CropSoalData) {
    val statusColor = when (soal.status) {
        "BENAR" -> Success
        "SALAH" -> Error
        "RAGU" -> Warning
        "KOSONG" -> Color.Gray
        else -> Color.Gray
    }
    
    val statusIcon = when (soal.status) {
        "BENAR" -> "✅"
        "SALAH" -> "❌"
        "RAGU" -> "⚠️"
        "KOSONG" -> "⭕"
        else -> "❓"
    }

    val isEssay = soal.nomor >= 25
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = statusColor.copy(alpha = 0.05f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            statusColor.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isEssay) "Isian No. ${soal.nomor}" else "Soal ${soal.nomor}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Navy
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        statusIcon,
                        fontSize = 16.sp
                    )
                }
                
                Text(
                    "Confidence: ${soal.confidence}%",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = statusColor
                )
            }
            
            // Status Info
            Row(
                modifier = Modifier.padding(vertical = 6.dp)
            ) {
                Text("Kunci: ", fontSize = 13.sp, color = TextSecondary)
                Text(soal.kunci, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Navy)
                Spacer(modifier = Modifier.width(20.dp))
                Text("Jawaban: ", fontSize = 13.sp, color = TextSecondary)
                Text(soal.jawaban.ifEmpty { "[KOSONG]" }, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = statusColor)
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            if (!isEssay) {
                // Bubble Visualization
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    listOf("A", "B", "C", "D", "E").forEach { option ->
                        val percentage = soal.bubbleScores[option] ?: 0
                        BubbleVisual(
                            label = option,
                            percentage = percentage,
                            isFilled = soal.jawaban == option,
                            isKunci = soal.kunci == option
                        )
                    }
                }
            } else {
                // Text representation for Handwriting match
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(NavyContainer.copy(alpha = 0.3f))
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "OCR Tulisan Tangan",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                        Text(
                            text = "Kecocokan Semantik: ${soal.confidence}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BubbleVisual(
    label: String,
    percentage: Int,
    isFilled: Boolean,
    isKunci: Boolean
) {
    val bubbleColor = when {
        isFilled && isKunci -> Success
        isFilled && !isKunci -> Error
        !isFilled && isKunci -> Navy.copy(alpha = 0.4f)
        else -> Color.LightGray
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(52.dp)
    ) {
        // Bubble circle
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(if (isFilled) bubbleColor.copy(alpha = 0.15f) else Color.Transparent)
                .border(1.5.dp, bubbleColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isFilled) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(bubbleColor)
                )
            } else if (isKunci) {
                // Light dashed/dotted marker for teacher key reference
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(bubbleColor.copy(alpha = 0.5f))
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Label
        Text(
            label,
            fontSize = 11.sp,
            fontWeight = if (isFilled || isKunci) FontWeight.Bold else FontWeight.Normal,
            color = if (isFilled) bubbleColor else if (isKunci) Navy else TextSecondary
        )
        
        Spacer(modifier = Modifier.height(2.dp))

        // Percentage bar
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.LightGray.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percentage / 100f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(bubbleColor)
            )
        }
        
        Spacer(modifier = Modifier.height(2.dp))

        Text(
            "$percentage%",
            fontSize = 9.sp,
            color = TextSecondary,
            fontWeight = if (isFilled) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun CropVisualList(soalList: List<CropSoalData>) {
    soalList.forEach { soal ->
        CropVisualCard(soal = soal)
    }
}
