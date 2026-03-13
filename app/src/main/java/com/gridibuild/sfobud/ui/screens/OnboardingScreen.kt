package com.gridibuild.sfobud.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gridibuild.sfobud.ui.theme.*
import com.gridibuild.sfobud.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color
)

val onboardingPages = listOf(
    OnboardingPage(
        "Organize every room",
        "Create separate plans for each room and track progress clearly.",
        Icons.Filled.MeetingRoom,
        SaturatedBlue
    ),
    OnboardingPage(
        "Track tasks and materials",
        "Keep all tasks, materials, and purchases in one place.",
        Icons.Filled.Construction,
        Orange
    ),
    OnboardingPage(
        "Control budget easily",
        "See where your money goes and what still needs to be done.",
        Icons.Filled.AccountBalance,
        Turquoise
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val authViewModel: AuthViewModel = viewModel()
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val scope = rememberCoroutineScope()

    fun finish() {
        authViewModel.setOnboardingDone()
        onFinish()
    }

    Box(modifier = Modifier.padding(bottom = 32.dp).fillMaxSize().background(LightBackground)) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            OnboardingPageContent(page = onboardingPages[page])
        }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter).padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(onboardingPages.size) { idx ->
                    val isSelected = idx == pagerState.currentPage
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isSelected) Orange else GrayBeige)
                            .size(if (isSelected) 12.dp else 8.dp)
                    )
                }
            }
            Spacer(Modifier.height(32.dp))
            if (pagerState.currentPage < onboardingPages.size - 1) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(
                        onClick = { finish() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Skip")
                    }
                    Button(
                        onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Orange)
                    ) {
                        Text("Next", color = Color.White)
                    }
                }
            } else {
                Button(
                    onClick = { finish() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Orange)
                ) {
                    Text("Get Started", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(page.color.copy(alpha = 0.2f), page.color.copy(alpha = 0.05f)))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                page.icon,
                contentDescription = null,
                tint = page.color,
                modifier = Modifier.size(80.dp)
            )
        }
        Spacer(Modifier.height(48.dp))
        Text(
            page.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = DarkBlueViolet
        )
        Spacer(Modifier.height(16.dp))
        Text(
            page.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = OnSurfaceVariant
        )
        Spacer(Modifier.height(120.dp))
    }
}
