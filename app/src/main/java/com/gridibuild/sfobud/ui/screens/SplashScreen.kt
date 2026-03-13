package com.gridibuild.sfobud.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gridibuild.sfobud.ui.theme.*
import com.gridibuild.sfobud.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigate: (isOnboardingDone: Boolean) -> Unit) {
    val authViewModel: AuthViewModel = viewModel()
    val state by authViewModel.state.collectAsState()

    var visible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.3f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(600),
        label = "alpha"
    )

    LaunchedEffect(Unit) {
        visible = true
        delay(2500)
        onNavigate(state.isOnboardingDone)
    }

    Box(
        modifier = Modifier.fillMaxSize().background(LightBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(scale)
        ) {
            BuildingTowerIllustration()
            Spacer(Modifier.height(32.dp))
            Text(
                "GridBuild",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = DarkBlueViolet,
                modifier = Modifier.scale(alpha)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Plan repairs step by step",
                fontSize = 16.sp,
                color = OnSurfaceVariant,
                modifier = Modifier.scale(alpha)
            )
        }
    }
}

@Composable
fun BuildingTowerIllustration(modifier: Modifier = Modifier) {
    val blocks = listOf(
        Triple(BrightYellow, 80.dp, 28.dp),
        Triple(Orange, 68.dp, 28.dp),
        Triple(Turquoise, 56.dp, 28.dp),
        Triple(SaturatedBlue, 44.dp, 28.dp),
        Triple(WarmRed, 32.dp, 24.dp)
    )
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        blocks.reversed().forEach { (color, width, height) ->
            Box(
                modifier = Modifier
                    .width(width)
                    .height(height)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color)
            )
        }
    }
}
