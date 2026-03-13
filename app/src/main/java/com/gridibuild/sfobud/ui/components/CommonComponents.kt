package com.gridibuild.sfobud.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gridibuild.sfobud.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun formatMoney(amount: Double): String = formatCurrency(amount, LocalCurrency.current)

@Composable
fun currentUnits(): String = LocalUnits.current

@Composable
fun GradientCard(
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(Orange, BrightYellow),
    shape: RoundedCornerShape = RoundedCornerShape(20.dp),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(Brush.linearGradient(colors))
            .padding(20.dp),
        content = content
    )
}

@Composable
fun SectionHeader(
    title: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(actionLabel, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun StageChip(stage: String, modifier: Modifier = Modifier) {
    val (bg, fg) = when (stage) {
        "Planning" -> Color(0xFFE3F2FD) to SaturatedBlue
        "Preparing" -> Color(0xFFFFF8E1) to BrightYellow
        "In Progress" -> Color(0xFFFFF3E0) to Orange
        "Finishing" -> Color(0xFFE8F5E9) to ProgressGreen
        "Done" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        else -> GrayBeige to DarkBlueViolet
    }
    Surface(
        color = bg,
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
    ) {
        Text(
            text = stage,
            color = fg,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun PriorityChip(priority: String, modifier: Modifier = Modifier) {
    val (bg, fg, label) = when (priority) {
        "HIGH" -> Triple(Color(0xFFFFEBEE), WarmRed, "High")
        "MEDIUM" -> Triple(Color(0xFFFFF8E1), Orange, "Medium")
        "LOW" -> Triple(Color(0xFFE8F5E9), ProgressGreen, "Low")
        else -> Triple(GrayBeige, DarkBlueViolet, priority)
    }
    Surface(color = bg, shape = RoundedCornerShape(20.dp), modifier = modifier) {
        Text(label, color = fg, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
    }
}

@Composable
fun ColoredIconBox(
    icon: ImageVector,
    color: Color,
    size: Dp = 44.dp,
    iconSize: Dp = 22.dp
) {
    Box(
        modifier = Modifier.size(size).clip(RoundedCornerShape(12.dp)).background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(iconSize))
    }
}

@Composable
fun CircleProgress(
    progress: Float,
    color: Color,
    backgroundColor: Color = GrayBeige,
    size: Dp = 56.dp,
    strokeWidth: Dp = 6.dp,
    label: String = "${(progress * 100).toInt()}%"
) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(size)) {
        CircularProgressIndicator(
            progress = progress,
            modifier = Modifier.size(size),
            color = color,
            trackColor = backgroundColor,
            strokeWidth = strokeWidth
        )
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun EmptyState(
    icon: ImageVector = Icons.Filled.Inbox,
    title: String,
    subtitle: String = "",
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(80.dp).clip(CircleShape).background(GrayBeige),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(40.dp), tint = OnSurfaceVariant)
        }
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        if (subtitle.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(20.dp))
            Button(onClick = onAction, shape = RoundedCornerShape(12.dp)) {
                Text(actionLabel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        },
        actions = actions,
        windowInsets = WindowInsets(0),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

fun formatDate(timestamp: Long): String {
    return SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH).format(Date(timestamp))
}

fun formatCurrency(amount: Double, currency: String = "USD"): String {
    val symbol = when (currency) {
        "USD" -> "$"; "EUR" -> "€"; "GBP" -> "£"
        "RUB" -> "₽"; "UAH" -> "₴"; "PLN" -> "zł"
        "CZK" -> "Kč"; "KZT" -> "₸"
        else -> "$"
    }
    return if (amount >= 1000) "$symbol${String.format("%.0f", amount)}"
    else "$symbol${String.format("%.2f", amount)}"
}

fun parseColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        Orange
    }
}
