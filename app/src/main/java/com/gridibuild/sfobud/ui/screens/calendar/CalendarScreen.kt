package com.gridibuild.sfobud.ui.screens.calendar

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.gridibuild.sfobud.data.local.entity.TaskEntity
import com.gridibuild.sfobud.ui.components.*
import com.gridibuild.sfobud.ui.theme.*
import com.gridibuild.sfobud.viewmodel.TasksViewModel
import java.util.*

@Composable
fun CalendarScreen(navController: NavController) {
    val vm: TasksViewModel = viewModel()
    val allTasks by vm.tasks.collectAsState()

    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDay by remember { mutableStateOf<Int?>(null) }

    val calendar = currentMonth.clone() as Calendar
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) - 1).let { if (it == 0) 7 else it }
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    val tasksByDay = allTasks.filter { it.dueDate != null }.groupBy { task ->
        val cal = Calendar.getInstance()
        cal.timeInMillis = task.dueDate!!
        if (cal.get(Calendar.MONTH) == currentMonth.get(Calendar.MONTH) && cal.get(Calendar.YEAR) == currentMonth.get(Calendar.YEAR))
            cal.get(Calendar.DAY_OF_MONTH) else null
    }.filterKeys { it != null }.mapKeys { it.key!! }

    val selectedTasks = selectedDay?.let { tasksByDay[it] } ?: emptyList()

    Scaffold(
        topBar = { AppTopBar("Calendar", onBack = { navController.popBackStack() }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(0.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            val prev = currentMonth.clone() as Calendar
                            prev.add(Calendar.MONTH, -1)
                            currentMonth = prev
                            selectedDay = null
                        }) {
                            Icon(Icons.Filled.ChevronLeft, null, tint = Orange)
                        }
                        Text(
                            "${getMonthName(currentMonth.get(Calendar.MONTH))} ${currentMonth.get(Calendar.YEAR)}",
                            fontWeight = FontWeight.Bold, fontSize = 18.sp
                        )
                        IconButton(onClick = {
                            val next = currentMonth.clone() as Calendar
                            next.add(Calendar.MONTH, 1)
                            currentMonth = next
                            selectedDay = null
                        }) {
                            Icon(Icons.Filled.ChevronRight, null, tint = Orange)
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                            Text(day, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 12.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Medium)
                        }
                    }
                    Spacer(Modifier.height(8.dp))

                    val today = Calendar.getInstance()
                    val cells = (1 - firstDayOfWeek + 1)..(daysInMonth)
                    val rows = ((cells.count() + firstDayOfWeek - 1) / 7) + 1

                    for (week in 0 until rows) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            for (dayOfWeek in 1..7) {
                                val dayNum = week * 7 + dayOfWeek - (firstDayOfWeek - 1)
                                if (dayNum < 1 || dayNum > daysInMonth) {
                                    Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                                } else {
                                    val hasTasks = tasksByDay.containsKey(dayNum)
                                    val isToday = dayNum == today.get(Calendar.DAY_OF_MONTH) &&
                                            currentMonth.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                                            currentMonth.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                                    val isSelected = dayNum == selectedDay

                                    Box(
                                        modifier = Modifier.weight(1f).aspectRatio(1f)
                                            .padding(2.dp)
                                            .clip(CircleShape)
                                            .background(when { isSelected -> Orange; isToday -> Orange.copy(alpha = 0.15f); else -> Color.Transparent })
                                            .clickable { selectedDay = if (selectedDay == dayNum) null else dayNum },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                dayNum.toString(),
                                                fontSize = 13.sp,
                                                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = when { isSelected -> Color.White; isToday -> Orange; else -> MaterialTheme.colorScheme.onSurface }
                                            )
                                            if (hasTasks) {
                                                Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(if (isSelected) Color.White else WarmRed))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (selectedDay != null) {
                Text(
                    "${getMonthName(currentMonth.get(Calendar.MONTH))} $selectedDay — ${selectedTasks.size} task(s)",
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
                if (selectedTasks.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No tasks on this day", color = OnSurfaceVariant)
                    }
                } else {
                    LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(selectedTasks) { task ->
                            CalendarTaskItem(task = task)
                        }
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                    Text("Upcoming Tasks", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(12.dp))
                    val upcoming = allTasks.filter { it.dueDate != null && it.dueDate > System.currentTimeMillis() && it.status != "DONE" }.sortedBy { it.dueDate }.take(5)
                    if (upcoming.isEmpty()) {
                        Text("No upcoming tasks", color = OnSurfaceVariant, fontSize = 14.sp)
                    } else {
                        upcoming.forEach { task ->
                            CalendarTaskItem(task = task, modifier = Modifier.padding(bottom = 8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarTaskItem(task: TaskEntity, modifier: Modifier = Modifier) {
    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(0.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)), modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(when (task.priority) { "HIGH" -> WarmRed; "MEDIUM" -> Orange; else -> ProgressGreen }))
            Column(modifier = Modifier.weight(1f)) {
                Text(task.title, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                if (task.dueDate != null) Text(formatDate(task.dueDate), fontSize = 12.sp, color = OnSurfaceVariant)
            }
            PriorityChip(task.priority)
        }
    }
}

private fun getMonthName(month: Int): String {
    return listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")[month]
}
