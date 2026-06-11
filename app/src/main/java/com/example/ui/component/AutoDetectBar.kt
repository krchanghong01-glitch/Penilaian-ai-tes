package com.example.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel

@Composable
fun AutoDetectBar(viewModel: MainViewModel) {
    val autoDetect by viewModel.autoDetect.collectAsState()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AutoDetectBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = "Auto Detect Icon",
                    tint = Gold,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "AUTO DETECT ANALYSIS",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = TextPrimary,
                    letterSpacing = 0.5.sp
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DetectItem(icon = "👤", label = "NAMA MURID", value = autoDetect.namaMurid.ifEmpty { "BELUM TERDETEKSI" }, isPrimary = true, modifier = Modifier.weight(1.2f))
                DetectItem(icon = "📚", label = "MAPEL", value = autoDetect.mataPelajaran.ifEmpty { "OTOMATIS" }, modifier = Modifier.weight(1f))
                DetectItem(icon = "📝", label = "PG", value = "${autoDetect.jumlahPG} PG", modifier = Modifier.weight(0.9f))
                DetectItem(icon = "✍️", label = "ESAI", value = "${autoDetect.jumlahEsai} ESAI", modifier = Modifier.weight(0.9f))
            }
        }
    }
}

@Composable
fun RowScope.DetectItem(
    icon: String,
    label: String,
    value: String,
    isPrimary: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .background(HeaderBg, RoundedCornerShape(12.dp))
            .border(1.dp, BorderSoft, RoundedCornerShape(12.dp))
            .padding(vertical = 10.dp, horizontal = 4.dp)
    ) {
        Text(icon, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            value.uppercase(),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = if (isPrimary) Gold else TextPrimary,
            maxLines = 1,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
