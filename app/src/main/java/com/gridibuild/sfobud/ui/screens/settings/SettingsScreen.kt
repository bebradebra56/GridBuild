package com.gridibuild.sfobud.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.gridibuild.sfobud.ui.components.AppTopBar
import com.gridibuild.sfobud.ui.theme.*
import com.gridibuild.sfobud.viewmodel.AuthViewModel
import com.gridibuild.sfobud.viewmodel.ExportStatus
import com.gridibuild.sfobud.viewmodel.ProjectViewModel
import com.gridibuild.sfobud.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(navController: NavController, authViewModel: AuthViewModel) {
    val vm: SettingsViewModel = viewModel()
    val projectVm: ProjectViewModel = viewModel()
    val authState by authViewModel.state.collectAsState()
    val currency by vm.currency.collectAsState()
    val units by vm.units.collectAsState()
    val isDark by vm.isDarkTheme.collectAsState()
    val notificationsEnabled by vm.notificationsEnabled.collectAsState()
    val archivedProjects by projectVm.archivedProjects.collectAsState()
    val exportStatus by vm.exportStatus.collectAsState()
    val context = LocalContext.current

    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showUnitsDialog by remember { mutableStateOf(false) }
    var showClearDataConfirm by remember { mutableStateOf(false) }
    var showEditName by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }

    LaunchedEffect(exportStatus) {
        if (exportStatus is ExportStatus.Success || exportStatus is ExportStatus.Error) {
            kotlinx.coroutines.delay(3000)
            vm.clearExportStatus()
        }
    }

    Scaffold(
        topBar = { AppTopBar("Settings", onBack = { navController.popBackStack() }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Orange.copy(alpha = 0.1f)),
                    elevation = CardDefaults.cardElevation(0.dp),
                    border = BorderStroke(1.dp, Orange.copy(alpha = 0.2f))
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Box(
                            modifier = Modifier.size(56.dp).clip(CircleShape).background(Orange.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(authState.userName.firstOrNull()?.uppercase() ?: "U", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Orange)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(authState.userName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Renovation Planner", fontSize = 13.sp, color = OnSurfaceVariant)
                        }
                        IconButton(onClick = { editName = authState.userName; showEditName = true }) {
                            Icon(Icons.Filled.Edit, null, tint = Orange, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            if (exportStatus !is ExportStatus.Idle) {
                item {
                    val (bg, fg, msg) = when (val s = exportStatus) {
                        is ExportStatus.Loading -> Triple(SaturatedBlue.copy(alpha = 0.1f), SaturatedBlue, "Processing...")
                        is ExportStatus.Success -> Triple(ProgressGreen.copy(alpha = 0.1f), ProgressGreen, s.message)
                        is ExportStatus.Error -> Triple(WarmRed.copy(alpha = 0.1f), WarmRed, s.message)
                        else -> Triple(Color.Transparent, Color.Transparent, "")
                    }
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = bg), elevation = CardDefaults.cardElevation(0.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (exportStatus is ExportStatus.Loading) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = fg, strokeWidth = 2.dp)
                            } else {
                                Icon(if (exportStatus is ExportStatus.Success) Icons.Filled.CheckCircle else Icons.Filled.Error, null, tint = fg, modifier = Modifier.size(18.dp))
                            }
                            Text(msg, fontSize = 13.sp, color = fg, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            item { SettingsSectionHeader("Appearance") }
            item {
                SettingsCard {
                    SettingsRow(icon = Icons.Filled.DarkMode, iconColor = DarkBlueViolet, title = "Dark Theme", subtitle = if (isDark) "Dark" else "Light") {
                        Switch(checked = isDark, onCheckedChange = { vm.setDarkTheme(it) }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Orange))
                    }
                }
            }

            item { SettingsSectionHeader("Preferences") }
            item {
                SettingsCard {
                    SettingsRow(icon = Icons.Filled.AttachMoney, iconColor = ProgressGreen, title = "Currency", subtitle = currency, onClick = { showCurrencyDialog = true })
                    androidx.compose.material3.HorizontalDivider(color = GrayBeige)
                    SettingsRow(icon = Icons.Filled.Straighten, iconColor = SaturatedBlue, title = "Units", subtitle = if (units == "m") "Metric (m)" else "Imperial (ft)", onClick = { showUnitsDialog = true })
                    androidx.compose.material3.HorizontalDivider(color = GrayBeige)
                    SettingsRow(icon = Icons.Filled.Notifications, iconColor = Orange, title = "Notifications", subtitle = if (notificationsEnabled) "Enabled" else "Disabled") {
                        Switch(checked = notificationsEnabled, onCheckedChange = { vm.setNotificationsEnabled(it) }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Orange))
                    }
                }
            }

            item { SettingsSectionHeader("Project Archive") }
            item {
                SettingsCard {
                    SettingsRow(icon = Icons.Filled.Inventory, iconColor = Turquoise, title = "Archived Projects", subtitle = "${archivedProjects.size} archived", onClick = { navController.navigate(com.gridibuild.sfobud.ui.navigation.Screen.Projects.route) })
                }
            }

            item { SettingsSectionHeader("Data") }
            item {
                SettingsCard {
                    SettingsRow(
                        icon = Icons.Filled.Upload,
                        iconColor = SaturatedBlue,
                        title = "Export Data",
                        subtitle = "Share all project data as JSON",
                        onClick = { vm.exportData(context) }
                    )
                    androidx.compose.material3.HorizontalDivider(color = GrayBeige)
                    SettingsRow(
                        icon = Icons.Filled.Backup,
                        iconColor = Turquoise,
                        title = "Backup",
                        subtitle = "Save local backup (keeps last 5)",
                        onClick = { vm.backupData(context) }
                    )
                    androidx.compose.material3.HorizontalDivider(color = GrayBeige)
                    SettingsRow(
                        icon = Icons.Filled.DeleteSweep,
                        iconColor = WarmRed,
                        title = "Clear All Data",
                        subtitle = "Delete all projects and data",
                        onClick = { showClearDataConfirm = true }
                    )
                }
            }

            item {
                Box(Modifier.fillMaxWidth().padding(top = 16.dp), contentAlignment = Alignment.Center) {
                    Text("GridBuild v1.0", fontSize = 12.sp, color = OnSurfaceVariant)
                }
            }
        }
    }

    if (showEditName) {
        AlertDialog(
            onDismissRequest = { showEditName = false },
            title = { Text("Edit Name") },
            text = {
                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    label = { Text("Your name") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (editName.isNotBlank()) authViewModel.updateUserName(editName)
                    showEditName = false
                }) { Text("Save", color = Orange, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showEditName = false }) { Text("Cancel") } }
        )
    }

    if (showCurrencyDialog) {
        SelectionDialog(
            title = "Currency",
            options = vm.currencies,
            selected = currency,
            onSelect = { vm.setCurrency(it); showCurrencyDialog = false },
            onDismiss = { showCurrencyDialog = false }
        )
    }

    if (showUnitsDialog) {
        SelectionDialog(
            title = "Units",
            options = vm.unitOptions,
            selected = units,
            displayName = { if (it == "m") "Metric (m)" else "Imperial (ft)" },
            onSelect = { vm.setUnits(it); showUnitsDialog = false },
            onDismiss = { showUnitsDialog = false }
        )
    }

    if (showClearDataConfirm) {
        AlertDialog(
            onDismissRequest = { showClearDataConfirm = false },
            icon = { Icon(Icons.Filled.Warning, null, tint = WarmRed) },
            title = { Text("Clear All Data") },
            text = { Text("This will permanently delete ALL renovation data including projects, tasks, rooms, and expenses. This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    authViewModel.clearAllData {}
                    showClearDataConfirm = false
                }) { Text("Clear Data", color = WarmRed, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showClearDataConfirm = false }) { Text("Cancel") } }
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Orange, modifier = Modifier.padding(start = 4.dp, top = 8.dp))
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(0.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))) {
        Column(modifier = Modifier.fillMaxWidth(), content = content)
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    val rowModifier = if (onClick != null) Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp) else Modifier.fillMaxWidth().padding(16.dp)
    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(iconColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Text(subtitle, fontSize = 12.sp, color = OnSurfaceVariant)
        }
        if (trailing != null) trailing()
        else if (onClick != null) Icon(Icons.Filled.ChevronRight, null, tint = OnSurfaceVariant, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun SelectionDialog(
    title: String,
    options: List<String>,
    selected: String,
    displayName: (String) -> String = { it },
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                options.forEach { option ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onSelect(option) }.padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(displayName(option), fontSize = 15.sp)
                        if (option == selected) Icon(Icons.Filled.Check, null, tint = Orange)
                    }
                }
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text("Cancel") }
            }
        }
    }
}
