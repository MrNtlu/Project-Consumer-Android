package com.mrntlu.projectconsumer.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun LoadingShimmer(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false,
    roundedCornerSize: Dp = 8.dp,
    aspectRatio: Float = 0.6666667f,
    sizeModifier: Modifier.() -> Modifier = { modifier.fillMaxWidth() },
) {
    Spacer(
        modifier = modifier
            .sizeModifier()
            .aspectRatio(aspectRatio)
            .clip(RoundedCornerShape(roundedCornerSize))
            .background(getShimmerBrush(isDarkTheme = isDarkTheme)),
    )
}