package com.gridibuild.sfobud.ui.screens.materials

import androidx.compose.foundation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.gridibuild.sfobud.data.local.entity.MaterialEntity
import com.gridibuild.sfobud.ui.components.*
import com.gridibuild.sfobud.ui.theme.*
import com.gridibuild.sfobud.viewmodel.MaterialsViewModel

@Composable
fun MaterialsScreen(navController: NavController) {
    val vm: MaterialsViewModel = viewModel()
    val materials by vm.materials.collectAsState()
    val categoryFilter by vm.categoryFilter.collectAsState()
    val totalCost by vm.totalCost.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editMaterial by remember { mutableStateOf<MaterialEntity?>(null) }

    Scaffold(
        topBar = { AppTopBar("Materials", onBack = { navController.popBackStack() }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }, containerColor = Orange, contentColor = Color.White) {
                Icon(Icons.Filled.Add, "Add Material")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Orange.copy(alpha = 0.1f))
            ) {
                Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Total Material Cost", fontWeight = FontWeight.SemiBold)
                    Text(formatMoney(totalCost), fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Orange)
                }
            }

            ScrollableTabRow(
                selectedTabIndex = vm.categories.indexOf(categoryFilter),
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = Orange
            ) {
                vm.categories.forEach { cat ->
                    Tab(selected = categoryFilter == cat, onClick = { vm.setCategoryFilter(cat) },
                        text = { Text(cat, fontSize = 12.sp, fontWeight = if (categoryFilter == cat) FontWeight.Bold else FontWeight.Normal) })
                }
            }

            if (materials.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(icon = Icons.Filled.Construction, title = "No materials", subtitle = "Add materials needed for your renovation")
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(materials, key = { it.id }) { material ->
                        MaterialCard(
                            material = material,
                            onToggle = { vm.togglePurchased(material) },
                            onEdit = { editMaterial = material },
                            onDelete = { vm.deleteMaterial(material) }
                        )
                    }
                }
            }
        }
    }

    if (showDialog || editMaterial != null) {
        MaterialDialog(
            material = editMaterial,
            categories = vm.categories.drop(1),
            onDismiss = { showDialog = false; editMaterial = null },
            onSave = { name, cat, qty, unit, price, notes ->
                if (editMaterial != null) vm.updateMaterial(editMaterial!!.copy(name = name, category = cat, quantity = qty, unit = unit, pricePerUnit = price, notes = notes))
                else vm.addMaterial(name, cat, qty, unit, price, notes, null)
                showDialog = false; editMaterial = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialCard(
    material: MaterialEntity,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val total = material.quantity * material.pricePerUnit

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (material.purchased) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        border = if (!material.purchased) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)) else null
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Checkbox(checked = material.purchased, onCheckedChange = { onToggle() }, colors = CheckboxDefaults.colors(checkedColor = ProgressGreen))
            Column(modifier = Modifier.weight(1f)) {
                Text(material.name, fontWeight = FontWeight.SemiBold, textDecoration = if (material.purchased) TextDecoration.LineThrough else TextDecoration.None)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CategoryBadge(material.category)
                    Text("${material.quantity} ${material.unit}", fontSize = 12.sp, color = OnSurfaceVariant)
                }
                if (material.pricePerUnit > 0) {
                    Spacer(Modifier.height(4.dp))
                    Text("${formatMoney(material.pricePerUnit)}/${material.unit} = ${formatMoney(total)}", fontSize = 12.sp, color = Orange, fontWeight = FontWeight.Medium)
                }
                if (material.notes.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(material.notes, fontSize = 11.sp, color = OnSurfaceVariant, maxLines = 2)
                }
            }
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

@Composable
fun CategoryBadge(category: String) {
    val color = when (category) {
        "Paint" -> Color(0xFFE91E63); "Flooring" -> Color(0xFF795548); "Tiles" -> Color(0xFF9C27B0)
        "Lighting" -> BrightYellow; "Plumbing" -> SaturatedBlue; "Hardware" -> Color(0xFF607D8B)
        "Decor" -> Color(0xFFE91E63); "Tools" -> Orange; "Electrical" -> Color(0xFFFF9800)
        else -> GrayBeige
    }
    Surface(color = color.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)) {
        Text(category, color = color, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialDialog(
    material: MaterialEntity?,
    categories: List<String>,
    onDismiss: () -> Unit,
    onSave: (String, String, Double, String, Double, String) -> Unit
) {
    var name by remember { mutableStateOf(material?.name ?: "") }
    var category by remember { mutableStateOf(material?.category ?: "Other") }
    var quantity by remember { mutableStateOf(material?.quantity?.toString() ?: "1") }
    var unit by remember { mutableStateOf(material?.unit ?: "pcs") }
    var price by remember { mutableStateOf(material?.pricePerUnit?.toString() ?: "") }
    var notes by remember { mutableStateOf(material?.notes ?: "") }
    var expandedCat by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
                Text(if (material == null) "New Material" else "Edit Material", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(20.dp))
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name *") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)
                Spacer(Modifier.height(12.dp))
                ExposedDropdownMenuBox(expanded = expandedCat, onExpandedChange = { expandedCat = it }) {
                    OutlinedTextField(value = category, onValueChange = {}, readOnly = true, label = { Text("Category") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedCat) }, modifier = Modifier.fillMaxWidth().menuAnchor(), shape = RoundedCornerShape(12.dp))
                    ExposedDropdownMenu(expanded = expandedCat, onDismissRequest = { expandedCat = false }) {
                        categories.forEach { DropdownMenuItem(text = { Text(it) }, onClick = { category = it; expandedCat = false }) }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("Qty") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), singleLine = true)
                    OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Unit") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), singleLine = true)
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price per unit") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                    Button(onClick = { onSave(name, category, quantity.toDoubleOrNull() ?: 1.0, unit, price.toDoubleOrNull() ?: 0.0, notes) }, modifier = Modifier.weight(1f), enabled = name.isNotBlank(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Orange)) { Text("Save") }
                }
            }
        }
    }
}
