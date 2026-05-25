package com.example.loginapp.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import coil.compose.AsyncImage
import com.example.loginapp.R
import com.example.loginapp.auth.Movie
import com.example.loginapp.auth.getMoviesFromFirestore
import kotlin.math.absoluteValue

// Dữ liệu tĩnh cho các section tin tức / khuyến mãi phía dưới HomeScreen
val items = listOf(
    Pair(R.drawable.banner_uu_dai, "Cơ hội trúng quà khi mua online"),
    Pair(R.drawable.thanh_vien_cgv, "Đăng ký thành viên"),
    Pair(R.drawable.voucher_momo_cgv, "Voucher momo")
)

val giftList = listOf(
    Pair(R.drawable.banner_egift, "Thẻ egift cgv"),
    Pair(R.drawable.the_qua_tang, "Thẻ quà tặng"),
    Pair(R.drawable.voucher_khuyen_mai, "voucher khuyến mãi")
)

val videoList = listOf(
    Pair(R.drawable.spiderman_brand_new_day_trailer, "Trailer Spider-Man Brand New Day "),
    Pair(R.drawable.avatar_lua_va_trotan, "Trailer Avatar lửa và tro tàn"),
    Pair(R.drawable.movie3, "Thoát khỏi tận thế")
)


// Màn hình chính sau khi đăng nhập — nền blur, carousel phim, các section tin tức
@Composable
fun HomeScreen(
    displayName: String?,
    userEmail: String?,
    infoMessage: String?,
    onSignOutClick: () -> Unit,
    onMovieClick: (String) -> Unit,
    onSearchStateChange: (Boolean) -> Unit = {}
) {

    var movieList by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }

    // Gọi Firestore khi mở màn hình
    LaunchedEffect(Unit) {
        getMoviesFromFirestore {
        movieList = it
            it.forEach { movie ->
                println("DEBUG: ${movie.title} - ${movie.posterUrl}")
            }
        }
    }

//    LaunchedEffect(Unit) {
//        getMoviesFromFirestore { it ->
//            // Lấy danh sách 'it' trả về, trộn ngẫu nhiên và random trạng thái isShowing
//            movieList = it.shuffled().map { movie ->
//                movie.copy(isShowing = listOf(true, false).random())
//            }
//        }
//    }

    Box(modifier = Modifier.fillMaxSize()) {

        if (movieList.isNotEmpty()) {
            AsyncImage(
                model = movieList[0].posterUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(50.dp),
                contentScale = ContentScale.Crop
            )
        }

        // Gradient phủ lên
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xAA1A0033),
                            Color(0xDD000000)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()

                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically // căn giữa theo màn hình
            ) {

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            text = "CGV",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "*",
                            color = Color.White,
                            fontSize = 12.sp,
                            modifier = Modifier.offset(x = 2.dp, y = (-6).dp)
                        )
                    }
                }
            }

            androidx.compose.material3.OutlinedTextField(
                value = searchQuery,
                onValueChange = { newText ->
                    searchQuery = newText
                    onSearchStateChange(newText.isNotEmpty())
                },
                placeholder = { Text("Tìm phim đang chiếu...", color = Color.Gray) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.Gray
                    )
                },


                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        androidx.compose.material3.IconButton(onClick = {
                            searchQuery = "" // Xóa sạch chữ
                            onSearchStateChange(false) // Hiện lại thanh điều hướng ở dưới
                        }) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Clear,
                                contentDescription = "Xóa",
                                tint = Color.White
                            )
                        }
                    }
                },


                shape = androidx.compose.foundation.shape.RoundedCornerShape(25.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF2A2E38),
                    unfocusedContainerColor = Color(0xFF2A2E38),
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (searchQuery.isEmpty()) {
                Text(
                    text = "Đang Chiếu",
                    color = Color.White,
                    fontSize = 20.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                MovieCarousel(movieList.filter { it.isShowing }, onMovieClick)

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .background(
                            Color.White,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                        )
                        .padding(16.dp)
                ) {
                    Column {
                        supportSelection()
                        Spacer(modifier = Modifier.height(12.dp))
                        NewsSection(
                            title = "Tin nóng",
                            items = listOf(
                                Pair(R.drawable.banner_uu_dai, "Cơ hội trúng quà khi mua online"),
                                Pair(R.drawable.thanh_vien_cgv, "x2 khi đăng ký thành viên"),
                                Pair(R.drawable.voucher_momo_cgv, "voucher của momo")
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        NewsSection(title = "CGV eGift", items = giftList)
                        Spacer(modifier = Modifier.height(16.dp))
                        Column {
                            SectionHeader("Videos")
                            androidx.compose.foundation.lazy.LazyRow {
                                items(videoList) { item ->
                                    VideoItem(image = item.first, title = item.second)
                                }
                            }
                        }
                    }
                }
            } else {
                val searchResults = movieList.filter { movie ->
                    movie.isShowing && movie.title.contains(searchQuery, ignoreCase = true)
                }

                Text(
                    text = "Kết quả tìm kiếm cho: \"$searchQuery\"",
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                if (searchResults.isEmpty()) {
                    Text(
                        text = "Không tìm thấy phim đang chiếu nào.",
                        color = Color.Gray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        searchResults.forEach { movie ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .background(Color(0xFF2A2E38), androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                                    .clickable {
                                        // Bấm vào sẽ gọi mở màn hình chi tiết phim
                                        if (movie.id.isNotEmpty()) {
                                            onMovieClick(movie.id)
                                        } else {
                                            onMovieClick(movie.title)
                                        }
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                coil.compose.AsyncImage(
                                    model = movie.posterUrl,
                                    contentDescription = movie.title,
                                    modifier = Modifier
                                        .size(50.dp, 75.dp)
                                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp)),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                Text(
                                    text = movie.title,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun MovieCarousel(movieList: List<Movie>, onMovieClick: (String) -> Unit) {

    if (movieList.isEmpty()) {
        Text("Đang tải dữ liệu...")
        return
    }

    val pagerState = rememberPagerState(
        initialPage = 5000,
        pageCount = { 10000 }
    )

    HorizontalPager(
        state = pagerState,
        contentPadding = PaddingValues(horizontal = 80.dp),
        pageSpacing = 12.dp
    ) { page ->

        val movie = movieList[page % movieList.size]

        Card(
            modifier = Modifier
                .width(220.dp)
                .graphicsLayer {

                    val pageOffset =
                        (pagerState.currentPage - page) +
                                pagerState.currentPageOffsetFraction

                    val scale =
                        0.85f + (1 - pageOffset.absoluteValue) * 0.15f

                    scaleX = scale
                    scaleY = scale
                }
                .clickable {
                    if (movie.id.isNotEmpty()){
                        onMovieClick(movie.id)
                    }else{
                        onMovieClick(movie.title)
                    }
                },
            shape = RoundedCornerShape(12.dp)
        ) {

            Column {

                AsyncImage(
                    model = movie.posterUrl,
                    contentDescription = movie.title,
                    modifier = Modifier
                        .height(300.dp)
                        .fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )

                Text(
                    text = movie.title,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }


}

@Composable
fun supportSelection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()              // chiếm toàn bộ chiều ngang
            .padding(vertical = 12.dp)   // cách trên dưới
            .clickable {                 // click toàn bộ card

            },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Text chính
            Text (
                text = "Bạn cần hỗ trợ gì?",
                color = Color.Black,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Default.ArrowForwardIos,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(18.dp)
            )

        }


    }
}

@Composable
fun NewsItem(image: Int, title: String) {

    Column(
        modifier = Modifier
            .width(220.dp) // chiều rộng mỗi item
            .padding(end = 12.dp)
    ) {

        // Ảnh
        Image(
            painter = painterResource(image),
            contentDescription = title,
            modifier = Modifier
                .height(140.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Tiêu đề
        Text(
            text = title,
            fontSize = 14.sp,
            maxLines = 2, // tránh tràn

        )
    }
}

@Composable
fun SectionHeader(title: String) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // 🏷 Tiêu đề
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f) // đẩy nút sang phải
        )


    }
}

@Composable
fun NewsSection(title: String, items: List<Pair<Int, String>>) {

    Column {

        // Header
        SectionHeader(title)

        // LazyRow
        LazyRow {
            items(items) { item ->
                NewsItem(image = item.first, title = item.second)
            }
        }
    }
}

@Composable
fun VideoItem(image: Int, title: String) {

    Column(
        modifier = Modifier
            .width(220.dp)
            .padding(end = 12.dp)
    ) {

        Box {

            Image(
                painter = painterResource(image),
                contentDescription = title,
                modifier = Modifier
                    .height(120.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            // icon play
            Icon(
                imageVector = Icons.Default.ArrowForwardIos,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(30.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            fontSize = 14.sp,
            maxLines = 2
        )
    }
}