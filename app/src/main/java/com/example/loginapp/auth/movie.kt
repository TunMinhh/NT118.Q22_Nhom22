package com.example.loginapp.auth

import com.google.firebase.firestore.FirebaseFirestore


data class Movie(
    val id: String = "",
    val title: String = "",
    val posterUrl: String = "",
    val ageRating: String = "",
    val cast: List<String> = emptyList(),
    val director: String = "",
    val duration: Int = 0,
    val genres: List<String> = emptyList(),
    val isShowing: Boolean = false,
    val isTrending: Boolean = false
)

fun getMoviesFromFirestore(onResult: (List<Movie>) -> Unit) {
    val db = FirebaseFirestore.getInstance()

    db.collection("movies")
        .get()
        .addOnSuccessListener { result ->
            val movieList = mutableListOf<Movie>()


            for (document in result) {
                val movie = document.toObject(Movie::class.java)
                movieList.add(movie)
            }
            onResult(movieList)
        }
        .addOnFailureListener { exception ->
            println("Error getting document: $exception")
            onResult(emptyList())
        }
}
