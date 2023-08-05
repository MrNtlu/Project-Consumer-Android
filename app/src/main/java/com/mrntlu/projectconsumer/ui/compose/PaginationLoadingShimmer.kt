package com.mrntlu.projectconsumer.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mrntlu.projectconsumer.utils.Constants

@Composable
fun PaginationLoadingShimmer(
    gridCount: Int,
    aspectRatio: Float = Constants.DEFAULT_RATIO,
    isDarkTheme: Boolean = false,
) {
    Column(
        modifier = Modifier
            .height(IntrinsicSize.Min)
            .fillMaxWidth(),
    ) {
        repeat(2) {
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
                        isDarkTheme = isDarkTheme,
                        aspectRatio = aspectRatio
                    ) {
                        fillMaxWidth()
                        padding(4.dp)
                    }
                }
            }
        }
    }
}