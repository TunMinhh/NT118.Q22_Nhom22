package com.example.loginapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.loginapp.auth.Movie
import com.example.loginapp.auth.getMoviesFromFirestore
import java.util.Locale

private val AccentRed = Color(0xFFE50914)

@Composable
fun MovieListScreen(onMovieClick: (String) -> Unit) {
    var movieList by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var hasLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        getMoviesFromFirestore {
            movieList = it
            hasLoaded = true
        }
    }

//    Cái này để test
//    LaunchedEffect(Unit) {
//        getMoviesFromFirestore { it ->
//            // Lấy danh sách 'it' trả về, trộn ngẫu nhiên và random trạng thái isShowing
//            movieList = it.shuffled().map { movie ->
//                movie.copy(isShowing = listOf(true, false).random())
//            }
//            hasLoaded = true
//        }
//    }


    if (!hasLoaded) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = AccentRed)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Đang tải danh sách phim...",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        return
    }

    if (movieList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "Không có phim để hiển thị",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        return
    }

    val nowShowing = movieList.filter { it.isShowing }
    val comingSoon = movieList.filterNot { it.isShowing }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            MovieSectionHeader(text = "Đang chiếu")
        }

        if (nowShowing.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                EmptySectionText(text = "Chưa có phim đang chiếu")
            }
        } else {
            items(nowShowing, key = { it.id.ifBlank { it.title } }) { movie ->
                MovieGridItem(movie = movie, onMovieClick = onMovieClick)
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            MovieSectionHeader(text = "Sắp chiếu", topPadding = true)
        }

        if (comingSoon.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                EmptySectionText(text = "Chưa có phim sắp chiếu")
            }
        } else {
            items(comingSoon, key = { it.id.ifBlank { it.title } }) { movie ->
                MovieGridItem(movie = movie, onMovieClick = onMovieClick)
            }
        }
    }
}

@Composable
private fun MovieSectionHeader(text: String, topPadding: Boolean = false) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = if (topPadding) 12.dp else 0.dp, bottom = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(3.dp)
                .background(AccentRed, RoundedCornerShape(2.dp))
        )
    }
}

@Composable
private fun EmptySectionText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = Color.Gray,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    )
}

@Composable
private fun MovieGridItem(movie: Movie, onMovieClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (movie.id.isNotEmpty()) onMovieClick(movie.id)
                else onMovieClick(movie.title)
            },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
            ) {
                AsyncImage(
                    model = movie.posterUrl,
                    contentDescription = movie.title,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentScale = ContentScale.Crop
                )

                if (movie.rating > 0.0) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .background(Color(0xCC000000), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = String.format(Locale.US, "%.1f", movie.rating),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (movie.ageRating.isNotBlank()) {
                    Text(
                        text = movie.ageRating,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(6.dp)
                            .background(AccentRed, RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )

                if (movie.genres.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = movie.genres.joinToString(" • "),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
