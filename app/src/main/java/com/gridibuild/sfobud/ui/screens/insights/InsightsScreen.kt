package com.gridibuild.sfobud.ui.screens.insights

import androidx.compose.foundation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.gridibuild.sfobud.ui.components.*
import com.gridibuild.sfobud.ui.theme.*
import com.gridibuild.sfobud.viewmodel.InsightsViewModel

@Composable
fun InsightsScreen(navController: NavController) {
    val vm: InsightsViewModel = viewModel()
    val summary by vm.summary.collectAsState()

    Scaffold(
        topBar = { AppTopBar("Insights", onBack = { navController.popBackStack() }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                        .background(Brush.linearGradient(listOf(SaturatedBlue, Turquoise)))
                        .padding(24.dp)
                ) {
                    Column {
                        Text("Project Overview", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                            InsightStat("Completion", "${summary.completionPercent}%", Color.White)
                            InsightStat("Tasks Done", "${summary.completedTasks}/${summary.totalTasks}", Color.White)
                            InsightStat("Overdue", "${summary.overdueTasks}", if (summary.overdueTasks > 0) BrightYellow else Color.White)
                        }
                        Spacer(Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress = summary.completionPercent / 100f,
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = ProgressGreen,
                            trackColor = Color.White.copy(alpha = 0.3f)
                        )
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    InsightCard(modifier = Modifier.weight(1f), icon = Icons.Filled.CheckCircle, label = "Completed Tasks", value = summary.completedTasks.toString(), color = ProgressGreen)
                    InsightCard(modifier = Modifier.weight(1f), icon = Icons.Filled.Warning, label = "Overdue Tasks", value = summary.overdueTasks.toString(), color = WarmRed)
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    InsightCard(modifier = Modifier.weight(1f), icon = Icons.Filled.AttachMoney, label = "Total Spent", value = formatMoney(summary.totalExpenses), color = Orange)
                    InsightCard(modifier = Modifier.weight(1f), icon = Icons.Filled.AccountBalance, label = "Total Budget", value = formatMoney(summary.totalBudget), color = SaturatedBlue)
                }
            }

            if (summary.expenseByCategory.isNotEmpty()) {
                item {
                    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(0.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Filled.PieChart, null, tint = Orange)
                                Text("Spending by Category", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Spacer(Modifier.height(16.dp))
                            val sorted = summary.expenseByCategory.entries.sortedByDescending { it.value }
                            val total = summary.totalExpenses
                            val barColors = listOf(Orange, SaturatedBlue, Turquoise, WarmRed, ProgressGreen, BrightYellow)
                            sorted.forEachIndexed { idx, (cat, amount) ->
                                val color = barColors[idx % barColors.size]
                                val progress = if (total > 0) (amount / total).toFloat().coerceIn(0f, 1f) else 0f
                                Column {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(cat, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                        Text(formatMoney(amount), fontSize = 13.sp, color = color, fontWeight = FontWeight.SemiBold)
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)), color = color, trackColor = GrayBeige)
                                    Spacer(Modifier.height(12.dp))
                                }
                            }
                        }
                    }
                }
            }

            if (summary.rooms.isNotEmpty()) {
                item {
                    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(0.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Filled.MeetingRoom, null, tint = Turquoise)
                                Text("Rooms Progress", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Spacer(Modifier.height(16.dp))
                            summary.rooms.forEach { room ->
                                val roomColor = parseColor(room.colorHex)
                                val stageIndex = listOf("Planning", "Preparing", "In Progress", "Finishing", "Done").indexOf(room.stage)
                                val progress = ((stageIndex + 1) / 5f).coerceIn(0f, 1f)
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text(room.name, modifier = Modifier.weight(1f), fontSize = 13.sp)
                                    LinearProgressIndicator(progress = progress, modifier = Modifier.weight(2f).height(6.dp).clip(RoundedCornerShape(3.dp)), color = roomColor, trackColor = GrayBeige)
                                    StageChip(room.stage)
                                }
                                Spacer(Modifier.height(10.dp))
                            }
                        }
                    }
                }
            }

            if (summary.totalBudget > 0) {
                item {
                    val remaining = summary.totalBudget - summary.totalExpenses
                    val budgetHealth = when {
                        remaining >= summary.totalBudget * 0.3 -> "On Track"
                        remaining >= 0 -> "Watch Spending"
                        else -> "Over Budget"
                    }
                    val healthColor = when (budgetHealth) { "On Track" -> ProgressGreen; "Watch Spending" -> Orange; else -> WarmRed }
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = healthColor.copy(alpha = 0.1f)),
                        border = BorderStroke(1.dp, healthColor.copy(alpha = 0.3f))
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Icon(
                                if (remaining >= 0) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown,
                                null, tint = healthColor, modifier = Modifier.size(32.dp)
                            )
                            Column {
                                Text("Budget Health: $budgetHealth", fontWeight = FontWeight.Bold, color = healthColor)
                                Text(
                                    if (remaining >= 0) "You have ${formatMoney(remaining)} remaining"
                                    else "You are ${formatMoney(-remaining)} over budget",
                                    fontSize = 13.sp, color = OnSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InsightStat(label: String, value: String, color: Color) {
    Column {
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
        Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}

@Composable
fun InsightCard(modifier: Modifier = Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, color: Color) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)), border = BorderStroke(1.dp, color.copy(alpha = 0.2f))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = color)
            Text(label, fontSize = 12.sp, color = OnSurfaceVariant)
        }
    }
}
