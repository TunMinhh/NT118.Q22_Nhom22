package com.example.loginapp.auth

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

data class AuthUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val userEmail: String? = null,
    val displayName: String? = null,
    val errorMessage: String? = null,
    val infoMessage: String? = null
)

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    var uiState by mutableStateOf(
        AuthUiState(
            isAuthenticated = auth.currentUser != null,
            userEmail = auth.currentUser?.email,
            displayName = auth.currentUser?.displayName
        )
    )
        private set

    fun clearFeedback() {
        uiState = uiState.copy(errorMessage = null, infoMessage = null)
    }

    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit
    ) {
        val normalizedEmail = email.trim()

        when {
            normalizedEmail.isBlank() -> {
                uiState = uiState.copy(errorMessage = "Vui lòng nhập email.", infoMessage = null)
                return
            }

            !Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches() -> {
                uiState = uiState.copy(errorMessage = "Email không hợp lệ.", infoMessage = null)
                return
            }

            password.isBlank() -> {
                uiState = uiState.copy(errorMessage = "Vui lòng nhập mật khẩu.", infoMessage = null)
                return
            }
        }

        uiState = uiState.copy(isLoading = true, errorMessage = null, infoMessage = null)

        auth.signInWithEmailAndPassword(normalizedEmail, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val currentUser = auth.currentUser
                    uiState = uiState.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        userEmail = currentUser?.email,
                        displayName = currentUser?.displayName,
                        errorMessage = null,
                        infoMessage = "Đăng nhập thành công."
                    )
                    onSuccess()
                } else {
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = task.exception?.localizedMessage
                            ?: "Đăng nhập thất bại. Vui lòng thử lại.",
                        infoMessage = null
                    )
                }
            }
    }

    fun signUp(
        fullName: String,
        email: String,
        password: String,
        confirmPassword: String,
        onSuccess: () -> Unit
    ) {
        val normalizedName = fullName.trim()
        val normalizedEmail = email.trim()

        when {
            normalizedName.isBlank() -> {
                uiState = uiState.copy(errorMessage = "Vui lòng nhập họ và tên.", infoMessage = null)
                return
            }

            normalizedEmail.isBlank() -> {
                uiState = uiState.copy(errorMessage = "Vui lòng nhập email.", infoMessage = null)
                return
            }

            !Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches() -> {
                uiState = uiState.copy(errorMessage = "Email không hợp lệ.", infoMessage = null)
                return
            }

            password.length < 6 -> {
                uiState = uiState.copy(
                    errorMessage = "Mật khẩu phải có ít nhất 6 ký tự.",
                    infoMessage = null
                )
                return
            }

            password != confirmPassword -> {
                uiState = uiState.copy(errorMessage = "Mật khẩu xác nhận không khớp.", infoMessage = null)
                return
            }
        }

        uiState = uiState.copy(isLoading = true, errorMessage = null, infoMessage = null)

        auth.createUserWithEmailAndPassword(normalizedEmail, password)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = task.exception?.localizedMessage
                            ?: "Tạo tài khoản thất bại. Vui lòng thử lại.",
                        infoMessage = null
                    )
                    return@addOnCompleteListener
                }

                val currentUser = auth.currentUser
                if (currentUser == null) {
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = "Không tìm thấy thông tin tài khoản vừa tạo.",
                        infoMessage = null
                    )
                    return@addOnCompleteListener
                }

                val profileRequest = UserProfileChangeRequest.Builder()
                    .setDisplayName(normalizedName)
                    .build()

                currentUser.updateProfile(profileRequest)
                    .addOnCompleteListener { profileTask ->
                        saveUserProfile(
                            uid = currentUser.uid,
                            email = normalizedEmail,
                            displayName = normalizedName
                        ) { firestoreError ->
                            val infoMessage = when {
                                !profileTask.isSuccessful && firestoreError != null -> {
                                    "Tài khoản đã được tạo, nhưng hồ sơ người dùng chưa lưu đầy đủ."
                                }

                                !profileTask.isSuccessful -> {
                                    "Tài khoản đã được tạo, nhưng tên hiển thị chưa cập nhật."
                                }

                                firestoreError != null -> {
                                    "Tài khoản đã được tạo, nhưng chưa lưu được hồ sơ Firestore."
                                }

                                else -> "Tạo tài khoản thành công."
                            }

                            uiState = uiState.copy(
                                isLoading = false,
                                isAuthenticated = true,
                                userEmail = currentUser.email ?: normalizedEmail,
                                displayName = normalizedName,
                                errorMessage = null,
                                infoMessage = infoMessage
                            )
                            onSuccess()
                        }
                    }
            }
    }

    fun resetPassword(email: String) {
        val normalizedEmail = email.trim()

        when {
            normalizedEmail.isBlank() -> {
                uiState = uiState.copy(errorMessage = "Vui lòng nhập email.", infoMessage = null)
                return
            }

            !Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches() -> {
                uiState = uiState.copy(errorMessage = "Email không hợp lệ.", infoMessage = null)
                return
            }
        }

        uiState = uiState.copy(isLoading = true, errorMessage = null, infoMessage = null)

        auth.sendPasswordResetEmail(normalizedEmail)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = null,
                        infoMessage = "Đã gửi email đặt lại mật khẩu. Vui lòng kiểm tra hộp thư."
                    )
                } else {
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = task.exception?.localizedMessage
                            ?: "Không thể gửi email đặt lại mật khẩu.",
                        infoMessage = null
                    )
                }
            }
    }

    fun signOut() {
        auth.signOut()
        uiState = AuthUiState()
    }

    private fun saveUserProfile(
        uid: String,
        email: String,
        displayName: String,
        onComplete: (String?) -> Unit
    ) {
        val profile = hashMapOf(
            "id" to uid,
            "email" to email,
            "displayName" to displayName,
            "phoneNumber" to "",
            "favoriteGenres" to emptyList<String>(),
            "createdAt" to FieldValue.serverTimestamp()
        )

        firestore.collection("users")
            .document(uid)
            .set(profile)
            .addOnSuccessListener {
                onComplete(null)
            }
            .addOnFailureListener { exception ->
                onComplete(exception.localizedMessage ?: "Không thể lưu hồ sơ người dùng.")
            }
    }
}

