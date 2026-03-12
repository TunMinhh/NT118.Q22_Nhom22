package com.example.loginapp.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.loginapp.R
import com.example.loginapp.ui.theme.LoginAppTheme

@Composable
fun ForgotPasswordScreen(onBackClick: () -> Unit) {
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
            TextButton(onClick = onBackClick) {
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
                text = "Nhập email hoặc số điện thoại để nhận hướng dẫn khôi phục mật khẩu.",
                color = Color.Gray,
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Ô nhập email hoặc số điện thoại
            TextField(
                value = account,
                onValueChange = { account = it },
                label = { Text("Email hoặc số điện thoại") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(25.dp))

            // Nút gửi yêu cầu đặt lại mật khẩu
            Button(
                onClick = {
                    // Xử lý khi bấm gửi yêu cầu
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB33A3A)
                )
            ) {
                Text("GỬI YÊU CẦU", fontSize = 18.sp, color = Color.White)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ForgotPasswordScreenPreview() {
    LoginAppTheme {
        ForgotPasswordScreen(onBackClick = {})
    }
}
