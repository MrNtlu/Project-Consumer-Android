package com.mrntlu.projectconsumer.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun PaginationLoadingShimmer(
    gridCount: Int,
    isDarkTheme: Boolean = false,
) {
    Row(
        modifier = Modifier
            .height(IntrinsicSize.Min)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(gridCount) {
            LoadingShimmer(
                modifier = Modifier.weight(1f),
                isDarkTheme = isDarkTheme
            )
        }
    }
}

@Preview
@Composable
fun PaginationLoadingShimmerPreview() {
    PaginationLoadingShimmer(gridCount = 2)
}