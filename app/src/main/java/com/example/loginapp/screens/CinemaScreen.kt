package com.example.loginapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.loginapp.auth.Cinema
import com.example.loginapp.auth.Movie
import com.example.loginapp.auth.Showtime
import com.example.loginapp.auth.getCinemasFromFirestore
import com.example.loginapp.auth.getMoviesFromFirestore
import com.example.loginapp.auth.getShowtimesByCinemaId
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CinemaScreen(
    onShowtimeClick: (movieId: String, showtimeId: String) -> Unit = { _, _ -> }
) {
    var cinemaList by remember { mutableStateOf<List<Cinema>>(emptyList()) }
    var movieList by remember { mutableStateOf<List<Movie>>(emptyList()) }
    val showtimesByCinema = remember { mutableStateMapOf<String, List<Showtime>>() }
    val loadingShowtimes = remember { mutableStateMapOf<String, Boolean>() }
    var expandedCinemaId by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        getCinemasFromFirestore { cinemas ->
            cinemaList = cinemas
            getMoviesFromFirestore { movies ->
                movieList = movies
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        Text(
            text = "Danh sách rạp",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp, top = 8.dp)
        )

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFE50914))
                }
            }
            cinemaList.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Chưa có dữ liệu rạp chiếu.")
                }
            }
            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(cinemaList, key = { it.id.ifBlank { it.name } }) { cinema ->
                        val isExpanded = expandedCinemaId == cinema.id
                        CinemaItem(
                            cinema = cinema,
                            isExpanded = isExpanded,
                            showtimes = showtimesByCinema[cinema.id].orEmpty(),
                            movies = movieList,
                            isLoadingShowtimes = loadingShowtimes[cinema.id] == true,
                            onClick = {
                                expandedCinemaId = if (isExpanded) null else cinema.id
                                if (!isExpanded && showtimesByCinema[cinema.id] == null) {
                                    loadingShowtimes[cinema.id] = true
                                    getShowtimesByCinemaId(cinema.id) { showtimes ->
                                        showtimesByCinema[cinema.id] = showtimes
                                        loadingShowtimes[cinema.id] = false
                                    }
                                }
                            },
                            onShowtimeClick = onShowtimeClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CinemaItem(
    cinema: Cinema,
    isExpanded: Boolean,
    showtimes: List<Showtime>,
    movies: List<Movie>,
    isLoadingShowtimes: Boolean,
    onClick: () -> Unit,
    onShowtimeClick: (movieId: String, showtimeId: String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = cinema.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = cinema.address,
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )
                }

                Text(
                    text = if (isExpanded) "Thu gọn" else "Lịch chiếu",
                    color = Color(0xFFE50914),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (cinema.facilities.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(cinema.facilities) { facility ->
                        FacilityChip(facility)
                    }
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(14.dp))
                CinemaShowtimeContent(
                    showtimes = showtimes,
                    movies = movies,
                    isLoading = isLoadingShowtimes,
                    onShowtimeClick = onShowtimeClick
                )
            }
        }
    }
}

@Composable
private fun FacilityChip(facility: String) {
    Box(
        modifier = Modifier
            .background(color = Color(0xFFE0F7FA), shape = RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = facility,
            fontSize = 12.sp,
            color = Color(0xFF006064),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun CinemaShowtimeContent(
    showtimes: List<Showtime>,
    movies: List<Movie>,
    isLoading: Boolean,
    onShowtimeClick: (movieId: String, showtimeId: String) -> Unit
) {
    when {
        isLoading -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFE50914))
            }
        }
        showtimes.isEmpty() -> {
            Text(
                text = "Rạp này chưa có lịch chiếu.",
                color = Color.Gray,
                fontSize = 13.sp
            )
        }
        else -> {
            val showtimesByMovie = showtimes
                .sortedBy { it.startTime }
                .groupBy { it.movieId }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                showtimesByMovie.forEach { (movieId, movieShowtimes) ->
                    val movie = movies.firstOrNull { it.id == movieId || it.title == movieId }
                    CinemaMovieSchedule(
                        movieTitle = movie?.title ?: movieId,
                        movieId = movie?.id?.ifBlank { movieId } ?: movieId,
                        showtimes = movieShowtimes,
                        onShowtimeClick = onShowtimeClick
                    )
                }
            }
        }
    }
}

@Composable
private fun CinemaMovieSchedule(
    movieTitle: String,
    movieId: String,
    showtimes: List<Showtime>,
    onShowtimeClick: (movieId: String, showtimeId: String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = movieTitle,
            color = Color.Black,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        val showtimesByDate = showtimes.groupBy { showtimeDateKey(it.startTime) }
        showtimesByDate.forEach { (date, dateShowtimes) ->
            Text(
                text = formatShowtimeDate(date),
                color = Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(dateShowtimes, key = { it.id }) { showtime ->
                    ShowtimeChip(
                        showtime = showtime,
                        onClick = { onShowtimeClick(movieId, showtime.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ShowtimeChip(showtime: Showtime, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF2A2E38))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = formatHour(showtime.startTime),
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = formatPrice(showtime.price),
            color = Color(0xFFCCCCCC),
            fontSize = 11.sp
        )
    }
}

private fun showtimeDateKey(value: String): String = value.take(10)

private fun formatShowtimeDate(value: String): String {
    return try {
        val date = LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE)
        val vi = Locale("vi", "VN")
        val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.FULL, vi)
        val day = String.format(Locale.US, "%02d", date.dayOfMonth)
        val month = String.format(Locale.US, "%02d", date.monthValue)
        "$dayOfWeek, $day/$month"
    } catch (_: Exception) {
        value
    }
}

private fun formatHour(value: String): String {
    return value.replace("T", " ").replace("Z", "").drop(11).take(5).ifBlank { value }
}

private fun formatPrice(value: Long): String {
    return String.format(Locale.US, "%,d VND", value)
}
