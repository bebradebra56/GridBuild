package com.gridibuild.sfobud.ui.screens.home

import androidx.compose.foundation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.gridibuild.sfobud.data.local.entity.TaskEntity
import com.gridibuild.sfobud.ui.components.*
import com.gridibuild.sfobud.ui.navigation.Screen
import com.gridibuild.sfobud.ui.theme.*
import com.gridibuild.sfobud.viewmodel.AuthViewModel
import com.gridibuild.sfobud.viewmodel.HomeViewModel

@Composable
fun HomeScreen(navController: NavController) {
    val homeVm: HomeViewModel = viewModel()
    val authVm: AuthViewModel = viewModel()
    val authState by authVm.state.collectAsState()
    val currentProject by homeVm.currentProject.collectAsState()
    val rooms by homeVm.rooms.collectAsState()
    val tasks by homeVm.tasks.collectAsState()
    val expenses by homeVm.expenses.collectAsState()
    val shoppingItems by homeVm.shoppingItems.collectAsState()

    val totalTasks = tasks.size
    val completedTasks = tasks.count { it.status == "DONE" }
    val totalSpent = expenses.filter { !it.isPlanned }.sumOf { it.amount }
    val completionPercent = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0f

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(Orange, BrightYellow)))
                    .padding(horizontal = 24.dp, vertical = 28.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Column {
                        Text("Hello, ${authState.userName} 👋", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
                        Text(
                            if (currentProject != null) currentProject!!.name else "No active project",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (currentProject != null) {
                            Spacer(Modifier.height(8.dp))
                            Text("${(completionPercent * 100).toInt()}% complete", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        CircleProgress(progress = completionPercent, color = Color.White, backgroundColor = Color.White.copy(alpha = 0.3f))
                    }
                }
            }
        }

        if (currentProject == null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SoftSectionBg),
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.Construction, null, tint = Orange, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("Start your renovation", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Create your first project to get started", color = OnSurfaceVariant, fontSize = 13.sp)
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { navController.navigate(Screen.Projects.route) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Orange)
                        ) {
                            Icon(Icons.Filled.Add, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Create Project")
                        }
                    }
                }
            }
        } else {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(modifier = Modifier.weight(1f), label = "Tasks Done", value = "$completedTasks / $totalTasks", color = ProgressGreen, icon = Icons.Filled.CheckCircle)
                    StatCard(modifier = Modifier.weight(1f), label = "Rooms", value = "${rooms.size}", color = SaturatedBlue, icon = Icons.Filled.MeetingRoom)
                    StatCard(modifier = Modifier.weight(1f), label = "Shopping", value = "${shoppingItems.size}", color = Orange, icon = Icons.Filled.ShoppingCart)
                }
            }

            if (rooms.isNotEmpty()) {
                item { SectionHeader("Rooms Overview", "See all") { navController.navigate(Screen.Rooms.route) } }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(rooms) { room ->
                            RoomOverviewCard(room = room, onClick = { navController.navigate(Screen.Rooms.route) })
                        }
                    }
                }
            }

            val todayTasks = tasks.filter { task ->
                task.dueDate != null && isSameDay(task.dueDate, System.currentTimeMillis())
            }
            if (todayTasks.isNotEmpty()) {
                item { SectionHeader("Today's Tasks", "All tasks") { navController.navigate(Screen.Tasks.route) } }
                items(todayTasks.take(3)) { task ->
                    TaskSummaryCard(task = task, modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp))
                }
            }

            item {
                SectionHeader("Budget Snapshot", "Details") { navController.navigate(Screen.Budget.route) }
                BudgetSnapshotCard(
                    totalBudget = currentProject!!.totalBudget,
                    totalSpent = totalSpent,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            item { Spacer(Modifier.height(16.dp)) }

            item {
                SectionHeader("Quick Actions", null, null)
                QuickActionsRow(navController = navController)
            }
        }
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, label: String, value: String, color: Color, icon: ImageVector) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = color)
            Text(label, fontSize = 11.sp, color = OnSurfaceVariant)
        }
    }
}

@Composable
fun RoomOverviewCard(room: com.gridibuild.sfobud.data.local.entity.RoomEntity, onClick: () -> Unit) {
    val roomColor = parseColor(room.colorHex)
    Card(
        onClick = onClick,
        modifier = Modifier.width(130.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = roomColor.copy(alpha = 0.12f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(roomColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.MeetingRoom, null, tint = roomColor, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(room.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1)
            Spacer(Modifier.height(4.dp))
            StageChip(room.stage)
        }
    }
}

@Composable
fun TaskSummaryCard(task: TaskEntity, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(10.dp).clip(CircleShape).background(
                    when (task.priority) { "HIGH" -> WarmRed; "MEDIUM" -> Orange; else -> ProgressGreen }
                )
            )
            Text(task.title, modifier = Modifier.weight(1f), fontSize = 14.sp, fontWeight = FontWeight.Medium)
            if (task.dueDate != null) {
                Text(formatDate(task.dueDate), fontSize = 11.sp, color = OnSurfaceVariant)
            }
        }
    }
}

@Composable
fun BudgetSnapshotCard(totalBudget: Double, totalSpent: Double, modifier: Modifier = Modifier) {
    val progress = if (totalBudget > 0) (totalSpent / totalBudget).coerceIn(0.0, 1.0) else 0.0
    val isOver = totalSpent > totalBudget && totalBudget > 0

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Spent", fontSize = 12.sp, color = OnSurfaceVariant)
                    Text(formatMoney(totalSpent), fontWeight = FontWeight.Bold, fontSize = 20.sp, color = if (isOver) WarmRed else DarkBlueViolet)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Budget", fontSize = 12.sp, color = OnSurfaceVariant)
                    Text(formatMoney(totalBudget), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            }
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = progress.toFloat(),
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                color = if (isOver) WarmRed else ProgressGreen,
                trackColor = GrayBeige
            )
            if (isOver) {
                Spacer(Modifier.height(8.dp))
                Text("⚠️ Over budget by ${formatMoney(totalSpent - totalBudget)}", color = WarmRed, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun QuickActionsRow(navController: NavController) {
    val actions = listOf(
        Triple("Rooms", Icons.Filled.MeetingRoom, Screen.Rooms.route),
        Triple("Shopping", Icons.Filled.ShoppingCart, Screen.Shopping.route),
        Triple("Materials", Icons.Filled.Construction, Screen.Materials.route),
        Triple("Contacts", Icons.Filled.Contacts, Screen.Contacts.route)
    )
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        actions.forEach { (label, icon, route) ->
            Card(
                onClick = { navController.navigate(route) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SoftSectionBg)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(icon, null, tint = Orange, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.height(6.dp))
                    Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

private fun isSameDay(ts1: Long, ts2: Long): Boolean {
    val cal1 = java.util.Calendar.getInstance().also { it.timeInMillis = ts1 }
    val cal2 = java.util.Calendar.getInstance().also { it.timeInMillis = ts2 }
    return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
            cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
}
