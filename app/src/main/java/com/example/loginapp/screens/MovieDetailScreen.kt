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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.loginapp.auth.Movie
import com.example.loginapp.auth.Review
import com.example.loginapp.auth.getReviewsByMovieId
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.style.TextAlign
import com.example.loginapp.auth.Actor
import com.example.loginapp.auth.getActorsByNames
import com.example.loginapp.auth.getMoviesFromFirestore
import androidx.compose.foundation.lazy.items
import com.example.loginapp.auth.User_Review
import com.example.loginapp.auth.getUserReviewsByMovieId
import com.example.loginapp.auth.postReview


@Composable
fun ReviewItem(review: Review) {
    val textColor = Color.White
    val secondaryTextColor = Color(0xFFA0A0A0)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF2A2E38), androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tên người dùng
            Text(
                text = if (review.userName.isNotEmpty()) review.userName else "Người dùng ẩn danh",
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            // Ngày tháng
            Text(
                text = review.createdAt,
                color = secondaryTextColor,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Vẽ số sao (Rating)
        Row {
            repeat(5) { index ->
                Icon(
                    imageVector = Icons.Rounded.Star,
                    contentDescription = null,
                    tint = if (index < review.rating) Color(0xFFFFC107) else Color.Gray,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Nội dung bình luận
        Text(
            text = review.comment,
            color = Color(0xFFD0D0D0),
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
    }
}

@Composable
fun ActorItem(actor: Actor) {
    Column(
        modifier = Modifier
            .width(90.dp)
            .padding(end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = actor.imageURL,
            contentDescription = actor.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color.DarkGray, CircleShape)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Tên diễn viên
        Text(
            text = actor.name,
            color = Color.White,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            maxLines = 2, // Nếu tên dài quá thì tự xuống dòng (tối đa 2 dòng)
            lineHeight = 16.sp
        )
    }
}

@Composable
fun AddReviewSection(
    movieId: String,
    onReviewSubmitted: () -> Unit // Để load lại danh sách sau khi gửi
) {
    var rating by remember { mutableIntStateOf(5) } // Mặc định 5 sao
    var comment by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }

    val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
    val secondaryTextColor = Color(0xFFA0A0A0)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF2A2E38), androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text("Đánh giá của bạn", color = Color.White, fontWeight = FontWeight.Bold)

        // 1. Hàng chọn sao
        Row(modifier = Modifier.padding(vertical = 8.dp)) {
            repeat(5) { index ->
                val starIndex = index + 1
                Icon(
                    imageVector = Icons.Rounded.Star,
                    contentDescription = null,
                    tint = if (starIndex <= rating) Color(0xFFFFC107) else Color.Gray,
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { rating = starIndex } // Người dùng bấm vào sao để chọn
                )
            }
        }

        // 2. Ô nhập bình luận
        androidx.compose.material3.TextField(
            value = comment,
            onValueChange = { comment = it },
            placeholder = { Text("Viết cảm nghĩ của bạn về phim...", color = secondaryTextColor, fontSize = 14.sp) },
            modifier = Modifier.fillMaxWidth().height(100.dp),
            colors = androidx.compose.material3.TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF1B1E25),
                unfocusedContainerColor = Color(0xFF1B1E25),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Nút Gửi
        Button(
            onClick = {
                if (currentUser != null && comment.isNotBlank()) {
                    isSending = true
                    postReview(
                        movieId = movieId,
                        userId = currentUser.uid,
                        userName = currentUser.displayName ?: "Người dùng ẩn danh",
                        rating = rating,
                        comment = comment,
                        onSuccess = {
                            isSending = false
                            comment = ""
                            rating = 5
                            onReviewSubmitted()
                        },
                        onFailure = { isSending = false }
                    )
                }
            },
            modifier = Modifier.align(Alignment.End),
            enabled = !isSending && comment.isNotBlank(),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color.White)
        ) {
            if (isSending) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp), // Dùng Modifier để chỉnh kích thước
                    color = Color.Black,             // Màu sắc
                    strokeWidth = 2.dp               // (Tùy chọn) Độ dày của vòng tròn
                )
            } else {
                Text("Gửi", color = Color.Black)
            }
        }
    }
}


@Composable
fun MovieDetailScreen(
    movieId: String,
    onBackClick: () -> Unit
) {
    val backgroundColor = Color(0xFF1B1E25)
    val textColor = Color.White
    val secondaryTextColor = Color(0xFFA0A0A0)
    val tabs = listOf("Thông tin", "Đánh giá", "Diễn viên")
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    var movieList by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var reviewList by remember { mutableStateOf<List<Review>>(emptyList()) }
    var userreviewList by remember { mutableStateOf<List<User_Review>>(emptyList()) }
    var hasLoaded by remember { mutableStateOf(false) }
    var actorList by remember { mutableStateOf<List<Actor>>(emptyList()) }


    LaunchedEffect(movieId) {
        getMoviesFromFirestore {
            movieList = it
            hasLoaded = true
        }

        getReviewsByMovieId(movieId) { result ->
            reviewList = result
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
    else {
        getActorsByNames(movie.cast) { result ->
            actorList = result
        }
    }





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
                Column(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 32.dp)
                ) {
                    AddReviewSection(
                        movieId = movieId,
                        onReviewSubmitted = {
                            getReviewsByMovieId(movieId) { result ->
                                reviewList = result
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                if (reviewList.isEmpty() && userreviewList.isEmpty()) {
                    Text(
                        text = "Chưa có đánh giá nào cho phim này. Hãy là người đầu tiên!",
                        color = secondaryTextColor,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .padding(bottom = 32.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .padding(bottom = 32.dp)
                            .heightIn(max = 400.dp)
                    ) {
                        items(reviewList) { review ->
                            ReviewItem(review = review)
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
            2 -> {
                if (actorList.isEmpty()) {
                    Text(
                        text = "Danh sách diễn viên đang được cập nhật...",
                        color = secondaryTextColor, // Đảm bảo bạn đã khai báo màu này
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .padding(bottom = 32.dp)
                    )
                } else {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, bottom = 32.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(actorList) { actor ->
                            ActorItem(actor = actor)
                        }
                    }
                }

            }
        }
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














