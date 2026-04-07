package com.example.loginapp.list_movie

import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.loginapp.R
import com.google.firebase.firestore.FirebaseFirestore
import coil.compose.AsyncImage
// --- MÀU SẮC GIAO DIỆN ---
val BackgroundColor = Color(0xFFFFFFFF)
val SearchBarColor = Color(0xFFF2F2F2)
val TabUnselectedColor = Color(0xFF67686D)
val PrimaryTextColor = Color(0xFF000000)
val AccentColor = Color(0xFF0296E5)

// --- DATA CLASS (Đã cập nhật theo Database mới nhất) ---
data class Movie(
    val ageRating: String? = null,
    val cast: List<String>? = null,
    val director: String? = null,
    val duration: Int? = null,
    val genres: List<String>? = null,
    val id: String? = null,
    val isShowing: Boolean? = null,
    val isTrending: Boolean? = null,
    val posterUrl: String? = null,
    val rating: Double? = null,
    val releaseDate: String? = null,
    val synopsis: String? = null,
    val title: String? = null
)

// --- HÀM LẤY TOÀN BỘ PHIM TỪ FIREBASE ---
fun getAllMovies(onResult: (List<Movie>) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("movies")
        .get()
        .addOnSuccessListener { result ->
            val movieList = result.toObjects(Movie::class.java)
            onResult(movieList)
        }
        .addOnFailureListener { exception ->
            Log.e("Firestore", "Lỗi tải phim: ", exception)
            onResult(emptyList())
        }
}

// --- MÀN HÌNH CHÍNH ---
@Composable
fun MainScreen() {   var movieList by remember { mutableStateOf<List<Movie>>(emptyList()) }

    LaunchedEffect(Unit) {
        getAllMovies { data ->
            //Lấy ngẫu nhiên phần tử của danh sách phim
            //Sau này phân loại phim theo độ nổi tiếng, mới ra, trending,...
            movieList = data.shuffled()
        }
    }

    Scaffold(
        containerColor = BackgroundColor,
        bottomBar = { BottomNavigationBar() }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
        ) {
            HeaderAndSearchBar()
            FeaturedMoviesRow(movies = movieList)
            CategoryTabs()
            MovieGrid(movies = movieList)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeaderAndSearchBar() {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Text(
            text = "Hôm nay xem gì?",
            color = PrimaryTextColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        TextField(
            value = "",
            onValueChange = {},
            placeholder = { Text("Tìm kiếm", color = TabUnselectedColor, fontSize = 14.sp) },
            trailingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TabUnselectedColor) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = SearchBarColor,
                unfocusedContainerColor = SearchBarColor,
                disabledContainerColor = SearchBarColor,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = PrimaryTextColor,
                unfocusedTextColor = PrimaryTextColor
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        )
    }
}

// --- DANH SÁCH PHIM NỔI BẬT (CUỘN NGANG) ---
@Composable
fun FeaturedMoviesRow(movies: List<Movie>) {
    Box(modifier = Modifier.padding(vertical = 16.dp)) {
        if (movies.isEmpty()) {
            Text("Đang tải dữ liệu phim...", color = TabUnselectedColor)
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                items(movies) { movie ->
                    AsyncImage(
                        model = movie.posterUrl,
                        contentDescription = movie.title,
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.ic_launcher_background),
                        error = painterResource(id = R.drawable.ic_launcher_background),
                        modifier = Modifier
                            .width(200.dp)
                            .height(300.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryTabs() {
    val tabs = listOf("Đang chiếu", "Sắp có", "Đánh giá cao", "Phổ biến")
    val selectedTabIndex = remember { mutableIntStateOf(0) }

    LazyRow(
        modifier = Modifier.padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        items(tabs.size) { index ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { selectedTabIndex.intValue = index }
            ) {
                Text(
                    text = tabs[index],
                    color = if (selectedTabIndex.intValue == index) PrimaryTextColor else TabUnselectedColor,
                    fontSize = 14.sp,
                    fontWeight = if (selectedTabIndex.intValue == index) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                if (selectedTabIndex.intValue == index) {
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(2.dp)
                            .background(AccentColor)
                    )
                }
            }
        }
    }
}

@Composable
fun MovieGrid(movies: List<Movie>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        // SỬA DÒNG NÀY: Dùng fillMaxSize() để chiếm hết chỗ trống còn lại mà không bị lỗi
        modifier = Modifier.fillMaxSize()
    ) {

        items(movies) { movie ->
            // Gói ảnh và Text vào Column để xếp dọc
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AsyncImage(
                    model = movie.posterUrl,
                    contentDescription = movie.title,
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.ic_launcher_background),
                    error = painterResource(id = R.drawable.ic_launcher_background),
                    modifier = Modifier
                        .aspectRatio(0.7f)
                        .clip(RoundedCornerShape(8.dp))
                )

                Text(
                    text = movie.title ?: "Đang cập nhật",
                    color = PrimaryTextColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1, // Nếu tên quá dài thì cắt bớt
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
@Composable
fun BottomNavigationBar() {
    val selectedItemIndex = remember { mutableIntStateOf(0) }
    val items = listOf(
        Pair("Trang chủ", Icons.Default.Home),
        Pair("Tìm kiếm", Icons.Default.Search),
        Pair("Danh sách", Icons.Default.BookmarkBorder)
    )

    NavigationBar(
        containerColor = BackgroundColor,
        tonalElevation = 8.dp
    ) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedItemIndex.intValue == index,
                onClick = { selectedItemIndex.intValue = index },
                icon = {
                    Icon(
                        imageVector = item.second,
                        contentDescription = item.first,
                        tint = if (selectedItemIndex.intValue == index) AccentColor else TabUnselectedColor
                    )
                },
                label = {
                    Text(
                        text = item.first,
                        color = if (selectedItemIndex.intValue == index) AccentColor else TabUnselectedColor,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMainScreen() {
    MainScreen()
}