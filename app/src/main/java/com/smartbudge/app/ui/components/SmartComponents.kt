package com.smartbudge.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartbudge.app.ui.theme.PrimaryGradient

@Composable
fun PremiumButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: List<Color> = PrimaryGradient,
    isLoading: Boolean = false
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isPressed) 0.94f else 1f)

    Box(
        modifier = modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.horizontalGradient(colors))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    isPressed = true
                    onClick()
                }
            )
            .padding(vertical = 14.dp, horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

@Composable
fun MeshGradient(
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(Color(0xFF007AFF), Color(0xFF5856D6), Color(0xFFFF2D55))
) {
    Box(modifier = modifier.background(Color.Black)) {
        colors.forEachIndexed { index, color ->
            var offset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
            val xAnim by animateFloatAsState(
                targetValue = if (offset == androidx.compose.ui.geometry.Offset.Zero) 0f else 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 3000 + index * 500, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "x"
            )
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        translationX = (xAnim * 200 - 100).dp.value,
                        translationY = (index * 100).dp.value,
                        alpha = 0.4f,
                        scaleX = 2f,
                        scaleY = 2f
                    )
                    .background(
                        Brush.radialGradient(
                            listOf(color, Color.Transparent),
                            radius = 600f
                        )
                    )
            )
        }
    }
}

@Composable
fun PremiumCard(
    modifier: Modifier = Modifier,
    bgColor: Color = Color.Transparent,
    borderColor: Color = Color.Transparent,
    cornerRadius: Dp = 24.dp,
    elevation: Dp = 0.dp,
    padding: Dp = 24.dp,
    isGlass: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val finalBg = if (isGlass) bgColor.copy(alpha = 0.18f) else bgColor
    
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .then(
                if (isGlass) {
                    Modifier
                        .drawWithContent {
                            drawContent()
                        }
                        .border(
                            width = 1.2.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.5f),
                                    Color.White.copy(alpha = 0.1f),
                                    Color.White.copy(alpha = 0.2f)
                                )
                            ),
                            shape = RoundedCornerShape(cornerRadius)
                        )
                } else Modifier
            ),
        color = finalBg,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shadowElevation = if (isGlass) 0.dp else elevation
    ) {
        Column(
            modifier = Modifier.padding(padding)
        ) {
            content()
        }
    }
}

@Composable
fun GlowingBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val moveX1 = 0f
    val moveY2 = 0f

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(com.smartbudge.app.ui.theme.BackgroundDark)
    ) {
        // Soft static center glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF3A2154).copy(alpha = 0.5f), // Soft purple
                            Color.Transparent
                        ),
                        radius = 1200f
                    )
                )
        )
        // Main Content
        content()
    }
}
