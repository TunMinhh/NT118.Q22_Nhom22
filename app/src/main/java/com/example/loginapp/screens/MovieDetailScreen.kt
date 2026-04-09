package com.example.loginapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

// Nhớ import các class Movie và hàm getMoviesFromFirestore của bạn vào đây
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

    // Gọi API lấy dữ liệu
    LaunchedEffect(Unit) {
        getMoviesFromFirestore {
            movieList = it.shuffled()
        }
    }

    // Tìm phim theo ID
    val movie = movieList.find { it.id == movieId }

    // XỬ LÝ QUAN TRỌNG: Nếu phim chưa tải xong (hoặc không thấy), hiện Loading
    if (movie == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.White)
        }
        return // Dừng vẽ giao diện bên dưới cho đến khi có dữ liệu
    }

    // Nếu đã có dữ liệu movie thì vẽ giao diện chính
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .verticalScroll(rememberScrollState())
    ) {
        // 1. Thanh Top Bar (Back, Tiêu đề, Bookmark)
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

        // 2. Khu vực Hero (Thẻ Box bao quanh toàn bộ phần ảnh)
        Box(modifier = Modifier.fillMaxWidth()) {

            // Ảnh bìa to (Backdrop)
            AsyncImage(
                model = movie.posterUrl, // Tạm thời dùng posterUrl, nếu Model có backdropUrl thì bạn thay vào
                contentDescription = movie.synopsis,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )

            // Nút đánh giá (Rating) nằm góc dưới phải của ảnh bìa
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd) // Lệnh này giờ đã hợp lệ vì nằm trong Box
                    .padding(bottom = 16.dp, end = 24.dp)
                    .background(Color(0x80000000), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(movie.rating.toString(), color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            // Khối chứa Poster nhỏ và Tiêu đề
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp)
                    .offset(y = 160.dp) // Đẩy khối này xuống
            ) {
                // Ảnh Poster nhỏ
                AsyncImage(
                    model = movie.posterUrl,
                    contentDescription = movie.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(110.dp)
                        .height(150.dp)
                        .clip(RoundedCornerShape(12.dp))
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Tiêu đề phim lấy từ Model
                Text(
                    text = movie.title,
                    color = textColor,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 70.dp)
                )
            }
        } // KẾT THÚC KHU VỰC HERO

        // Tạo khoảng trống bù lại cho phần offset ở trên
        Spacer(modifier = Modifier.height(100.dp))

        // 3. Thanh Thông tin (Năm | Thời lượng | Thể loại)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            InfoItem(icon = Icons.Default.CalendarToday, text = movie.releaseDate, color = secondaryTextColor)
            Text("  |  ", color = secondaryTextColor)
            InfoItem(icon = Icons.Default.Schedule, text = movie.duration.toString(), color = secondaryTextColor)
            Text("  |  ", color = secondaryTextColor)
            InfoItem(
                icon = Icons.Default.Theaters,
                text = movie.genres.joinToString(separator = ", "), // Nối các thể loại bằng dấu phẩy
                color = secondaryTextColor
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 4. Các Tab (About Movie, Reviews, Cast)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Thông tin về phim", color = textColor, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier
                    .height(2.dp)
                    .width(40.dp)
                    .background(Color.DarkGray))
            }
            Text("Đánh giá", color = secondaryTextColor, fontSize = 16.sp)
            Text("Diễn viên", color = secondaryTextColor, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 5. Nội dung mô tả (Nếu Model có trường mô tả, bạn có thể thay biến vào đây)
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

// Hàm phụ
@Composable
fun InfoItem(icon: ImageVector, text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, color = color, fontSize = 12.sp)
    }
}