package com.example.loginapp.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.loginapp.auth.Ticket
import com.example.loginapp.auth.getTicketsByUserId
import com.google.firebase.auth.FirebaseAuth


private val HistoryBg      = Color(0xFF111318)
private val HistoryCard    = Color(0xFF1B1E25)
private val HistoryCardAlt = Color(0xFF2A2E38)
private val HistoryRed     = Color(0xFFE50914)
private val HistoryGreen   = Color(0xFF2ECC71)
private val HistoryMuted   = Color(0xFFA0A0A0)
private val HistoryAccent  = Color(0xFFFFD700)

@Composable
fun BookingHistoryScreen(onBackClick: () -> Unit) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var tickets by remember { mutableStateOf<List<Ticket>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotBlank()) {
            isLoading = true
            getTicketsByUserId(currentUserId) { result ->
                tickets = result
                isLoading = false
                isLoaded = true
            }
        } else {
            isLoading = false
            isLoaded = true
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = HistoryBg) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Header ──────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF1F222E), HistoryBg)
                        )
                    )
                    .padding(top = 42.dp, bottom = 12.dp)
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 4.dp)
                ) {
                    Icon(
                        Icons.Default.ArrowBackIosNew,
                        contentDescription = "Quay lại",
                        tint = Color.White
                    )
                }
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.ConfirmationNumber,
                        contentDescription = null,
                        tint = HistoryRed,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Lịch sử đặt vé",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // ── Body ─────────────────────────────────────────────────────────
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = HistoryRed)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Đang tải lịch sử...", color = HistoryMuted, fontSize = 14.sp)
                        }
                    }
                }

                tickets.isEmpty() && isLoaded -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.ConfirmationNumber,
                                contentDescription = null,
                                tint = HistoryCardAlt,
                                modifier = Modifier.size(72.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Bạn chưa đặt vé nào",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Hãy chọn phim yêu thích và đặt vé ngay!",
                                color = HistoryMuted,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                else -> {
                    // Tổng số vé đã đặt
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(HistoryRed.copy(alpha = 0.15f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${tickets.size} vé đã đặt",
                                color = HistoryRed,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        itemsIndexed(tickets, key = { _, t -> t.id }) { index, ticket ->
                            AnimatedVisibility(
                                visible = isLoaded,
                                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 })
                            ) {
                                TicketHistoryCard(ticket = ticket, index = index + 1)
                            }
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun TicketHistoryCard(ticket: Ticket, index: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = HistoryCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // ── Thanh trên: số thứ tự + trạng thái ─────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(HistoryRed.copy(alpha = 0.85f), Color(0xFF8B0000))
                        ),
                        shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ConfirmationNumber,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Vé #$index",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                // Badge trạng thái
                val isPaid = ticket.paymentStatus.equals("success", ignoreCase = true)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isPaid) HistoryGreen.copy(alpha = 0.2f) else HistoryAccent.copy(alpha = 0.2f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (isPaid) HistoryGreen else HistoryAccent,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isPaid) "Đã thanh toán" else ticket.paymentStatus,
                        color = if (isPaid) HistoryGreen else HistoryAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // ── Nội dung vé ─────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Tên phim (nổi bật)
                Text(
                    text = ticket.movieTitle,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                HorizontalDivider(color = HistoryCardAlt, thickness = 1.dp)

                HistoryInfoRow(icon = Icons.Default.LocationOn,   label = "Rạp",       value = ticket.cinemaName)
                HistoryInfoRow(icon = Icons.Default.EventSeat,    label = "Ghế",       value = ticket.seats.joinToString(", ").ifBlank { "—" })
                HistoryInfoRow(icon = Icons.Default.Schedule,     label = "Đặt lúc",   value = formatHistoryDateTime(ticket.bookingTime))
                HistoryInfoRow(icon = Icons.Default.Movie,        label = "Thanh toán",value = ticket.paymentMethod.ifBlank { "—" })

                HorizontalDivider(color = HistoryCardAlt, thickness = 1.dp)

                // Tổng tiền
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tổng tiền", color = HistoryMuted, fontSize = 13.sp)
                    Text(
                        text = formatHistoryPrice(ticket.totalPrice),
                        color = HistoryRed,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Mã vé (nhỏ, ở cuối)
                if (ticket.id.isNotBlank()) {
                    Text(
                        text = "Mã vé: ${ticket.id}",
                        color = HistoryMuted.copy(alpha = 0.6f),
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = HistoryMuted,
            modifier = Modifier
                .size(15.dp)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label: ",
            color = HistoryMuted,
            fontSize = 13.sp,
            modifier = Modifier.width(76.dp)
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun formatHistoryDateTime(value: String): String {
    if (value.isBlank()) return "—"
    return try {
        // "2026-05-22T19:00:00Z" -> "22/05/2026 19:00"
        val clean = value.replace("T", " ").replace("Z", "").take(16)
        val parts = clean.split(" ")
        if (parts.size == 2) {
            val dateParts = parts[0].split("-")
            if (dateParts.size == 3) "${dateParts[2]}/${dateParts[1]}/${dateParts[0]} ${parts[1]}"
            else clean
        } else clean
    } catch (_: Exception) { value }
}

private fun formatHistoryPrice(value: Long): String {
    return String.format(java.util.Locale.US, "%,d VND", value)
}
