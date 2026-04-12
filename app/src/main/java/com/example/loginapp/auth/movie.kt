package com.example.loginapp.auth

import com.google.firebase.firestore.FirebaseFirestore

// Model dữ liệu phim — mỗi field tương ứng một field trong Firestore
data class Movie(
    val id: String = "",             // ID document Firestore
    val title: String = "",          // Tên phim
    val posterUrl: String = "",      // Link ảnh poster
    val ageRating: String = "",      // Giới hạn tuổi (C13, C16, C18, P)
    val cast: List<String> = emptyList(),
    val director: String = "",
    val duration: Int = 0,           // Thời lượng (phút)
    val genres: List<String> = emptyList(),
    val isShowing: Boolean = false,  // true = đang chiếu, false = sắp chiếu
    val isTrending: Boolean = false,
    val synopsis: String = "",
    val rating: Double = 0.0,
    val releaseDate: String = "",
)

// Lấy toàn bộ danh sách phim từ Firestore rồi trả về qua callback
fun getMoviesFromFirestore(onResult: (List<Movie>) -> Unit) {
    val db = FirebaseFirestore.getInstance()

    db.collection("movies")
        .get()
        .addOnSuccessListener { result ->
            val movieList = mutableListOf<Movie>()


            for (document in result) {
                val data = document.data ?: continue

                // isShowing trên Firestore có thể là Boolean hoặc String "true"/"false"
                // phải xử lý thủ công, không thể tin hoàn toàn vào toObject()
                val isShowingRaw = data["isShowing"]
                val isShowing = when (isShowingRaw) {
                    is Boolean -> isShowingRaw
                    is String  -> isShowingRaw.trim().equals("true", ignoreCase = true)
                    else       -> false
                }
                val movie = document.toObject(Movie::class.java).copy(isShowing = isShowing)
                movieList.add(movie)
            }
            onResult(movieList)
        }
        .addOnFailureListener { exception ->
            println("Error getting document: $exception")
            onResult(emptyList()) // Trả về rỗng để UI không crash
        }
}
