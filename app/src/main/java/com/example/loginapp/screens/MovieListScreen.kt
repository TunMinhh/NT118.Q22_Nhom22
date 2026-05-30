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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items as lazyRowItems
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
import androidx.compose.runtime.mutableStateListOf
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
    val selectedGenres = remember { mutableStateListOf<String>() }
    var selectedAgeRating by remember { mutableStateOf<String?>(null) }

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

    val genres = remember(movieList) {
        movieList
            .flatMap { it.genres }
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
    }
    val ageRatings = remember(movieList) {
        val priority = listOf("P", "K", "T13", "C13", "T16", "C16", "T18", "C18")
        movieList
            .map { it.ageRating.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .sortedWith(compareBy<String> {
                val index = priority.indexOf(it.uppercase(Locale.US))
                if (index == -1) Int.MAX_VALUE else index
            }.thenBy { it })
    }
    val filteredMovies = movieList.filter { movie ->
        val matchesGenre = selectedGenres.isEmpty() ||
            selectedGenres.all { selectedGenre ->
                movie.genres.any { it.equals(selectedGenre, ignoreCase = true) }
            }
        val matchesAgeRating = selectedAgeRating == null ||
            movie.ageRating.equals(selectedAgeRating, ignoreCase = true)
        matchesGenre && matchesAgeRating
    }
    val nowShowing = filteredMovies.filter { it.isShowing }
    val comingSoon = filteredMovies.filterNot { it.isShowing }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            MovieFilterPanel(
                genres = genres,
                ageRatings = ageRatings,
                selectedGenres = selectedGenres,
                selectedAgeRating = selectedAgeRating,
                onGenreToggle = { genre ->
                    if (selectedGenres.contains(genre)) {
                        selectedGenres.remove(genre)
                    } else {
                        selectedGenres.add(genre)
                    }
                },
                onAgeRatingSelected = { selectedAgeRating = it },
                onClearClick = {
                    selectedGenres.clear()
                    selectedAgeRating = null
                }
            )
        }

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
private fun MovieFilterPanel(
    genres: List<String>,
    ageRatings: List<String>,
    selectedGenres: List<String>,
    selectedAgeRating: String?,
    onGenreToggle: (String) -> Unit,
    onAgeRatingSelected: (String?) -> Unit,
    onClearClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Bộ lọc phim",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (selectedGenres.isNotEmpty() || selectedAgeRating != null) {
                Text(
                    text = "Xóa lọc",
                    color = AccentRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable(onClick = onClearClick)
                )
            }
        }

        FilterRow(
            title = "Thể loại",
            options = genres,
            selectedOptions = selectedGenres,
            onAllSelected = { selectedGenres.forEach { onGenreToggle(it) } },
            onOptionToggle = onGenreToggle
        )

        SingleChoiceFilterRow(
            title = "Độ tuổi",
            options = ageRatings,
            selectedOption = selectedAgeRating,
            onOptionSelected = onAgeRatingSelected
        )
    }
}

@Composable
private fun FilterRow(
    title: String,
    options: List<String>,
    selectedOptions: List<String>,
    onAllSelected: () -> Unit,
    onOptionToggle: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            color = Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChipText(
                    text = "Tất cả",
                    selected = selectedOptions.isEmpty(),
                    onClick = onAllSelected
                )
            }
            lazyRowItems(options, key = { it }) { option ->
                FilterChipText(
                    text = option,
                    selected = selectedOptions.contains(option),
                    onClick = { onOptionToggle(option) }
                )
            }
        }
    }
}

@Composable
private fun SingleChoiceFilterRow(
    title: String,
    options: List<String>,
    selectedOption: String?,
    onOptionSelected: (String?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            color = Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChipText(
                    text = "Tất cả",
                    selected = selectedOption == null,
                    onClick = { onOptionSelected(null) }
                )
            }
            lazyRowItems(options, key = { it }) { option ->
                FilterChipText(
                    text = option,
                    selected = option == selectedOption,
                    onClick = { onOptionSelected(option) }
                )
            }
        }
    }
}

@Composable
private fun FilterChipText(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (selected) AccentRed else Color(0xFFEFEFEF)
    val textColor = if (selected) Color.White else Color(0xFF333333)

    Text(
        text = text,
        color = textColor,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp)
    )
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


