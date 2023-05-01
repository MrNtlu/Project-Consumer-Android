package com.mrntlu.projectconsumer.ui.compose

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.models.common.GenreUI
import com.mrntlu.projectconsumer.utils.Constants

@Composable
fun GenreGrid(
    isDarkTheme: Boolean = false,
    genreList: List<GenreUI>,
    onDiscoveryClicked: () -> Unit,
    onGenreClicked: (String) -> Unit,
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    val cellHeight = 100
    val padding = 3
    var gridColumnCount = calculateGridColumnCount(configuration)
    var gridHeight = calculateGridLayoutHeight(genreList.size, gridColumnCount, cellHeight, padding)

    LaunchedEffect(key1 = configuration) {
        gridColumnCount = calculateGridColumnCount(configuration)
        gridHeight = calculateGridLayoutHeight(genreList.size, gridColumnCount, cellHeight, padding)
    }

    LazyVerticalGrid(
        modifier = Modifier
            .height(gridHeight.dp),
        columns = GridCells.Fixed(gridColumnCount),
        userScrollEnabled = false,
    ) {
        items(
            genreList,
            key = {
                it.genre
            }
        ) { genre ->
            Box(
                modifier = Modifier
                    .padding(padding.dp)
                    .fillMaxSize()
                    .height(cellHeight.dp)
                    .clickable {
                        if (genre.genre == "Discover") {
                            onDiscoveryClicked()
                        } else {
                            onGenreClicked(genre.genre)
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp)),
                    model = ImageRequest.Builder(context)
                        .data(genre.image)
                        .build(),
                    contentScale = ContentScale.Crop,
                    contentDescription = stringResource(id = R.string.genre_image_cd)
                )

                Box(
                    modifier = Modifier
                        .background(
                            (if (isDarkTheme)
                                Color.White
                            else
                                Color.Black).copy(alpha = 0.65f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = genre.genre,
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            fontSize = 22.sp,
                            color = if (isDarkTheme) Color(0xFF212121) else Color.White,
                            fontWeight = FontWeight.Bold,
                        )
                    )
                }
            }

        }
    }
}

private fun calculateGridColumnCount(configuration: Configuration) = if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 3 else 2

private fun calculateGridLayoutHeight(size: Int, gridColumnCount: Int, cellHeight: Int, padding: Int) = size.div(
    gridColumnCount
).plus(
    if (size.rem(gridColumnCount) > 0) 1 else 0
).times(
    cellHeight.plus(padding.times(2))
).plus(
    12
)

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Preview
@Composable
fun GenreGridPreview() {
    MaterialTheme {
        Scaffold {
            GenreGrid(false, Constants.MovieGenreList, {}, {})
        }
    }
}