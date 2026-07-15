package com.youtubeclone.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.youtubeclone.presentation.theme.AccentRed
import com.youtubeclone.presentation.theme.TextPrimary

@Composable
fun SubscribeButton(
    isSubscribed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSubscribed) Color(0xFF3D3D3D) else AccentRed,
        animationSpec = tween(durationMillis = 300),
        label = "subscribe_color"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSubscribed) TextPrimary else Color.White,
        animationSpec = tween(durationMillis = 300),
        label = "subscribe_text_color"
    )

    Text(
        text = if (isSubscribed) "Subscribed" else "Subscribe",
        color = textColor,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}
