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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.loginapp.R
import org.intellij.lang.annotations.JdkConstants
import kotlin.math.absoluteValue
import androidx.compose.ui.text.style.TextAlign

//Dùng để lưu thông tin phim
data class Movie(
    val title: String,
    val imageRes: Int
)

// Màn hình chính sau khi login
@Composable
fun HomeScreen(
    displayName: String?,
    userEmail: String?,
    infoMessage: String?,
    onSignOutClick: () -> Unit
) {

    // Danh sách phim
    val movieList = listOf(
        Movie("Đêm ngày xa mẹ", R.drawable.movie1),
        Movie("Quỷ nhập tràng 2", R.drawable.movie2),
        Movie("Cuộc cứu nhân loại", R.drawable.movie3),
        Movie("Tài", R.drawable.movie4)
    )

    // Xếp màn hình theo chieu dọc
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFFFF))
            .padding(16.dp)
    ) {

        BannerCarousel()

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Đang Chiếu",
            color = Color.Black,
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()

        )

        Spacer(modifier = Modifier.height(12.dp))

        MovieCarousel(movieList)

    }
}

@Composable
fun BannerCarousel() {

    val banners = listOf(
        R.drawable.movie1,
        R.drawable.movie2,
        R.drawable.movie3
    )

    val pagerState = rememberPagerState(
        initialPage = 5000,
        pageCount = { 10000 }
    )

    HorizontalPager(
        state = pagerState,
        contentPadding = PaddingValues(horizontal = 60.dp),
        pageSpacing = 12.dp
    ) { page ->

        val banner = banners[page % banners.size]

        Card(
            modifier = Modifier
                .width(300.dp)
                .graphicsLayer {

                    val pageOffset =
                        (pagerState.currentPage - page) +
                                pagerState.currentPageOffsetFraction

                    val scale =
                        0.9f + (1 - pageOffset.absoluteValue) * 0.1f

                    scaleX = scale
                    scaleY = scale

                },
            shape = RoundedCornerShape(12.dp)

        )

        {
            Image(
                painter = painterResource(banner),
                contentDescription = "banner",
                modifier = Modifier
                    .height(160.dp)
                    .fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
        }

    }
}

@Composable
fun MovieCarousel(movieList: List<Movie>) {

    val pagerState = rememberPagerState(
        initialPage = 5000,   // bắt đầu ở giữa để vuốt 2 phía
        pageCount = { 10000 } //tạo giả lập 10000 page
    )

    HorizontalPager(
        state = pagerState,


        // khoảng cách hai bên để thấy poster kế bên
        contentPadding = PaddingValues(horizontal = 80.dp),

        // khoảng cách giữa các poster
        pageSpacing = 12.dp
    ) { page ->

        val movie = movieList[page % movieList.size]

        // Card để chứa các poster
        Card(
            modifier = Modifier
                .width(220.dp)

                // dùng để scale poster ở giữa lớn
                .graphicsLayer {

                    val pageOffset =
                        (pagerState.currentPage - page) +
                                pagerState.currentPageOffsetFraction

                    val scale =
                        0.85f + (1 - pageOffset.absoluteValue) * 0.15f

                    scaleX = scale
                    scaleY = scale
                }
                .clickable { },
            shape = RoundedCornerShape(12.dp)
        ) {

            Column {

                Image(
                    painter = painterResource(movie.imageRes),
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