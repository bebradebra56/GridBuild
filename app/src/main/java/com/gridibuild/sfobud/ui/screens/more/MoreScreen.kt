package com.gridibuild.sfobud.ui.screens.more

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.gridibuild.sfobud.ui.navigation.Screen
import com.gridibuild.sfobud.ui.theme.*

data class MoreMenuItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val route: String
)

val moreMenuItems = listOf(
    MoreMenuItem("Materials", "Track supplies", Icons.Filled.Construction, Orange, Screen.Materials.route),
    MoreMenuItem("Shopping", "Buy list", Icons.Filled.ShoppingCart, WarmRed, Screen.Shopping.route),
    MoreMenuItem("Measurements", "Room dimensions", Icons.Filled.Straighten, SaturatedBlue, Screen.Measurements.route),
    MoreMenuItem("Photos", "Progress shots", Icons.Filled.PhotoCamera, Turquoise, Screen.Photos.route),
    MoreMenuItem("Contacts", "Team & vendors", Icons.Filled.Contacts, ProgressGreen, Screen.Contacts.route),
    MoreMenuItem("Calendar", "Schedule", Icons.Filled.CalendarMonth, BrightYellow, Screen.Calendar.route),
    MoreMenuItem("Insights", "Analytics", Icons.Filled.TrendingUp, Color(0xFF9B59B6), Screen.Insights.route),
    MoreMenuItem("Notifications", "Alerts", Icons.Filled.Notifications, Orange, Screen.Notifications.route),
    MoreMenuItem("Settings", "Preferences", Icons.Filled.Settings, DarkBlueViolet, Screen.Settings.route)
)

@Composable
fun MoreScreen(navController: NavController) {

    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(Brush.linearGradient(listOf(DarkBlueViolet, Color(0xFF3D3F5A))))
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            Column {
                Text("More", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 28.sp)
                Text("All tools in one place", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(moreMenuItems) { item ->
                MoreMenuCard(item = item, onClick = { navController.navigate(item.route) })
            }
            item {
                MoreMenuCard(item = MoreMenuItem(
                    "Privacy",
                    "Policy",
                    Icons.Filled.PrivacyTip,
                    ProgressGreen,
                    Screen.Settings.route
                ), onClick = { val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://grridbuilld.com/privacy-policy.html"))
                    context.startActivity(intent) })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreMenuCard(item: MoreMenuItem, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = item.color.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, item.color.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(52.dp).clip(RoundedCornerShape(14.dp)).background(item.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(item.icon, contentDescription = item.title, tint = item.color, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(item.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
            Text(item.subtitle, fontSize = 10.sp, color = OnSurfaceVariant, maxLines = 1)
        }
    }
}
