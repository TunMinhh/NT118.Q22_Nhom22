package com.example.loginapp.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.loginapp.R
import com.example.loginapp.ui.theme.LoginAppTheme
//By cam
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ForgotPasswordScreenWithLogic(onBackClick: () -> Unit) {
    // 1. Tạo các biến trạng thái để điều khiển giao diện
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }

    // 2. Gọi Firebase Auth
    val auth = FirebaseAuth.getInstance()

    // 3. Gọi lại cái giao diện xịn của bạn bạn ở đây và truyền logic vào
    ForgotPasswordScreen(
        onBackClick = onBackClick,
        isLoading = isLoading,
        errorMessage = errorMessage,
        infoMessage = infoMessage,
        onResetPasswordClick = { email ->
            // Khi người dùng bấm nút "GỬI YÊU CẦU", code ở đây sẽ chạy

            // Xóa thông báo cũ
            errorMessage = null
            infoMessage = null

            if (email.isBlank()) {
                errorMessage = "Vui lòng nhập email của bạn."
                return@ForgotPasswordScreen
            }

            // Bật vòng xoay loading
            isLoading = true

            // Gọi Firebase gửi email
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    // Tắt vòng xoay loading khi có kết quả
                    isLoading = false

                    if (task.isSuccessful) {
                        infoMessage = "Đã gửi link khôi phục! Vui lòng kiểm tra hộp thư."
                        errorMessage = null
                    } else {
                        errorMessage = "Lỗi: ${task.exception?.localizedMessage}"
                        infoMessage = null
                    }
                }
        }
    )
}
//

@Composable
fun ForgotPasswordScreen(
    onBackClick: () -> Unit,
    onResetPasswordClick: (String) -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    infoMessage: String?
) {
    // Lưu email hoặc số điện thoại người dùng nhập
    var account by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Giữ phần banner giống giao diện đăng nhập
        Image(
            painter = painterResource(id = R.drawable.login_banner),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Nút quay về màn hình trước
            TextButton(onClick = onBackClick, enabled = !isLoading) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lại"
                )
                Text(text = "Quay lại")
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Quên mật khẩu",
                fontSize = 28.sp,
                color = Color(0xFFB33A3A)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Nhập email để nhận hướng dẫn khôi phục mật khẩu.",
                color = Color.Gray,
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            TextField(
                value = account,
                onValueChange = { account = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )

            if (!errorMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = errorMessage,
                    color = Color(0xFFB00020)
                )
            }

            if (!infoMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = infoMessage,
                    color = Color(0xFF2E7D32)
                )
            }

            Spacer(modifier = Modifier.height(25.dp))

            // Nút gửi yêu cầu đặt lại mật khẩu
            Button(
                onClick = {
                    onResetPasswordClick(account)
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB33A3A)
                )
            ) {
                if (isLoading) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier
                                .width(18.dp)
                                .height(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ĐANG GỬI...", fontSize = 16.sp, color = Color.White)
                    }
                } else {
                    Text("GỬI YÊU CẦU", fontSize = 18.sp, color = Color.White)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ForgotPasswordScreenPreview() {
    LoginAppTheme {
        ForgotPasswordScreen(
            onBackClick = {},
            onResetPasswordClick = {},
            isLoading = false,
            errorMessage = null,
            infoMessage = null
        )
    }
}
