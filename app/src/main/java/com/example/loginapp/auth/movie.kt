package com.example.loginapp.auth

import android.R
import androidx.compose.runtime.mutableStateOf
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Named

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
fun getMoviesFromFirestore(onResult: (List<Movie>) -> Unit) {
    val db = FirebaseFirestore.getInstance()

    db.collection("movies")
        .get()
        .addOnSuccessListener { result ->
            val movieList = mutableListOf<Movie>()

            for (document in result) {
                val data = document.data ?: continue

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

data class Review(
    val comment: String = "",
    val createdAt: String = "",
    val id: String = "",
    val movieId: String = "",
    val rating: Int = 0,
    val userId: String = "",
    val userName: String = ""
)

fun getReviewsByMovieId(movieId: String, onResult: (List<Review>) -> Unit) {
    val db = FirebaseFirestore.getInstance()

    db.collection("reviews")
        .whereEqualTo("movieId", movieId)
        .get()
        .addOnSuccessListener { result ->
            val reviewList = result.toObjects(Review::class.java)
            onResult(reviewList)
        }
        .addOnFailureListener { exception ->
            println("Error getting Review: $exception")
            onResult(emptyList())
        }
}


data class Actor(
    val name: String = "",
    val age: String = "",
    val imageURL: String = "",
    val nationality: String =""
)

fun getActorsByNames(list_actor: List<String>, onResult: (List<Actor>) -> Unit) {
    val db = FirebaseFirestore.getInstance()

    db.collection("actors")
        .whereIn("name", list_actor)
        .get()
        .addOnSuccessListener { result ->
            val allActor = result.toObjects(Actor::class.java)
            onResult(allActor)
        }
        .addOnFailureListener{ exception ->
            println("Error getting actor: $exception")
            onResult(emptyList())
        }
}

