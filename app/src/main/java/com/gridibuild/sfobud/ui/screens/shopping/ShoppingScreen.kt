package com.gridibuild.sfobud.ui.screens.shopping

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.gridibuild.sfobud.data.local.entity.ShoppingItemEntity
import com.gridibuild.sfobud.ui.components.*
import com.gridibuild.sfobud.ui.theme.*
import com.gridibuild.sfobud.viewmodel.ShoppingViewModel

@Composable
fun ShoppingScreen(navController: NavController) {
    val vm: ShoppingViewModel = viewModel()
    val items by vm.items.collectAsState()
    val tabFilter by vm.tabFilter.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editItem by remember { mutableStateOf<ShoppingItemEntity?>(null) }

    Scaffold(
        topBar = { AppTopBar("Shopping List", onBack = { navController.popBackStack() }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }, containerColor = Orange, contentColor = Color.White) {
                Icon(Icons.Filled.Add, "Add Item")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(
                selectedTabIndex = vm.tabs.indexOf(tabFilter),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = Orange
            ) {
                vm.tabs.forEach { tab ->
                    Tab(
                        selected = tabFilter == tab,
                        onClick = { vm.setTab(tab) },
                        text = {
                            val color = when (tab) {
                                "Urgent" -> WarmRed; "This Week" -> Orange; "Later" -> SaturatedBlue; else -> ProgressGreen
                            }
                            Text(tab, fontSize = 12.sp, color = if (tabFilter == tab) color else OnSurfaceVariant, fontWeight = if (tabFilter == tab) FontWeight.Bold else FontWeight.Normal)
                        }
                    )
                }
            }

            if (items.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(icon = Icons.Filled.ShoppingCart, title = "Nothing here", subtitle = "Add items to your shopping list")
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(items, key = { it.id }) { item ->
                        ShoppingItemCard(
                            item = item,
                            isBought = tabFilter == "Bought",
                            onMarkBought = { vm.markBought(item) },
                            onMarkPending = { vm.markPending(item) },
                            onEdit = { editItem = item },
                            onDelete = { vm.deleteItem(item) }
                        )
                    }
                }
            }
        }
    }

    if (showDialog || editItem != null) {
        ShoppingDialog(
            item = editItem,
            onDismiss = { showDialog = false; editItem = null },
            onSave = { name, cat, qty, unit, price, urgency, notes ->
                if (editItem != null) vm.updateItem(editItem!!.copy(name = name, category = cat, quantity = qty, unit = unit, estimatedPrice = price, urgency = urgency, notes = notes))
                else vm.addItem(name, cat, qty, unit, price, urgency, notes)
                showDialog = false; editItem = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingItemCard(
    item: ShoppingItemEntity,
    isBought: Boolean,
    onMarkBought: () -> Unit,
    onMarkPending: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val urgencyColor = when (item.urgency) {
        "URGENT" -> WarmRed; "THIS_WEEK" -> Orange; else -> SaturatedBlue
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isBought) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        border = if (!isBought) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)) else null
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Checkbox(
                checked = isBought,
                onCheckedChange = { if (isBought) onMarkPending() else onMarkBought() },
                colors = CheckboxDefaults.colors(checkedColor = ProgressGreen)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontWeight = FontWeight.SemiBold, textDecoration = if (isBought) TextDecoration.LineThrough else TextDecoration.None)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${item.quantity} ${item.unit}", fontSize = 12.sp, color = OnSurfaceVariant)
                    if (item.category.isNotBlank()) Text("• ${item.category}", fontSize = 12.sp, color = OnSurfaceVariant)
                }
                if (item.estimatedPrice > 0) {
                    Spacer(Modifier.height(4.dp))
                    Text("~${formatMoney(item.estimatedPrice)}", fontSize = 13.sp, color = Orange, fontWeight = FontWeight.Medium)
                }
                if (!isBought) {
                    Spacer(Modifier.height(6.dp))
                    Surface(color = urgencyColor.copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp)) {
                        Text(item.urgency.replace("_", " "), color = urgencyColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                    }
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
fun ShoppingDialog(
    item: ShoppingItemEntity?,
    onDismiss: () -> Unit,
    onSave: (String, String, Double, String, Double, String, String) -> Unit
) {
    var name by remember { mutableStateOf(item?.name ?: "") }
    var category by remember { mutableStateOf(item?.category ?: "") }
    var quantity by remember { mutableStateOf(item?.quantity?.toString() ?: "1") }
    var unit by remember { mutableStateOf(item?.unit ?: "pcs") }
    var price by remember { mutableStateOf(item?.estimatedPrice?.toString() ?: "") }
    var urgency by remember { mutableStateOf(item?.urgency ?: "THIS_WEEK") }
    var notes by remember { mutableStateOf(item?.notes ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
                Text(if (item == null) "New Shopping Item" else "Edit Item", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(20.dp))
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Item Name *") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("Qty") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), singleLine = true)
                    OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Unit") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), singleLine = true)
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Est. Price") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)
                Spacer(Modifier.height(16.dp))
                Text("Urgency", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("URGENT" to WarmRed, "THIS_WEEK" to Orange, "LATER" to SaturatedBlue).forEach { (u, color) ->
                        FilterChip(selected = urgency == u, onClick = { urgency = u }, label = { Text(u.replace("_", " "), fontSize = 11.sp) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = color, selectedLabelColor = Color.White))
                    }
                }
                Spacer(Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                    Button(onClick = { onSave(name, category, quantity.toDoubleOrNull() ?: 1.0, unit, price.toDoubleOrNull() ?: 0.0, urgency, notes) }, modifier = Modifier.weight(1f), enabled = name.isNotBlank(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Orange)) { Text("Save") }
                }
            }
        }
    }
}
