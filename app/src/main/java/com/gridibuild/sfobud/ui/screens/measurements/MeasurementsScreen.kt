package com.gridibuild.sfobud.ui.screens.measurements

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.gridibuild.sfobud.data.local.entity.MeasurementEntity
import com.gridibuild.sfobud.data.local.entity.RoomEntity
import com.gridibuild.sfobud.ui.components.*
import com.gridibuild.sfobud.ui.components.currentUnits
import com.gridibuild.sfobud.ui.theme.*
import com.gridibuild.sfobud.viewmodel.MeasurementsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeasurementsScreen(navController: NavController) {
    val vm: MeasurementsViewModel = viewModel()
    val rooms by vm.rooms.collectAsState()
    val selectedRoom by vm.selectedRoom.collectAsState()
    val measurements by vm.measurements.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editMeasurement by remember { mutableStateOf<MeasurementEntity?>(null) }
    var expandedRoom by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { AppTopBar("Measurements", onBack = { navController.popBackStack() }) },
        floatingActionButton = {
            if (selectedRoom != null) {
                FloatingActionButton(onClick = { showDialog = true }, containerColor = Orange, contentColor = Color.White) {
                    Icon(Icons.Filled.Add, "Add Measurement")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (rooms.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(0.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    ExposedDropdownMenuBox(expanded = expandedRoom, onExpandedChange = { expandedRoom = it }, modifier = Modifier.padding(4.dp)) {
                        OutlinedTextField(
                            value = selectedRoom?.name ?: "Select Room",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Room") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedRoom) },
                            leadingIcon = { Icon(Icons.Filled.MeetingRoom, null, tint = Orange) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(expanded = expandedRoom, onDismissRequest = { expandedRoom = false }) {
                            rooms.forEach { room ->
                                DropdownMenuItem(
                                    text = { Text(room.name) },
                                    onClick = { vm.selectRoom(room.id); expandedRoom = false }
                                )
                            }
                        }
                    }
                }
            }

            if (selectedRoom == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(icon = Icons.Filled.Straighten, title = "No rooms", subtitle = "Add rooms first to track measurements")
                }
            } else if (measurements.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(icon = Icons.Filled.Straighten, title = "No measurements", subtitle = "Add wall, floor, and ceiling measurements", actionLabel = "Add Measurement", onAction = { showDialog = true })
                }
            } else {
                val grouped = measurements.groupBy { it.type }
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    grouped.forEach { (type, list) ->
                        item {
                            Text(type, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Orange, modifier = Modifier.padding(bottom = 4.dp, top = 8.dp))
                        }
                        items(list, key = { it.id }) { m ->
                            MeasurementCard(
                                measurement = m,
                                onEdit = { editMeasurement = m },
                                onDelete = { vm.deleteMeasurement(m) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDialog || editMeasurement != null) {
        MeasurementDialog(
            measurement = editMeasurement,
            types = vm.measurementTypes,
            units = vm.units,
            onDismiss = { showDialog = false; editMeasurement = null },
            onSave = { type, name, value, unit, notes ->
                if (editMeasurement != null) vm.updateMeasurement(editMeasurement!!.copy(type = type, name = name, value = value, unit = unit, notes = notes))
                else vm.addMeasurement(type, name, value, unit, notes)
                showDialog = false; editMeasurement = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeasurementCard(measurement: MeasurementEntity, onEdit: () -> Unit, onDelete: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    val typeColor = when (measurement.type) {
        "Wall" -> SaturatedBlue; "Ceiling" -> Turquoise; "Floor" -> Orange
        "Window" -> BrightYellow; "Door" -> WarmRed; else -> ProgressGreen
    }

    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(0.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ColoredIconBox(icon = Icons.Filled.Straighten, color = typeColor)
            Column(modifier = Modifier.weight(1f)) {
                Text(if (measurement.name.isNotBlank()) measurement.name else measurement.type, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                if (measurement.notes.isNotBlank()) Text(measurement.notes, fontSize = 12.sp, color = OnSurfaceVariant, maxLines = 1)
            }
            Text("${measurement.value} ${measurement.unit}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = typeColor)
            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) { Icon(Icons.Filled.MoreVert, null, modifier = Modifier.size(18.dp)) }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(text = { Text("Edit") }, onClick = { onEdit(); showMenu = false }, leadingIcon = { Icon(Icons.Filled.Edit, null) })
                    DropdownMenuItem(text = { Text("Delete", color = WarmRed) }, onClick = { onDelete(); showMenu = false }, leadingIcon = { Icon(Icons.Filled.Delete, null, tint = WarmRed) })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeasurementDialog(
    measurement: MeasurementEntity?,
    types: List<String>,
    units: List<String>,
    onDismiss: () -> Unit,
    onSave: (String, String, Double, String, String) -> Unit
) {
    val globalUnits = currentUnits()
    var type by remember { mutableStateOf(measurement?.type ?: types.first()) }
    var name by remember { mutableStateOf(measurement?.name ?: "") }
    var value by remember { mutableStateOf(measurement?.value?.toString() ?: "") }
    var unit by remember { mutableStateOf(measurement?.unit ?: globalUnits) }
    var notes by remember { mutableStateOf(measurement?.notes ?: "") }
    var expandedType by remember { mutableStateOf(false) }
    var expandedUnit by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(if (measurement == null) "Add Measurement" else "Edit Measurement", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(20.dp))
                ExposedDropdownMenuBox(expanded = expandedType, onExpandedChange = { expandedType = it }) {
                    OutlinedTextField(value = type, onValueChange = {}, readOnly = true, label = { Text("Type") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedType) }, modifier = Modifier.fillMaxWidth().menuAnchor(), shape = RoundedCornerShape(12.dp))
                    ExposedDropdownMenu(expanded = expandedType, onDismissRequest = { expandedType = false }) {
                        types.forEach { DropdownMenuItem(text = { Text(it) }, onClick = { type = it; expandedType = false }) }
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name / Label") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = value, onValueChange = { value = it }, label = { Text("Value *") }, modifier = Modifier.weight(2f), shape = RoundedCornerShape(12.dp), singleLine = true)
                    ExposedDropdownMenuBox(expanded = expandedUnit, onExpandedChange = { expandedUnit = it }, modifier = Modifier.weight(1f)) {
                        OutlinedTextField(value = unit, onValueChange = {}, readOnly = true, label = { Text("Unit") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedUnit) }, modifier = Modifier.menuAnchor(), shape = RoundedCornerShape(12.dp))
                        ExposedDropdownMenu(expanded = expandedUnit, onDismissRequest = { expandedUnit = false }) {
                            units.forEach { DropdownMenuItem(text = { Text(it) }, onClick = { unit = it; expandedUnit = false }) }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)
                Spacer(Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                    Button(onClick = { value.toDoubleOrNull()?.let { v -> onSave(type, name, v, unit, notes) } }, modifier = Modifier.weight(1f), enabled = value.isNotBlank(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Orange)) { Text("Save") }
                }
            }
        }
    }
}
