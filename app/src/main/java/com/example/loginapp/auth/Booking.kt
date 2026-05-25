package com.example.loginapp.auth

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class Room(
    val cinemaId: String = "",
    val id: String = "",
    val name: String = "",
    val seatsPerRow: Int = 0,
    val totalRows: Int = 0,
    val type: String = ""
)

data class Showtime(
    val bookedSeats: List<String> = emptyList(),
    val cinemaId: String = "",
    val id: String = "",
    val movieId: String = "",
    val price: Long = 0L,
    val roomId: String = "",
    val startTime: String = ""
)

data class Ticket(
    val bookingTime: String = "",
    val cinemaName: String = "",
    val id: String = "",
    val movieTitle: String = "",
    val paymentMethod: String = "",
    val paymentStatus: String = "",
    val seats: List<String> = emptyList(),
    val showtimeId: String = "",
    val totalPrice: Long = 0L,
    val userId: String = ""
)

fun getShowtimesByMovieId(movieId: String, onResult: (List<Showtime>) -> Unit) {
    val db = FirebaseFirestore.getInstance()

    db.collection("showtimes")
        .whereEqualTo("movieId", movieId)
        .get()
        .addOnSuccessListener { result ->
            val showtimes = result.map { document ->
                val showtime = document.toObject(Showtime::class.java)
                showtime.copy(id = showtime.id.ifBlank { document.id })
            }
            onResult(showtimes)
        }
        .addOnFailureListener { exception ->
            println("Error getting showtimes: $exception")
            onResult(emptyList())
        }
}

fun getShowtimeById(showtimeId: String, onResult: (Showtime?) -> Unit) {
    val db = FirebaseFirestore.getInstance()

    db.collection("showtimes")
        .document(showtimeId)
        .get()
        .addOnSuccessListener { document ->
            val showtime = document.toObject(Showtime::class.java)
            onResult(showtime?.copy(id = showtime.id.ifBlank { document.id }))
        }
        .addOnFailureListener { exception ->
            println("Error getting showtime: $exception")
            onResult(null)
        }
}

fun getRoomById(roomId: String, onResult: (Room?) -> Unit) {
    val db = FirebaseFirestore.getInstance()

    db.collection("rooms")
        .document(roomId)
        .get()
        .addOnSuccessListener { document ->
            val room = document.toObject(Room::class.java)
            onResult(room?.copy(id = room.id.ifBlank { document.id }))
        }
        .addOnFailureListener { exception ->
            println("Error getting room: $exception")
            onResult(null)
        }
}

fun createTicket(
    cinemaName: String,
    movieTitle: String,
    seats: List<String>,
    showtimeId: String,
    totalPrice: Long,
    userId: String,
    onSuccess: (String) -> Unit,
    onFailure: (Exception) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val ticketRef = db.collection("tickets").document()
    val ticketId = ticketRef.id
    val bookingTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }.format(Date())

    val ticketData = hashMapOf(
        "bookingTime" to bookingTime,
        "cinemaName" to cinemaName,
        "id" to ticketId,
        "movieTitle" to movieTitle,
        "paymentMethod" to "ZaloPay Simulation",
        "paymentStatus" to "success",
        "seats" to seats,
        "showtimeId" to showtimeId,
        "totalPrice" to totalPrice,
        "userId" to userId
    )

    ticketRef.set(ticketData)
        .addOnSuccessListener {
            db.collection("showtimes")
                .document(showtimeId)
                .update("bookedSeats", FieldValue.arrayUnion(*seats.map { it as Any }.toTypedArray()))
                .addOnSuccessListener { onSuccess(ticketId) }
                .addOnFailureListener { onFailure(it) }
        }
        .addOnFailureListener { onFailure(it) }
}

// Lấy danh sách vé đã đặt của một người dùng, sắp xếp theo thời gian mới nhất trước
fun getTicketsByUserId(userId: String, onResult: (List<Ticket>) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("tickets")
        .whereEqualTo("userId", userId)
        .get()
        .addOnSuccessListener { result ->
            val tickets = result.map { document ->
                val ticket = document.toObject(Ticket::class.java)
                ticket.copy(id = ticket.id.ifBlank { document.id })
            }.sortedByDescending { it.bookingTime }
            onResult(tickets)
        }
        .addOnFailureListener { exception ->
            println("Error getting tickets: $exception")
            onResult(emptyList())
        }
}
