package com.gridibuild.sfobud.ui.screens.rooms

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.gridibuild.sfobud.ui.components.*
import com.gridibuild.sfobud.ui.navigation.Screen
import com.gridibuild.sfobud.ui.theme.*
import com.gridibuild.sfobud.viewmodel.RoomsViewModel

@Composable
fun RoomDetailScreen(roomId: Long, navController: NavController) {
    val roomsVm: RoomsViewModel = viewModel()

    LaunchedEffect(roomId) { roomsVm.selectRoom(roomId) }

    val room by roomsVm.selectedRoom.collectAsState()

    if (room == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Orange)
        }
        return
    }

    val roomColor = parseColor(room!!.colorHex)

    Scaffold(
        topBar = {
            AppTopBar(
                title = room!!.name,
                onBack = { navController.popBackStack() },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Measurements.route) }) {
                        Icon(Icons.Filled.Straighten, null, tint = Orange)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .background(Brush.linearGradient(listOf(roomColor.copy(alpha = 0.8f), roomColor)))
                        .padding(24.dp)
                ) {
                    Column {
                        if (room!!.type.isNotBlank()) Text(room!!.type, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                        Spacer(Modifier.height(8.dp))
                        StageChip(room!!.stage)
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            if (room!!.width > 0) DimensionInfo("Width", "${room!!.width}m")
                            if (room!!.length > 0) DimensionInfo("Length", "${room!!.length}m")
                            if (room!!.ceilingHeight > 0) DimensionInfo("Ceiling", "${room!!.ceilingHeight}m")
                        }
                        if (room!!.width > 0 && room!!.length > 0) {
                            Spacer(Modifier.height(8.dp))
                            Text("Area: ${String.format("%.1f", room!!.width * room!!.length)} m²", color = Color.White, fontSize = 13.sp)
                        }
                    }
                }
            }

            item {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Quick Navigation", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        listOf(
                            Triple("Tasks", Icons.Filled.CheckCircle, Screen.Tasks.route),
                            Triple("Materials", Icons.Filled.Construction, Screen.Materials.route),
                            Triple("Photos", Icons.Filled.PhotoCamera, Screen.Photos.route),
                            Triple("Measurements", Icons.Filled.Straighten, Screen.Measurements.route)
                        ).forEach { (label, icon, route) ->
                            Card(
                                onClick = { navController.navigate(route) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = roomColor.copy(alpha = 0.1f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(icon, null, tint = roomColor, modifier = Modifier.size(22.dp))
                                    Spacer(Modifier.height(4.dp))
                                    Text(label, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }

            if (room!!.notes.isNotBlank()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SoftSectionBg)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Filled.Notes, null, tint = Orange, modifier = Modifier.size(18.dp))
                                Text("Notes", fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(room!!.notes, fontSize = 14.sp, color = OnSurfaceVariant)
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun DimensionInfo(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(label, color = Color.White.copy(alpha = 0.75f), fontSize = 11.sp)
    }
}
