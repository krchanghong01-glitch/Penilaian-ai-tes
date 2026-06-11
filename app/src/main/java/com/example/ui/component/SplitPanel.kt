package com.example.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel

@Composable
fun SplitPanel(viewModel: MainViewModel) {
    val guruBitmap by viewModel.guruBitmap.collectAsState()
    val muridBitmap by viewModel.muridBitmap.collectAsState()
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Panel Kiri: Lembar Guru
        CameraCaptureCard(
            modifier = Modifier.weight(1f),
            label = "LEMBAR GURU",
            warnaPanel = GuruPanel,
            warnaBorder = Color(0xFF3F51B5),
            icon = Icons.Default.School,
            capturedBitmap = guruBitmap,
            onCapture = { bitmap -> viewModel.setGuruBitmap(bitmap) },
            onFilePick = { bitmap -> viewModel.setGuruBitmap(bitmap) }
        )
        
        // Panel Kanan: Lembar Murid
        CameraCaptureCard(
            modifier = Modifier.weight(1f),
            label = "LEMBAR MURID",
            warnaPanel = MuridPanel,
            warnaBorder = Gold,
            icon = Icons.Default.Person,
            capturedBitmap = muridBitmap,
            onCapture = { bitmap -> viewModel.setMuridBitmap(bitmap) },
            onFilePick = { bitmap -> viewModel.setMuridBitmap(bitmap) }
        )
    }
}
