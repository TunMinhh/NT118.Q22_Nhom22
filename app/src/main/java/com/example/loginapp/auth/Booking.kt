package com.example.loginapp.auth

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
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

fun getShowtimesByCinemaId(cinemaId: String, onResult: (List<Showtime>) -> Unit) {
    val db = FirebaseFirestore.getInstance()

    db.collection("showtimes")
        .whereEqualTo("cinemaId", cinemaId)
        .get()
        .addOnSuccessListener { result ->
            val showtimes = result.map { document ->
                val showtime = document.toObject(Showtime::class.java)
                showtime.copy(id = showtime.id.ifBlank { document.id })
            }
            onResult(showtimes)
        }
        .addOnFailureListener { exception ->
            println("Error getting showtimes by cinema: $exception")
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
    paymentMethod: String,
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
        "paymentMethod" to paymentMethod,
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
                .addOnSuccessListener {
                    // Xoá các seat locks của những ghế vừa đặt thành công
                    val batch = db.batch()
                    seats.forEach { seat ->
                        val lockRef = db.collection("seat_locks").document("${showtimeId}_${seat}")
                        batch.delete(lockRef)
                    }
                    batch.commit().addOnCompleteListener {
                        onSuccess(ticketId)
                    }
                }
                .addOnFailureListener { onFailure(it) }
        }
        .addOnFailureListener { onFailure(it) }
}

// ──────────────────────────────────────────────────────────────────────────────
// REAL-TIME SEAT LOCK
// Khi user chọn ghế, tạo một "lock" tạm thời 5 phút trên Firestore.
// Các thiết bị khác nhận cập nhật real-time và hiển thị ghế đó màu vàng.
// ──────────────────────────────────────────────────────────────────────────────

data class SeatLock(
    val showtimeId: String = "",
    val seatName: String = "",
    val userId: String = "",
    val expiresAt: Long = 0L          // epoch milliseconds
)

/**
 * Lắng nghe real-time danh sách ghế đang bị khoá (chưa hết hạn)
 * trong một suất chiếu. Trả về ListenerRegistration để có thể huỷ.
 */
fun observeSeatLocks(
    showtimeId: String,
    onUpdate: (List<SeatLock>) -> Unit
): ListenerRegistration {
    val db = FirebaseFirestore.getInstance()
    return db.collection("seat_locks")
        .whereEqualTo("showtimeId", showtimeId)
        .addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                onUpdate(emptyList())
                return@addSnapshotListener
            }
            val now = System.currentTimeMillis()
            val activeLocks = snapshot.documents.mapNotNull { doc ->
                doc.toObject(SeatLock::class.java)
            }.filter { it.expiresAt > now }
            onUpdate(activeLocks)
        }
}

/**
 * Khoá một ghế cho userId trong 5 phút.
 * Dùng transaction để tránh race-condition:
 *   - Nếu ghế đang bị người khác khoá và chưa hết hạn → onResult(false)
 *   - Ngược lại → tạo/ghi đè lock, onResult(true)
 */
fun lockSeat(
    showtimeId: String,
    seatName: String,
    userId: String,
    onResult: (success: Boolean) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val docId = "${showtimeId}_${seatName}"
    val docRef = db.collection("seat_locks").document(docId)
    val expiresAt = System.currentTimeMillis() + 5 * 60 * 1000L

    db.runTransaction { transaction ->
        val snapshot = transaction.get(docRef)
        val now = System.currentTimeMillis()
        if (snapshot.exists()) {
            val existingUserId = snapshot.getString("userId") ?: ""
            val existingExpiry = snapshot.getLong("expiresAt") ?: 0L
            // Ghế đang bị người KHÁC khoá và chưa hết hạn
            if (existingUserId != userId && existingExpiry > now) {
                throw Exception("Ghế đang được người khác giữ.")
            }
        }
        transaction.set(
            docRef,
            hashMapOf(
                "showtimeId" to showtimeId,
                "seatName" to seatName,
                "userId" to userId,
                "expiresAt" to expiresAt
            )
        )
    }
    .addOnSuccessListener { onResult(true) }
    .addOnFailureListener { onResult(false) }
}

/**
 * Huỷ khoá một ghế (khi user bỏ chọn).
 * Chỉ xoá nếu lock thuộc về userId hiện tại.
 */
fun unlockSeat(
    showtimeId: String,
    seatName: String,
    userId: String
) {
    val db = FirebaseFirestore.getInstance()
    val docId = "${showtimeId}_${seatName}"
    val docRef = db.collection("seat_locks").document(docId)
    docRef.get().addOnSuccessListener { snapshot ->
        val owner = snapshot.getString("userId") ?: ""
        if (owner == userId) {
            docRef.delete()
        }
    }
}

/**
 * Huỷ toàn bộ lock của userId trong một suất chiếu.
 * Gọi khi user thoát màn hình chọn ghế.
 */
fun unlockAllUserSeats(
    showtimeId: String,
    userId: String
) {
    val db = FirebaseFirestore.getInstance()
    db.collection("seat_locks")
        .whereEqualTo("showtimeId", showtimeId)
        .whereEqualTo("userId", userId)
        .get()
        .addOnSuccessListener { result ->
            if (result.isEmpty) return@addOnSuccessListener
            val batch = db.batch()
            result.documents.forEach { batch.delete(it.reference) }
            batch.commit()
        }
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
