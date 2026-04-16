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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Theaters
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.loginapp.auth.Movie
import com.example.loginapp.auth.getMoviesFromFirestore

@Composable
fun MovieDetailScreen(
    movieId: String,
    onBackClick: () -> Unit
) {
    val backgroundColor = Color(0xFF1B1E25)
    val textColor = Color.White
    val secondaryTextColor = Color(0xFFA0A0A0)

    var movieList by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var hasLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(movieId) {
        getMoviesFromFirestore {
            movieList = it
            hasLoaded = true
        }
    }

    val movie = movieList.find { it.id == movieId || it.title == movieId }

    if (!hasLoaded) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.White)
        }
        return
    }

    if (movie == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Không tìm thấy phim này",
                color = textColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Dữ liệu phim có thể đã thay đổi hoặc liên kết không còn hợp lệ.",
                color = secondaryTextColor,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onBackClick) {
                Text("Quay lại")
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 24.dp, end = 24.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = "Quay về",
                    tint = textColor
                )
            }
            Text(
                text = "Chi tiết phim",
                color = textColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Icon(
                imageVector = Icons.Default.BookmarkBorder,
                contentDescription = "Dấu trang",
                tint = textColor
            )
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            AsyncImage(
                model = movie.posterUrl,
                contentDescription = movie.synopsis,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )

            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 16.dp, end = 24.dp)
                    .background(Color(0x80000000), androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFC107),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = movie.rating.toString(),
                    color = textColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp)
                    .offset(y = 160.dp)
            ) {
                AsyncImage(
                    model = movie.posterUrl,
                    contentDescription = movie.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(110.dp)
                        .height(150.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = movie.title,
                    color = textColor,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 70.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(100.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            InfoItem(
                icon = Icons.Default.CalendarToday,
                text = movie.releaseDate,
                color = secondaryTextColor
            )
            Text("  |  ", color = secondaryTextColor)
            InfoItem(
                icon = Icons.Default.Schedule,
                text = movie.duration.toString(),
                color = secondaryTextColor
            )
            Text("  |  ", color = secondaryTextColor)
            InfoItem(
                icon = Icons.Default.Theaters,
                text = movie.genres.joinToString(separator = ", "),
                color = secondaryTextColor
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Thông tin về phim",
                    color = textColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .height(2.dp)
                        .width(40.dp)
                        .background(Color.DarkGray)
                )
            }
            Text("Đánh giá", color = secondaryTextColor, fontSize = 16.sp)
            Text("Diễn viên", color = secondaryTextColor, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = movie.synopsis,
            color = secondaryTextColor,
            fontSize = 14.sp,
            lineHeight = 22.sp,
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        )
    }
}

@Composable
fun InfoItem(icon: ImageVector, text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, color = color, fontSize = 12.sp)
    }
}


































// =====================================================================
// 1. TÁCH PHẦN GIAO DIỆN RA MỘT HÀM RIÊNG (STATELESS COMPOSABLE)
// Hàm này chỉ nhận dữ liệu và vẽ, KHÔNG gọi Firebase.
// (Bạn hãy copy toàn bộ nội dung Column giao diện ở trên của bạn xuống đây)
// =====================================================================
@Composable
fun MovieDetailContent(
    movie: Movie,
    onBackClick: () -> Unit
) {
    val backgroundColor = Color(0xFF1B1E25)
    val textColor = Color.White
    val secondaryTextColor = Color(0xFFA0A0A0)
    val tabs = listOf("Thông tin", "Đánh giá", "Diễn viên")
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .verticalScroll(rememberScrollState())
    ) {
        // 1. Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 24.dp, end = 24.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Quay về", tint = textColor)
            }
            Text("Chi tiết phim", color = textColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Icon(Icons.Default.BookmarkBorder, contentDescription = "Dấu trang", tint = textColor)
        }

        // 2. Khu vực Banner & Poster
        Box(modifier = Modifier.fillMaxWidth()) {
            AsyncImage(
                model = movie.posterUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(220.dp)
            )

            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 16.dp, end = 24.dp)
                    .background(Color(0x80000000), androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(movie.rating.toString(), color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp)
                    .offset(y = 160.dp)
            ) {
                AsyncImage(
                    model = movie.posterUrl,
                    contentDescription = movie.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.width(110.dp).height(150.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = movie.title,
                    color = textColor, fontSize = 22.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 70.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(100.dp))

        // 3. Thông tin
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            InfoItem(icon = Icons.Default.CalendarToday, text = movie.releaseDate, color = secondaryTextColor)
            Text("  |  ", color = secondaryTextColor)
            // Lưu ý: Đổi duration thành chuỗi cho InfoItem
            InfoItem(icon = Icons.Default.Schedule, text = "${movie.duration} Phút", color = secondaryTextColor)
            Text("  |  ", color = secondaryTextColor)
            InfoItem(icon = Icons.Default.Theaters, text = movie.genres.joinToString(", "), color = secondaryTextColor)
        }

        Spacer(modifier = Modifier.height(32.dp))





        // 4. Thanh Tabs có thể click
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTabIndex == index // Kiểm tra xem tab này có đang được chọn không

                Column(
                    modifier = Modifier.clickable {
                        selectedTabIndex = index // Khi click, cập nhật lại biến trạng thái
                    },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = title,
                        color = if (isSelected) textColor else secondaryTextColor, // Đổi màu
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Chỉ hiện đường gạch chân nếu Tab này đang được chọn
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .height(2.dp)
                                .width(40.dp)
                                .background(Color.White) // Đổi thành màu trắng cho nổi bật
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 5. Nội dung thay đổi theo Tab
        when (selectedTabIndex) {
            0 -> {
                // Nội dung Tab 0: Thông tin phim (Synopsis)
                Text(
                    text = movie.synopsis,
                    color = secondaryTextColor,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 32.dp)
                )
            }
            1 -> {
                // Nội dung Tab 1: Đánh giá (Tạm thời để chữ mồi)
                Text(
                    text = "Chưa có đánh giá nào cho phim này.",
                    color = secondaryTextColor,
                    modifier = Modifier
                        .padding(horizontal = 24.dp) // Áp dụng padding 2 bên trái phải trước
                        .padding(bottom = 32.dp)     // Sau đó áp dụng tiếp padding ở dưới
                )
            }
            2 -> {
                // Nội dung Tab 2: Diễn viên (Tạm thời để chữ mồi)
                Text(
                    text = "Danh sách diễn viên đang được cập nhật...",
                    color = secondaryTextColor,
                    modifier = Modifier
                        .padding(horizontal = 24.dp) // Áp dụng padding 2 bên trái phải trước
                        .padding(bottom = 32.dp)     // Sau đó áp dụng tiếp padding ở dưới
                )
            }
        }
    }
}


// =====================================================================
// 2. HÀM PREVIEW HIỂN THỊ DỮ LIỆU GIẢ
// =====================================================================
@Preview(showBackground = true, name = "Màn hình chi tiết phim")
@Composable
fun MovieDetailScreenPreview() {
    val mockMovie = Movie(
        id = "mock_123",
        title = "Spiderman: No Way Home",
        posterUrl = "https://image.tmdb.org/t/p/w500/1g0dhYtq4irTY1R80vEM421aN5i.jpg", // Dùng link ảnh thật để test
        rating = 9.5,
        releaseDate = "2021",
        duration = 148, // Lưu ý xem Model của bạn duration là Int hay Double nhé
        genres = listOf("Hành động", "Viễn tưởng", "Phiêu lưu"),
        synopsis = "Danh tính của Người Nhện bị tiết lộ, anh ấy không còn có thể tách biệt cuộc sống bình thường của mình khỏi những rủi ro cao của một siêu anh hùng. Khi nhờ Doctor Strange giúp đỡ, rủi ro càng trở nên nguy hiểm hơn, buộc anh phải khám phá ra ý nghĩa thực sự của việc làm Người Nhện."
    )

    // Gọi hàm giao diện thuần túy ở trên
    MovieDetailContent(
        movie = mockMovie,
        onBackClick = {}
    )
}