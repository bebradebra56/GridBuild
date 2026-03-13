package com.gridibuild.sfobud.ui.screens.tasks

import androidx.compose.foundation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.gridibuild.sfobud.data.local.entity.TaskEntity
import com.gridibuild.sfobud.ui.components.*
import com.gridibuild.sfobud.ui.theme.*
import com.gridibuild.sfobud.viewmodel.TaskFilter
import com.gridibuild.sfobud.viewmodel.TasksViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TasksScreen(navController: NavController) {
    val vm: TasksViewModel = viewModel()
    val tasks by vm.tasks.collectAsState()
    val filter by vm.filter.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editTask by remember { mutableStateOf<TaskEntity?>(null) }

    Scaffold(
        topBar = { AppTopBar("Tasks") },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }, containerColor = Orange, contentColor = Color.White) {
                Icon(Icons.Filled.Add, "Add Task")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            ScrollableTabRow(
                selectedTabIndex = TaskFilter.entries.indexOf(filter),
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = Orange
            ) {
                TaskFilter.entries.forEachIndexed { _, f ->
                    Tab(
                        selected = filter == f,
                        onClick = { vm.setFilter(f) },
                        text = {
                            Text(
                                f.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                                fontSize = 13.sp,
                                fontWeight = if (filter == f) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            if (tasks.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(icon = Icons.Filled.CheckCircle, title = "No tasks", subtitle = "Add tasks to track your renovation work")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(tasks, key = { it.id }) { task ->
                        TaskCard(
                            task = task,
                            onToggle = { vm.toggleTaskStatus(task) },
                            onEdit = { editTask = task },
                            onDelete = { vm.deleteTask(task) }
                        )
                    }
                }
            }
        }
    }

    if (showDialog || editTask != null) {
        TaskDialog(
            task = editTask,
            onDismiss = { showDialog = false; editTask = null },
            onSave = { title, desc, priority, dueDate ->
                if (editTask != null) vm.updateTask(editTask!!.copy(title = title, description = desc, priority = priority, dueDate = dueDate))
                else vm.createTask(title, desc, priority, dueDate, null)
                showDialog = false; editTask = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCard(
    task: TaskEntity,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isDone = task.status == "DONE"
    val isOverdue = task.dueDate != null && task.dueDate < System.currentTimeMillis() && !isDone
    var showMenu by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isDone -> MaterialTheme.colorScheme.surfaceVariant
                isOverdue -> WarmRed.copy(alpha = 0.06f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(0.dp),
        border = if (!isDone && !isOverdue) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)) else null
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Checkbox(
                checked = isDone,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = ProgressGreen, uncheckedColor = GrayBeige)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    task.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (isDone) OnSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
                if (task.description.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(task.description, fontSize = 12.sp, color = OnSurfaceVariant, maxLines = 2)
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PriorityChip(task.priority)
                    if (task.dueDate != null) {
                        Text(
                            if (isOverdue) "⚠ ${formatDate(task.dueDate)}" else formatDate(task.dueDate),
                            fontSize = 11.sp,
                            color = if (isOverdue) WarmRed else OnSurfaceVariant
                        )
                    }
                    val statusColor = when (task.status) {
                        "IN_PROGRESS" -> SaturatedBlue; "DONE" -> ProgressGreen; else -> GrayBeige
                    }
                    Surface(color = statusColor.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)) {
                        Text(task.status.replace("_", " "), color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDialog(
    task: TaskEntity?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Long?) -> Unit
) {
    var title by remember { mutableStateOf(task?.title ?: "") }
    var description by remember { mutableStateOf(task?.description ?: "") }
    var priority by remember { mutableStateOf(task?.priority ?: "MEDIUM") }
    var selectedDateMillis by remember { mutableStateOf(task?.dueDate) }
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDateMillis
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedDateMillis = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) { Text("OK", color = Orange) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = Orange,
                    todayDateBorderColor = Orange
                )
            )
        }
        return
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(if (task == null) "New Task" else "Edit Task", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(20.dp))
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Task Title *") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), minLines = 2)
                Spacer(Modifier.height(16.dp))
                Text("Priority", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("LOW", "MEDIUM", "HIGH").forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = when (p) { "HIGH" -> WarmRed; "MEDIUM" -> Orange; else -> ProgressGreen },
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text("Due Date", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = if (selectedDateMillis != null) DarkBlueViolet else OnSurfaceVariant)
                ) {
                    Icon(Icons.Filled.CalendarMonth, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = selectedDateMillis?.let { formatDate(it) } ?: "Select date",
                        fontSize = 14.sp
                    )
                    if (selectedDateMillis != null) {
                        Spacer(Modifier.weight(1f))
                        IconButton(
                            onClick = { selectedDateMillis = null },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(Icons.Filled.Clear, null, modifier = Modifier.size(16.dp), tint = OnSurfaceVariant)
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                    Button(
                        onClick = { onSave(title, description, priority, selectedDateMillis) },
                        modifier = Modifier.weight(1f),
                        enabled = title.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Orange)
                    ) { Text("Save") }
                }
            }
        }
    }
}

private fun formatDate(timestamp: Long): String = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH).format(Date(timestamp))
