package com.gridibuild.sfobud.ui.screens.notifications

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.gridibuild.sfobud.ui.components.AppTopBar
import com.gridibuild.sfobud.ui.theme.*

data class MockNotification(
    val id: Int,
    val type: String,
    val title: String,
    val message: String,
    val time: String,
    val icon: ImageVector,
    val color: Color,
    var isRead: Boolean = false
)

val mockNotifications = listOf(
    MockNotification(1, "deadline", "Task Due Tomorrow", "\"Install flooring\" is due tomorrow. Don't forget to complete it!", "2h ago", Icons.Filled.Schedule, Orange),
    MockNotification(2, "delivery", "Delivery Today", "Your tiles order is scheduled for delivery today between 10:00-14:00.", "3h ago", Icons.Filled.LocalShipping, SaturatedBlue),
    MockNotification(3, "budget", "Budget Alert", "You've reached 80% of your total budget. Review expenses.", "5h ago", Icons.Filled.AttachMoney, WarmRed, true),
    MockNotification(4, "material", "Material Pending", "\"Wall paint\" hasn't been purchased yet and tasks depend on it.", "1d ago", Icons.Filled.Construction, BrightYellow, true),
    MockNotification(5, "overdue", "Task Overdue", "\"Electrical wiring\" is 2 days overdue. Mark it complete or reschedule.", "1d ago", Icons.Filled.Warning, WarmRed),
    MockNotification(6, "deadline", "Weekly Review", "You have 5 tasks due this week. Keep up the great work!", "2d ago", Icons.Filled.CheckCircle, ProgressGreen, true),
    MockNotification(7, "delivery", "Material Ordered", "Reminder: Follow up with the supplier about your lumber order.", "3d ago", Icons.Filled.Inventory, Turquoise, true)
)

@Composable
fun NotificationsScreen(navController: NavController) {
    var notifications by remember { mutableStateOf(mockNotifications.toMutableList()) }
    val unreadCount = notifications.count { !it.isRead }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Notifications",
                onBack = { navController.popBackStack() },
                actions = {
                    if (unreadCount > 0) {
                        TextButton(onClick = {
                            notifications = notifications.map { it.copy(isRead = true) }.toMutableList()
                        }) {
                            Text("Mark all read", color = Orange, fontSize = 13.sp)
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (notifications.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Notifications, null, modifier = Modifier.size(64.dp), tint = GrayBeige)
                    Spacer(Modifier.height(16.dp))
                    Text("No notifications", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                    Text("You're all caught up!", color = OnSurfaceVariant)
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                if (unreadCount > 0) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Orange.copy(alpha = 0.1f)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.Notifications, null, tint = Orange, modifier = Modifier.size(18.dp))
                            Text("$unreadCount unread notification${if (unreadCount > 1) "s" else ""}", fontSize = 14.sp, color = Orange, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(notifications, key = { it.id }) { notification ->
                        NotificationCard(
                            notification = notification,
                            onRead = {
                                notifications = notifications.map {
                                    if (it.id == notification.id) it.copy(isRead = true) else it
                                }.toMutableList()
                            },
                            onDismiss = {
                                notifications = notifications.filter { it.id != notification.id }.toMutableList()
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCard(
    notification: MockNotification,
    onRead: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        onClick = { if (!notification.isRead) onRead() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) MaterialTheme.colorScheme.surfaceVariant else notification.color.copy(alpha = 0.05f)
        ),
        elevation = CardDefaults.cardElevation(0.dp),
        border = if (!notification.isRead) BorderStroke(1.dp, notification.color.copy(alpha = 0.3f)) else null
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape)
                    .background(notification.color.copy(alpha = if (notification.isRead) 0.1f else 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(notification.icon, null, tint = notification.color.copy(alpha = if (notification.isRead) 0.5f else 1f), modifier = Modifier.size(22.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        notification.title,
                        fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (notification.isRead) OnSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    if (!notification.isRead) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(notification.color))
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(notification.message, fontSize = 13.sp, color = if (notification.isRead) OnSurfaceVariant.copy(alpha = 0.7f) else OnSurfaceVariant, maxLines = 2)
                Spacer(Modifier.height(6.dp))
                Text(notification.time, fontSize = 11.sp, color = OnSurfaceVariant.copy(alpha = 0.6f))
            }
            IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Filled.Close, null, modifier = Modifier.size(16.dp), tint = OnSurfaceVariant.copy(alpha = 0.5f))
            }
        }
    }
}
