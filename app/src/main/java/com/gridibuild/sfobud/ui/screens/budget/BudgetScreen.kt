package com.gridibuild.sfobud.ui.screens.budget

import androidx.compose.foundation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.gridibuild.sfobud.data.local.entity.BudgetExpenseEntity
import com.gridibuild.sfobud.ui.components.*
import com.gridibuild.sfobud.ui.theme.*
import com.gridibuild.sfobud.viewmodel.BudgetViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BudgetScreen(navController: NavController) {
    val vm: BudgetViewModel = viewModel()
    val expenses by vm.expenses.collectAsState()
    val summary by vm.summary.collectAsState()
    val expByCategory by vm.expenseByCategory.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editExpense by remember { mutableStateOf<BudgetExpenseEntity?>(null) }
    var showBudgetDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { AppTopBar("Budget") },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }, containerColor = Orange, contentColor = Color.White) {
                Icon(Icons.Filled.Add, "Add Expense")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .background(Brush.linearGradient(listOf(SaturatedBlue, Turquoise)))
                        .padding(24.dp)
                ) {
                    Column {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Total Budget", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(formatMoney(summary.totalBudget), color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                                    IconButton(onClick = { showBudgetDialog = true }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Filled.Edit, null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                            BudgetStat("Spent", formatMoney(summary.totalSpent), if (summary.isOverBudget) WarmRed else Color.White)
                            BudgetStat("Remaining", formatMoney(summary.remaining), if (summary.remaining < 0) WarmRed else ProgressGreen)
                            BudgetStat("Planned", formatMoney(summary.totalPlanned), Color.White.copy(alpha = 0.8f))
                        }
                        Spacer(Modifier.height(16.dp))
                        if (summary.totalBudget > 0) {
                            val progress = (summary.totalSpent / summary.totalBudget).coerceIn(0.0, 1.0)
                            LinearProgressIndicator(
                                progress = progress.toFloat(),
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = if (summary.isOverBudget) WarmRed else ProgressGreen,
                                trackColor = Color.White.copy(alpha = 0.3f)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text("${(progress * 100).toInt()}% of budget used", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                        }
                    }
                }
            }

            if (expByCategory.isNotEmpty()) {
                item {
                    SectionHeader("By Category", null, null)
                    LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(expByCategory.entries.sortedByDescending { it.value }.toList()) { (cat, amount) ->
                            CategoryExpenseCard(category = cat, amount = amount, total = summary.totalSpent)
                        }
                    }
                }
            }

            if (expenses.isNotEmpty()) {
                item { SectionHeader("All Expenses", null, null) }
                items(expenses, key = { it.id }) { expense ->
                    ExpenseCard(
                        expense = expense,
                        onEdit = { editExpense = expense },
                        onDelete = { vm.deleteExpense(expense) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            } else {
                item {
                    Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        EmptyState(icon = Icons.Filled.Receipt, title = "No expenses yet", subtitle = "Track your renovation spending")
                    }
                }
            }
        }
    }

    if (showDialog || editExpense != null) {
        ExpenseDialog(
            expense = editExpense,
            categories = vm.categories,
            onDismiss = { showDialog = false; editExpense = null },
            onSave = { desc, amount, cat, isPlanned, isUnexpected ->
                if (editExpense != null) vm.updateExpense(editExpense!!.copy(description = desc, amount = amount, category = cat, isPlanned = isPlanned, isUnexpected = isUnexpected))
                else vm.addExpense(desc, amount, cat, isPlanned, isUnexpected)
                showDialog = false; editExpense = null
            }
        )
    }

    if (showBudgetDialog) {
        BudgetEditDialog(
            currentBudget = summary.totalBudget,
            onDismiss = { showBudgetDialog = false },
            onSave = { vm.updateProjectBudget(it); showBudgetDialog = false }
        )
    }
}

@Composable
fun BudgetStat(label: String, value: String, color: Color) {
    Column {
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
        Text(value, color = color, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun CategoryExpenseCard(category: String, amount: Double, total: Double) {
    val progress = if (total > 0) (amount / total).toFloat().coerceIn(0f, 1f) else 0f
    val colors = listOf(Orange, SaturatedBlue, Turquoise, WarmRed, ProgressGreen, BrightYellow)
    val color = colors[category.hashCode().let { if (it < 0) -it else it } % colors.size]

    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(0.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))) {
        Column(modifier = Modifier.width(110.dp).padding(14.dp)) {
            ColoredIconBox(icon = Icons.Filled.Category, color = color)
            Spacer(Modifier.height(8.dp))
            Text(category, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text(formatMoney(amount), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)), color = color, trackColor = GrayBeige)
            Text("${(progress * 100).toInt()}%", fontSize = 10.sp, color = OnSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseCard(expense: BudgetExpenseEntity, onEdit: () -> Unit, onDelete: () -> Unit, modifier: Modifier = Modifier) {
    var showMenu by remember { mutableStateOf(false) }
    val color = if (expense.isUnexpected) WarmRed else if (expense.isPlanned) SaturatedBlue else Orange

    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(0.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)), modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.width(4.dp).fillMaxHeight().clip(RoundedCornerShape(2.dp)).background(color))
            Column(modifier = Modifier.weight(1f)) {
                Text(expense.description, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(expense.category, fontSize = 12.sp, color = OnSurfaceVariant)
                    Text("•", color = OnSurfaceVariant)
                    Text(SimpleDateFormat("MMM dd", Locale.ENGLISH).format(Date(expense.date)), fontSize = 12.sp, color = OnSurfaceVariant)
                    if (expense.isPlanned) Text("Planned", fontSize = 11.sp, color = SaturatedBlue)
                    if (expense.isUnexpected) Text("Unexpected", fontSize = 11.sp, color = WarmRed)
                }
            }
            Text(formatMoney(expense.amount), fontWeight = FontWeight.Bold, color = color, fontSize = 15.sp)
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
fun ExpenseDialog(
    expense: BudgetExpenseEntity?,
    categories: List<String>,
    onDismiss: () -> Unit,
    onSave: (String, Double, String, Boolean, Boolean) -> Unit
) {
    var description by remember { mutableStateOf(expense?.description ?: "") }
    var amount by remember { mutableStateOf(expense?.amount?.toString() ?: "") }
    var category by remember { mutableStateOf(expense?.category ?: "Other") }
    var isPlanned by remember { mutableStateOf(expense?.isPlanned ?: false) }
    var isUnexpected by remember { mutableStateOf(expense?.isUnexpected ?: false) }
    var expandedCat by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
                Text(if (expense == null) "New Expense" else "Edit Expense", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(20.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description *") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount *") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)
                Spacer(Modifier.height(12.dp))
                ExposedDropdownMenuBox(expanded = expandedCat, onExpandedChange = { expandedCat = it }) {
                    OutlinedTextField(value = category, onValueChange = {}, readOnly = true, label = { Text("Category") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedCat) }, modifier = Modifier.fillMaxWidth().menuAnchor(), shape = RoundedCornerShape(12.dp))
                    ExposedDropdownMenu(expanded = expandedCat, onDismissRequest = { expandedCat = false }) {
                        categories.forEach { DropdownMenuItem(text = { Text(it) }, onClick = { category = it; expandedCat = false }) }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isPlanned, onCheckedChange = { isPlanned = it })
                    Text("Planned expense", fontSize = 14.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isUnexpected, onCheckedChange = { isUnexpected = it })
                    Text("Unexpected cost", fontSize = 14.sp)
                }
                Spacer(Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                    Button(onClick = { val a = amount.toDoubleOrNull() ?: return@Button; if (description.isNotBlank()) onSave(description, a, category, isPlanned, isUnexpected) }, modifier = Modifier.weight(1f), enabled = description.isNotBlank() && amount.isNotBlank(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Orange)) { Text("Save") }
                }
            }
        }
    }
}

@Composable
fun BudgetEditDialog(currentBudget: Double, onDismiss: () -> Unit, onSave: (Double) -> Unit) {
    var budget by remember { mutableStateOf(if (currentBudget > 0) currentBudget.toString() else "") }
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Set Total Budget", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = budget, onValueChange = { budget = it }, label = { Text("Total Budget") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true, leadingIcon = { Icon(Icons.Filled.AttachMoney, null) })
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                    Button(onClick = { budget.toDoubleOrNull()?.let { onSave(it) } }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Orange)) { Text("Save") }
                }
            }
        }
    }
}
