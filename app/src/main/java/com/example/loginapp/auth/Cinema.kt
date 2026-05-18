package com.example.loginapp.auth

import com.google.firebase.firestore.FirebaseFirestore

data class Cinema(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val facilities: List<String> = emptyList()
)

fun getCinemasFromFirestore(onResult: (List<Cinema>) -> Unit) {
    val db = FirebaseFirestore.getInstance()

    db.collection("cinemas")
        .get()
        .addOnSuccessListener { result ->
            val cinemaList = result.toObjects(Cinema::class.java)
            onResult(cinemaList)
        }
        .addOnFailureListener { exception ->
            println("Error getting cinemas: $exception")
            onResult(emptyList())
        }
}
