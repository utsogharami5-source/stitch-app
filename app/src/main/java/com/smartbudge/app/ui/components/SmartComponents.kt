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
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = Brush.horizontalGradient(colors),
                alpha = 0.95f // Slightly more opaque matte
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.2f), Color.Transparent)
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    isPressed = true
                    onClick()
                }
            )
            .padding(horizontal = 16.dp), // Removed fixed vertical padding to allow height to govern
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
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
            val infiniteTransition = rememberInfiniteTransition(label = "mesh")
            val xOffset by infiniteTransition.animateFloat(
                initialValue = -100f,
                targetValue = 100f,
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
                        translationX = xOffset.dp.value,
                        translationY = (index * 120).dp.value,
                        alpha = 0.3f,
                        scaleX = 2.5f,
                        scaleY = 2.5f
                    )
                    .background(
                        Brush.radialGradient(
                            listOf(color, Color.Transparent),
                            radius = 700f
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
    // Matte glass effect: more opaque background + diffuse border
    val finalBg = if (isGlass) bgColor.copy(alpha = 0.22f) else bgColor
    
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .then(
                if (isGlass) {
                    Modifier
                        .border(
                            width = 1.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.25f),
                                    Color.White.copy(alpha = 0.05f),
                                    Color.White.copy(alpha = 0.15f)
                                )
                            ),
                            shape = RoundedCornerShape(cornerRadius)
                        )
                } else Modifier
            ),
        color = finalBg,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shadowElevation = if (isGlass) 2.dp else elevation // Slight elevation for matte depth
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
        // Main Content
        content()
    }
}
