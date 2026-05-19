package com.example.loginapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Theaters
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.loginapp.auth.Cinema
import com.example.loginapp.auth.Movie
import com.example.loginapp.auth.Room
import com.example.loginapp.auth.Showtime
import com.example.loginapp.auth.createTicket
import com.example.loginapp.auth.getCinemasFromFirestore
import com.example.loginapp.auth.getMoviesFromFirestore
import com.example.loginapp.auth.getRoomById
import com.example.loginapp.auth.getShowtimeById
import com.example.loginapp.auth.getShowtimesByMovieId
import com.example.loginapp.ui.theme.LoginAppTheme
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val BookingBg = Color(0xFF111318)
private val BookingCard = Color(0xFF1B1E25)
private val BookingCardAlt = Color(0xFF2A2E38)
private val BookingRed = Color(0xFFE50914)
private val BookingMuted = Color(0xFFA0A0A0)

@Composable
fun BookingCinemaScreen(
    movieId: String,
    onBackClick: () -> Unit,
    onShowtimeClick: (String) -> Unit
) {
    var cinemas by remember { mutableStateOf<List<Cinema>>(emptyList()) }
    var showtimes by remember { mutableStateOf<List<Showtime>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(movieId) {
        isLoading = true
        getShowtimesByMovieId(movieId) { showtimeResult ->
            showtimes = showtimeResult
            getCinemasFromFirestore { cinemaResult ->
                cinemas = cinemaResult
                isLoading = false
            }
        }
    }

    BookingCinemaContent(
        cinemas = cinemas,
        showtimes = showtimes,
        isLoading = isLoading,
        onBackClick = onBackClick,
        onShowtimeClick = onShowtimeClick
    )
}

@Composable
fun BookingSeatScreen(
    showtimeId: String,
    onBackClick: () -> Unit,
    onContinueClick: (String) -> Unit
) {
    var showtime by remember { mutableStateOf<Showtime?>(null) }
    var room by remember { mutableStateOf<Room?>(null) }
    var cinema by remember { mutableStateOf<Cinema?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val selectedSeats = remember { mutableStateListOf<String>() }

    LaunchedEffect(showtimeId) {
        isLoading = true
        selectedSeats.clear()
        getShowtimeById(showtimeId) { showtimeResult ->
            showtime = showtimeResult
            if (showtimeResult == null) {
                isLoading = false
                return@getShowtimeById
            }

            getRoomById(showtimeResult.roomId) { roomResult ->
                room = roomResult
                getCinemasFromFirestore { cinemaResult ->
                    cinema = cinemaResult.firstOrNull { it.id == showtimeResult.cinemaId }
                    isLoading = false
                }
            }
        }
    }

    BookingSeatContent(
        showtime = showtime,
        room = room,
        cinema = cinema,
        selectedSeats = selectedSeats,
        isLoading = isLoading,
        onBackClick = onBackClick,
        onSeatToggle = { seat ->
            if (selectedSeats.contains(seat)) selectedSeats.remove(seat) else selectedSeats.add(seat)
        },
        onContinueClick = {
            onContinueClick(selectedSeats.joinToString(","))
        }
    )
}

@Composable
fun BookingTicketScreen(
    movieId: String,
    showtimeId: String,
    seatsText: String,
    onBackClick: () -> Unit,
    onDoneClick: () -> Unit
) {
    val seats = remember(seatsText) {
        seatsText.split(",").map { it.trim() }.filter { it.isNotBlank() }
    }
    var movie by remember { mutableStateOf<Movie?>(null) }
    var showtime by remember { mutableStateOf<Showtime?>(null) }
    var room by remember { mutableStateOf<Room?>(null) }
    var cinema by remember { mutableStateOf<Cinema?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var successTicketId by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(movieId, showtimeId) {
        isLoading = true
        getMoviesFromFirestore { movies ->
            movie = movies.firstOrNull { it.id == movieId || it.title == movieId }
            getShowtimeById(showtimeId) { showtimeResult ->
                showtime = showtimeResult
                if (showtimeResult == null) {
                    isLoading = false
                    return@getShowtimeById
                }

                getRoomById(showtimeResult.roomId) { roomResult ->
                    room = roomResult
                    getCinemasFromFirestore { cinemaResult ->
                        cinema = cinemaResult.firstOrNull { it.id == showtimeResult.cinemaId }
                        isLoading = false
                    }
                }
            }
        }
    }

    BookingTicketContent(
        movie = movie,
        showtime = showtime,
        cinema = cinema,
        room = room,
        seats = seats,
        isLoading = isLoading,
        isSaving = isSaving,
        successTicketId = successTicketId,
        errorMessage = errorMessage,
        onBackClick = onBackClick,
        onDoneClick = onDoneClick,
        onConfirmClick = {
            val currentShowtime = showtime
            val currentMovie = movie
            if (currentShowtime == null || currentMovie == null) {
                errorMessage = "Không tìm thấy thông tin đặt vé."
            } else {
                isSaving = true
                errorMessage = null
                createTicket(
                    cinemaName = cinema?.name ?: currentShowtime.cinemaId,
                    movieTitle = currentMovie.title,
                    seats = seats,
                    showtimeId = currentShowtime.id,
                    totalPrice = currentShowtime.price * seats.size,
                    userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest",
                    onSuccess = { ticketId ->
                        isSaving = false
                        successTicketId = ticketId
                    },
                    onFailure = { exception ->
                        isSaving = false
                        errorMessage = exception.localizedMessage ?: "Không thể tạo vé. Vui lòng thử lại."
                    }
                )
            }
        }
    )
}

@Composable
private fun BookingCinemaContent(
    cinemas: List<Cinema>,
    showtimes: List<Showtime>,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onShowtimeClick: (String) -> Unit
) {
    BookingScaffold(title = "Chọn rạp", onBackClick = onBackClick) {
        when {
            isLoading -> LoadingBox()
            showtimes.isEmpty() -> EmptyText("Phim này chưa có lịch chiếu.")
            else -> {
                val showtimesByCinema = showtimes.groupBy { it.cinemaId }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    showtimesByCinema.forEach { (cinemaId, cinemaShowtimes) ->
                        item(key = cinemaId) {
                            val cinema = cinemas.firstOrNull { it.id == cinemaId }
                                ?: Cinema(id = cinemaId, name = cinemaId)
                            CinemaShowtimeCard(
                                cinema = cinema,
                                showtimes = cinemaShowtimes,
                                onShowtimeClick = onShowtimeClick
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BookingSeatContent(
    showtime: Showtime?,
    room: Room?,
    cinema: Cinema?,
    selectedSeats: List<String>,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onSeatToggle: (String) -> Unit,
    onContinueClick: () -> Unit
) {
    BookingScaffold(title = "Chọn ghế", onBackClick = onBackClick) {
        when {
            isLoading -> LoadingBox()
            showtime == null -> EmptyText("Không tìm thấy lịch chiếu.")
            else -> Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        BookingInfoCard(
                            title = cinema?.name ?: showtime.cinemaId,
                            lines = listOf(
                                "Phòng: ${room?.name ?: showtime.roomId}",
                                "Loại phòng: ${room?.type ?: "Đang cập nhật"}",
                                "Suất chiếu: ${formatDateTime(showtime.startTime)}",
                                "Giá vé: ${formatPrice(showtime.price)}"
                            )
                        )
                    }
                    item {
                        SeatMapCard(
                            room = room,
                            bookedSeats = showtime.bookedSeats,
                            selectedSeats = selectedSeats,
                            onSeatToggle = onSeatToggle
                        )
                    }
                }

                BottomActionBar(
                    title = "${selectedSeats.size} ghế đã chọn",
                    subtitle = "Tổng tiền: ${formatPrice(showtime.price * selectedSeats.size)}",
                    buttonText = "Tiếp tục",
                    enabled = selectedSeats.isNotEmpty(),
                    onClick = onContinueClick
                )
            }
        }
    }
}

@Composable
private fun BookingTicketContent(
    movie: Movie?,
    showtime: Showtime?,
    cinema: Cinema?,
    room: Room?,
    seats: List<String>,
    isLoading: Boolean,
    isSaving: Boolean,
    successTicketId: String?,
    errorMessage: String?,
    onBackClick: () -> Unit,
    onDoneClick: () -> Unit,
    onConfirmClick: () -> Unit
) {
    BookingScaffold(title = "Xác nhận vé", onBackClick = onBackClick) {
        when {
            isLoading -> LoadingBox()
            movie == null || showtime == null -> EmptyText("Không tìm thấy thông tin đặt vé.")
            else -> Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        TicketSummaryCard(
                            movieTitle = movie.title,
                            cinemaName = cinema?.name ?: showtime.cinemaId,
                            roomName = room?.name ?: showtime.roomId,
                            showtime = showtime.startTime,
                            seats = seats,
                            totalPrice = showtime.price * seats.size,
                            successTicketId = successTicketId
                        )
                    }

                    if (!errorMessage.isNullOrBlank()) {
                        item {
                            Text(
                                text = errorMessage,
                                color = Color(0xFFFFB4AB),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }
                }

                BottomActionBar(
                    title = if (successTicketId == null) "Thanh toán mô phỏng" else "Đặt vé thành công",
                    subtitle = if (successTicketId == null) "ZaloPay Simulation" else "Mã vé: $successTicketId",
                    buttonText = if (successTicketId == null) "Xác nhận đặt vé" else "Hoàn tất",
                    enabled = !isSaving,
                    onClick = if (successTicketId == null) onConfirmClick else onDoneClick
                )
            }
        }
    }
}

@Composable
private fun BookingScaffold(
    title: String,
    onBackClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = BookingBg) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 42.dp, start = 12.dp, end = 18.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Quay lại", tint = Color.White)
                }
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
            content()
        }
    }
}

@Composable
private fun CinemaShowtimeCard(
    cinema: Cinema,
    showtimes: List<Showtime>,
    onShowtimeClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BookingCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = BookingRed)
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(cinema.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = cinema.address,
                        color = BookingMuted,
                        fontSize = 13.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (cinema.facilities.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(cinema.facilities) { facility ->
                        FacilityChip(text = facility)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text("Lịch chiếu", color = BookingMuted, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(10.dp))

            val showtimesByDate = showtimes
                .sortedBy { it.startTime }
                .groupBy { showtimeDateKey(it.startTime) }

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                showtimesByDate.forEach { (date, dateShowtimes) ->
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = formatShowtimeDate(date),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(dateShowtimes, key = { it.id }) { showtime ->
                                ShowtimeChip(showtime = showtime, onClick = { onShowtimeClick(showtime.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FacilityChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF29313C))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(text = text, color = Color.White, fontSize = 12.sp)
    }
}

@Composable
private fun ShowtimeChip(showtime: Showtime, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(BookingCardAlt)
            .border(1.dp, BookingRed.copy(alpha = 0.65f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(formatHour(showtime.startTime), color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Text(formatPrice(showtime.price), color = BookingMuted, fontSize = 11.sp)
    }
}

@Composable
private fun BookingInfoCard(title: String, lines: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BookingCard)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            lines.forEach { line ->
                Text(line, color = BookingMuted, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun SeatMapCard(
    room: Room?,
    bookedSeats: List<String>,
    selectedSeats: List<String>,
    onSeatToggle: (String) -> Unit
) {
    val totalRows = room?.totalRows?.takeIf { it > 0 } ?: 6
    val seatsPerRow = room?.seatsPerRow?.takeIf { it > 0 } ?: 8

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(BookingCard)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.72f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text("MÀN HÌNH", color = BookingMuted, fontSize = 10.sp, letterSpacing = 1.sp)
        }

        Spacer(modifier = Modifier.height(22.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            repeat(totalRows) { rowIndex ->
                val rowLabel = rowName(rowIndex)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(rowLabel, color = BookingMuted, fontSize = 12.sp, modifier = Modifier.width(22.dp))
                    repeat(seatsPerRow) { seatIndex ->
                        val seatName = "$rowLabel${seatIndex + 1}"
                        SeatBox(
                            seatName = seatName,
                            isBooked = bookedSeats.contains(seatName),
                            isSelected = selectedSeats.contains(seatName),
                            onSeatToggle = onSeatToggle
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))
        SeatLegend()
    }
}

@Composable
private fun SeatBox(
    seatName: String,
    isBooked: Boolean,
    isSelected: Boolean,
    onSeatToggle: (String) -> Unit
) {
    val background = when {
        isBooked -> Color(0xFF4B4E57)
        isSelected -> BookingRed
        else -> Color(0xFF343844)
    }
    val textColor = if (isBooked) Color(0xFF8A8D96) else Color.White

    Box(
        modifier = Modifier
            .padding(horizontal = 3.dp)
            .size(width = 30.dp, height = 28.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(background)
            .then(if (isBooked) Modifier else Modifier.clickable { onSeatToggle(seatName) }),
        contentAlignment = Alignment.Center
    ) {
        Text(seatName.drop(1), color = textColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SeatLegend() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        LegendItem(color = Color(0xFF343844), label = "Còn trống")
        LegendItem(color = BookingRed, label = "Đang chọn")
        LegendItem(color = Color(0xFF4B4E57), label = "Đã đặt")
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, color = BookingMuted, fontSize = 11.sp)
    }
}

@Composable
private fun TicketSummaryCard(
    movieTitle: String,
    cinemaName: String,
    roomName: String,
    showtime: String,
    seats: List<String>,
    totalPrice: Long,
    successTicketId: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = BookingCard)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ConfirmationNumber, contentDescription = null, tint = BookingRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Thông tin vé", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            TicketLine(label = "Phim", value = movieTitle)
            TicketLine(label = "Rạp", value = cinemaName)
            TicketLine(label = "Phòng", value = roomName)
            TicketLine(label = "Suất chiếu", value = formatDateTime(showtime))
            TicketLine(label = "Ghế", value = seats.joinToString(", "))
            TicketLine(label = "Tổng tiền", value = formatPrice(totalPrice))
            TicketLine(label = "Thanh toán", value = "ZaloPay Simulation")

            if (successTicketId != null) {
                TicketLine(label = "Mã vé", value = successTicketId)
            }
        }
    }
}

@Composable
private fun TicketLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = BookingMuted, fontSize = 13.sp)
        Text(
            value,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .padding(start = 18.dp)
                .weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun BottomActionBar(
    title: String,
    subtitle: String,
    buttonText: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(color = BookingCardAlt, shadowElevation = 10.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = BookingMuted, fontSize = 12.sp)
            }
            Button(
                onClick = onClick,
                enabled = enabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BookingRed,
                    disabledContainerColor = Color(0xFF555862)
                )
            ) {
                Text(buttonText)
            }
        }
    }
}

@Composable
private fun LoadingBox() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Color.White)
    }
}

@Composable
private fun EmptyText(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, color = BookingMuted, style = MaterialTheme.typography.bodyLarge)
    }
}

private fun rowName(index: Int): String = ('A'.code + index).toChar().toString()

private fun formatHour(value: String): String {
    return value.replace("T", " ").replace("Z", "").drop(11).take(5).ifBlank { value }
}

private fun showtimeDateKey(value: String): String {
    return value.take(10)
}

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

private fun formatDateTime(value: String): String {
    return value.replace("T", " ").replace("Z", "").take(16)
}

private fun formatPrice(value: Long): String {
    return String.format(Locale.US, "%,d VND", value)
}

private val previewCinema = Cinema(
    id = "c_01",
    name = "CGV Vincom Center",
    address = "72 Lê Thánh Tôn, Quận 1, TP.HCM",
    facilities = listOf("IMAX", "4DX", "Dolby Atmos")
)

private val previewRoom = Room(
    cinemaId = "c_01",
    id = "r_01",
    name = "Phòng IMAX 01",
    seatsPerRow = 8,
    totalRows = 6,
    type = "IMAX"
)

private val previewShowtime = Showtime(
    bookedSeats = listOf("A1", "A2", "C5"),
    cinemaId = "c_01",
    id = "st_101",
    movieId = "m_001",
    price = 150000,
    roomId = "r_01",
    startTime = "2026-03-21T19:00:00Z"
)

private val previewShowtimeNextDay = Showtime(
    bookedSeats = listOf("B1", "B2", "D4"),
    cinemaId = "c_01",
    id = "st_102",
    movieId = "m_001",
    price = 150000,
    roomId = "r_01",
    startTime = "2026-03-22T13:15:00Z"
)

private val previewMovie = Movie(
    id = "m_001",
    title = "Nasuverse: The Crimson Moon",
    duration = 125,
    genres = listOf("Hành động", "Giả tưởng"),
    rating = 8.7
)

@Preview(showBackground = true, widthDp = 390, heightDp = 860)
@Composable
private fun BookingCinemaScreenPreview() {
    LoginAppTheme(dynamicColor = false) {
        BookingCinemaContent(
            cinemas = listOf(previewCinema),
            showtimes = listOf(previewShowtime, previewShowtimeNextDay),
            isLoading = false,
            onBackClick = {},
            onShowtimeClick = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 860)
@Composable
private fun BookingSeatScreenPreview() {
    LoginAppTheme(dynamicColor = false) {
        BookingSeatContent(
            showtime = previewShowtime,
            room = previewRoom,
            cinema = previewCinema,
            selectedSeats = listOf("B1", "B2"),
            isLoading = false,
            onBackClick = {},
            onSeatToggle = {},
            onContinueClick = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 860)
@Composable
private fun BookingTicketScreenPreview() {
    LoginAppTheme(dynamicColor = false) {
        BookingTicketContent(
            movie = previewMovie,
            showtime = previewShowtime,
            cinema = previewCinema,
            room = previewRoom,
            seats = listOf("B1", "B2"),
            isLoading = false,
            isSaving = false,
            successTicketId = "tk_888",
            errorMessage = null,
            onBackClick = {},
            onDoneClick = {},
            onConfirmClick = {}
        )
    }
}
