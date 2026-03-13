package com.gridibuild.sfobud.ui.screens.photos

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.gridibuild.sfobud.data.local.entity.PhotoEntity
import com.gridibuild.sfobud.ui.components.*
import com.gridibuild.sfobud.ui.theme.*
import com.gridibuild.sfobud.viewmodel.PhotosViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PhotosScreen(navController: NavController) {
    val vm: PhotosViewModel = viewModel()
    val photos by vm.filteredPhotos.collectAsState()
    val stageFilter by vm.stageFilter.collectAsState()
    val rooms by vm.rooms.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedStageForAdd by remember { mutableStateOf("DURING") }
    var selectedUri by remember { mutableStateOf<String?>(null) }
    var captionText by remember { mutableStateOf("") }
    var selectedRoomId by remember { mutableStateOf<Long?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedUri = it.toString()
            showAddDialog = true
        }
    }

    Scaffold(
        topBar = { AppTopBar("Progress Photos", onBack = { navController.popBackStack() }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { launcher.launch("image/*") }, containerColor = Orange, contentColor = Color.White) {
                Icon(Icons.Filled.AddAPhoto, "Add Photo")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(
                selectedTabIndex = vm.stages.indexOf(stageFilter),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = Orange
            ) {
                vm.stages.forEach { stage ->
                    Tab(
                        selected = stageFilter == stage,
                        onClick = { vm.setStageFilter(stage) },
                        text = {
                            Text(
                                if (stage == "ALL") "All" else stage.lowercase().replaceFirstChar { it.uppercase() },
                                fontSize = 12.sp,
                                fontWeight = if (stageFilter == stage) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            if (photos.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon = Icons.Filled.PhotoCamera,
                        title = "No photos yet",
                        subtitle = "Document your renovation progress",
                        actionLabel = "Add Photo",
                        onAction = { launcher.launch("image/*") }
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(photos, key = { it.id }) { photo ->
                        PhotoCard(photo = photo, onDelete = { vm.deletePhoto(photo) })
                    }
                }
            }
        }
    }

    if (showAddDialog && selectedUri != null) {
        Dialog(onDismissRequest = { showAddDialog = false; selectedUri = null }) {
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Add Photo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    AsyncImage(model = selectedUri, contentDescription = null, modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
                    Spacer(Modifier.height(16.dp))
                    Text("Stage", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("BEFORE" to SaturatedBlue, "DURING" to Orange, "AFTER" to ProgressGreen).forEach { (stage, color) ->
                            FilterChip(selected = selectedStageForAdd == stage, onClick = { selectedStageForAdd = stage }, label = { Text(stage, fontSize = 11.sp) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = color, selectedLabelColor = Color.White))
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(value = captionText, onValueChange = { captionText = it }, label = { Text("Caption (optional)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)
                    Spacer(Modifier.height(20.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TextButton(onClick = { showAddDialog = false; selectedUri = null }, modifier = Modifier.weight(1f)) { Text("Cancel") }
                        Button(
                            onClick = {
                                vm.addPhoto(selectedUri!!, selectedStageForAdd, captionText, selectedRoomId)
                                showAddDialog = false; selectedUri = null; captionText = ""
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Orange)
                        ) { Text("Save") }
                    }
                }
            }
        }
    }
}

@Composable
fun PhotoCard(photo: PhotoEntity, onDelete: () -> Unit) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val stageColor = when (photo.stage) { "BEFORE" -> SaturatedBlue; "AFTER" -> ProgressGreen; else -> Orange }

    Card(shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(0.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))) {
        Box {
            AsyncImage(
                model = photo.imagePath,
                contentDescription = photo.caption,
                modifier = Modifier.fillMaxWidth().height(150.dp),
                contentScale = ContentScale.Crop
            )
            Surface(
                color = stageColor.copy(alpha = 0.85f),
                shape = RoundedCornerShape(bottomEnd = 8.dp),
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Text(photo.stage, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }
            IconButton(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier.align(Alignment.TopEnd).size(32.dp)
            ) {
                Icon(Icons.Filled.Delete, null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
            if (photo.caption.isNotBlank()) {
                Surface(
                    color = Color.Black.copy(alpha = 0.5f),
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                ) {
                    Text(photo.caption, color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(8.dp), maxLines = 2)
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Photo") },
            text = { Text("Remove this photo?") },
            confirmButton = { TextButton(onClick = { onDelete(); showDeleteConfirm = false }) { Text("Delete", color = WarmRed) } },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } }
        )
    }
}
