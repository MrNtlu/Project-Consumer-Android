package com.mrntlu.projectconsumer.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun LoadingShimmer(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false,
) {
    Spacer(
        modifier = modifier
            .padding(4.dp)
            .fillMaxWidth()
            .aspectRatio(0.6666667f)
            .clip(RoundedCornerShape(12.dp))
            .background(GetShimmerBrush(isDarkTheme = isDarkTheme)),
    )
}

@Preview
@Composable
fun ShimmerPreview() {
    LoadingShimmer()
}