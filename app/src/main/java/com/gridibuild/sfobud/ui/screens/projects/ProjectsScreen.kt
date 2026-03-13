package com.gridibuild.sfobud.ui.screens.projects

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
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.gridibuild.sfobud.data.local.entity.ProjectEntity
import com.gridibuild.sfobud.ui.components.*
import com.gridibuild.sfobud.ui.navigation.Screen
import com.gridibuild.sfobud.ui.theme.*
import com.gridibuild.sfobud.viewmodel.ProjectViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProjectsScreen(navController: NavController) {
    val vm: ProjectViewModel = viewModel()
    val projects by vm.projects.collectAsState()
    val currentProjectId by vm.currentProjectId.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var editProject by remember { mutableStateOf<ProjectEntity?>(null) }
    var showArchived by remember { mutableStateOf(false) }
    val archivedProjects by vm.archivedProjects.collectAsState()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Projects",
                actions = {
                    IconButton(onClick = { showArchived = !showArchived }) {
                        Icon(if (showArchived) Icons.Filled.Inventory else Icons.Filled.Inventory2, null, tint = if (showArchived) Orange else MaterialTheme.colorScheme.onSurface)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = Orange,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, "Add Project")
            }
        }
    ) { padding ->
        val displayList = if (showArchived) archivedProjects else projects
        if (displayList.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState(
                    icon = Icons.Filled.Construction,
                    title = if (showArchived) "No archived projects" else "No projects yet",
                    subtitle = "Create your first renovation project",
                    actionLabel = if (!showArchived) "Create Project" else null,
                    onAction = if (!showArchived) ({ showCreateDialog = true }) else null
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (showArchived) item { Text("Archived Projects", fontWeight = FontWeight.Bold, color = OnSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp)) }
                items(displayList, key = { it.id }) { project ->
                    ProjectCard(
                        project = project,
                        isActive = project.id == currentProjectId,
                        isArchived = showArchived,
                        onClick = {
                            vm.selectProject(project.id)
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        onEdit = { editProject = project },
                        onArchive = { if (showArchived) vm.unarchiveProject(project) else vm.archiveProject(project) },
                        onDelete = { vm.deleteProject(project) }
                    )
                }
            }
        }
    }

    if (showCreateDialog || editProject != null) {
        ProjectDialog(
            project = editProject,
            onDismiss = { showCreateDialog = false; editProject = null },
            onSave = { name, desc, address, budget, start, end ->
                if (editProject != null) {
                    vm.updateProject(editProject!!.copy(name = name, description = desc, address = address, totalBudget = budget, startDate = start, endDate = end))
                } else {
                    vm.createProject(name, desc, address, budget, start, end)
                }
                showCreateDialog = false; editProject = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectCard(
    project: ProjectEntity,
    isActive: Boolean,
    isArchived: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = if (isActive) Orange.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface),
        border = if (isActive) BorderStroke(2.dp, Orange) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column {
            Box(
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(Brush.linearGradient(listOf(Orange, BrightYellow)))
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(project.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        if (isActive) Surface(color = Orange, shape = RoundedCornerShape(8.dp)) {
                            Text("Active", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                        }
                    }
                    if (project.address.isNotBlank()) {
                        Text(project.address, fontSize = 12.sp, color = OnSurfaceVariant)
                    }
                    if (project.description.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(project.description, fontSize = 13.sp, color = OnSurfaceVariant, maxLines = 2)
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (project.startDate != null) {
                            InfoChip(Icons.Filled.CalendarToday, formatDate(project.startDate))
                        }
                        if (project.totalBudget > 0) {
                            InfoChip(Icons.Filled.AttachMoney, formatMoney(project.totalBudget))
                        }
                    }
                }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Filled.MoreVert, null, tint = OnSurfaceVariant)
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(text = { Text("Edit") }, onClick = { onEdit(); showMenu = false }, leadingIcon = { Icon(Icons.Filled.Edit, null) })
                        DropdownMenuItem(
                            text = { Text(if (isArchived) "Unarchive" else "Archive") },
                            onClick = { onArchive(); showMenu = false },
                            leadingIcon = { Icon(Icons.Filled.Inventory, null) }
                        )
                        DropdownMenuItem(text = { Text("Delete", color = WarmRed) }, onClick = { showDeleteConfirm = true; showMenu = false }, leadingIcon = { Icon(Icons.Filled.Delete, null, tint = WarmRed) })
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Project") },
            text = { Text("Are you sure you want to delete \"${project.name}\"? All data will be lost.") },
            confirmButton = { TextButton(onClick = { onDelete(); showDeleteConfirm = false }) { Text("Delete", color = WarmRed) } },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } }
        )
    }
}

@Composable
fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, null, modifier = Modifier.size(14.dp), tint = OnSurfaceVariant)
        Text(text, fontSize = 12.sp, color = OnSurfaceVariant)
    }
}

@Composable
fun ProjectDialog(
    project: ProjectEntity?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Double, Long?, Long?) -> Unit
) {
    var name by remember { mutableStateOf(project?.name ?: "") }
    var description by remember { mutableStateOf(project?.description ?: "") }
    var address by remember { mutableStateOf(project?.address ?: "") }
    var budget by remember { mutableStateOf(if ((project?.totalBudget ?: 0.0) > 0) project!!.totalBudget.toString() else "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
                Text(if (project == null) "New Project" else "Edit Project", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(20.dp))
                listOf(
                    Triple("Project Name *", name, { v: String -> name = v }),
                    Triple("Description", description, { v: String -> description = v }),
                    Triple("Address", address, { v: String -> address = v }),
                    Triple("Total Budget", budget, { v: String -> budget = v })
                ).forEach { (label, value, onChange) ->
                    OutlinedTextField(
                        value = value, onValueChange = onChange,
                        label = { Text(label) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = label != "Description"
                    )
                    Spacer(Modifier.height(12.dp))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                    Button(
                        onClick = { onSave(name, description, address, budget.toDoubleOrNull() ?: 0.0, null, null) },
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

private fun formatDate(timestamp: Long): String =
    SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH).format(Date(timestamp))
