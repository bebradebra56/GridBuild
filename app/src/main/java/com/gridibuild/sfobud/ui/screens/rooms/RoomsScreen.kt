package com.gridibuild.sfobud.ui.screens.rooms

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.gridibuild.sfobud.data.local.entity.RoomEntity
import com.gridibuild.sfobud.ui.components.*
import com.gridibuild.sfobud.ui.navigation.Screen
import com.gridibuild.sfobud.ui.theme.*
import com.gridibuild.sfobud.viewmodel.RoomsViewModel

@Composable
fun RoomsScreen(navController: NavController) {
    val vm: RoomsViewModel = viewModel()
    val rooms by vm.rooms.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editRoom by remember { mutableStateOf<RoomEntity?>(null) }

    Scaffold(
        topBar = { AppTopBar("Rooms", onBack = { navController.popBackStack() }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }, containerColor = Orange, contentColor = Color.White) {
                Icon(Icons.Filled.Add, "Add Room")
            }
        }
    ) { padding ->
        if (rooms.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState(icon = Icons.Filled.MeetingRoom, title = "No rooms yet", subtitle = "Add rooms to your project", actionLabel = "Add Room", onAction = { showDialog = true })
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(rooms, key = { it.id }) { room ->
                    RoomCard(
                        room = room,
                        onClick = { navController.navigate(Screen.RoomDetail.createRoute(room.id)) },
                        onEdit = { editRoom = room },
                        onDelete = { vm.deleteRoom(room) },
                        onStageChange = { stage -> vm.updateRoomStage(room, stage) }
                    )
                }
            }
        }
    }

    if (showDialog || editRoom != null) {
        RoomDialog(
            room = editRoom,
            colors = vm.roomColors,
            stages = vm.roomStages,
            onDismiss = { showDialog = false; editRoom = null },
            onSave = { name, type, w, h, l, ch, color ->
                if (editRoom != null) vm.updateRoom(editRoom!!.copy(name = name, type = type, width = w, height = h, length = l, ceilingHeight = ch, colorHex = color))
                else vm.createRoom(name, type, w, h, l, ch, color)
                showDialog = false; editRoom = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomCard(
    room: RoomEntity,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onStageChange: (String) -> Unit
) {
    val roomColor = parseColor(room.colorHex)
    var showMenu by remember { mutableStateOf(false) }
    var showStageMenu by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                modifier = Modifier.size(52.dp).clip(RoundedCornerShape(14.dp)).background(roomColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.MeetingRoom, null, tint = roomColor, modifier = Modifier.size(28.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(room.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                if (room.type.isNotBlank()) Text(room.type, fontSize = 12.sp, color = OnSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                if (room.width > 0 || room.length > 0) {
                    Text("${room.width}m × ${room.length}m" + if (room.ceilingHeight > 0) " × ${room.ceilingHeight}m H" else "", fontSize = 12.sp, color = OnSurfaceVariant)
                }
                Spacer(Modifier.height(8.dp))
                Box {
                    StageChip(room.stage, modifier = Modifier.clickable { showStageMenu = true })
                    DropdownMenu(expanded = showStageMenu, onDismissRequest = { showStageMenu = false }) {
                        listOf("Planning", "Preparing", "In Progress", "Finishing", "Done").forEach { stage ->
                            DropdownMenuItem(text = { Text(stage) }, onClick = { onStageChange(stage); showStageMenu = false })
                        }
                    }
                }
            }
            Box {
                IconButton(onClick = { showMenu = true }) { Icon(Icons.Filled.MoreVert, null) }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(text = { Text("Edit") }, onClick = { onEdit(); showMenu = false }, leadingIcon = { Icon(Icons.Filled.Edit, null) })
                    DropdownMenuItem(text = { Text("Delete", color = WarmRed) }, onClick = { onDelete(); showMenu = false }, leadingIcon = { Icon(Icons.Filled.Delete, null, tint = WarmRed) })
                }
            }
        }
    }
}

@Composable
fun RoomDialog(
    room: RoomEntity?,
    colors: List<String>,
    stages: List<String>,
    onDismiss: () -> Unit,
    onSave: (String, String, Double, Double, Double, Double, String) -> Unit
) {
    var name by remember { mutableStateOf(room?.name ?: "") }
    var type by remember { mutableStateOf(room?.type ?: "") }
    var width by remember { mutableStateOf(room?.width?.toString() ?: "") }
    var height by remember { mutableStateOf(room?.height?.toString() ?: "") }
    var length by remember { mutableStateOf(room?.length?.toString() ?: "") }
    var ceilingHeight by remember { mutableStateOf(room?.ceilingHeight?.toString() ?: "") }
    var selectedColor by remember { mutableStateOf(room?.colorHex ?: colors.first()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(24.dp).verticalScroll(androidx.compose.foundation.rememberScrollState())) {
                Text(if (room == null) "New Room" else "Edit Room", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(20.dp))
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Room Name *") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text("Type (Kitchen, Bedroom...)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)
                Spacer(Modifier.height(16.dp))
                Text("Dimensions (meters)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = width, onValueChange = { width = it }, label = { Text("Width") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), singleLine = true)
                    OutlinedTextField(value = length, onValueChange = { length = it }, label = { Text("Length") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), singleLine = true)
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = height, onValueChange = { height = it }, label = { Text("Height") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), singleLine = true)
                    OutlinedTextField(value = ceilingHeight, onValueChange = { ceilingHeight = it }, label = { Text("Ceiling H") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), singleLine = true)
                }
                Spacer(Modifier.height(16.dp))
                Text("Color", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    colors.forEach { colorHex ->
                        val color = parseColor(colorHex)
                        Box(
                            modifier = Modifier.size(32.dp).clip(CircleShape).background(color)
                                .then(if (selectedColor == colorHex) Modifier.border(3.dp, DarkBlueViolet, CircleShape) else Modifier)
                                .clickable { selectedColor = colorHex }
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                    Button(
                        onClick = { onSave(name, type, width.toDoubleOrNull() ?: 0.0, height.toDoubleOrNull() ?: 0.0, length.toDoubleOrNull() ?: 0.0, ceilingHeight.toDoubleOrNull() ?: 0.0, selectedColor) },
                        modifier = Modifier.weight(1f),
                        enabled = name.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Orange)
                    ) { Text("Save") }
                }
            }
        }
    }
}
